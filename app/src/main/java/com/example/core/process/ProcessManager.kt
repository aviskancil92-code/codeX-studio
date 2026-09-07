package com.example.core.process

import android.content.Context
import com.example.core.model.BuildResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.*
import java.util.UUID

data class TerminalLine(
    val text: String,
    val type: LineType = LineType.OUTPUT,
    val timestamp: Long = System.currentTimeMillis()
)

enum class LineType {
    INPUT,
    OUTPUT,
    ERROR,
    SYSTEM
}

data class TerminalSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "bash",
    val lines: List<TerminalLine> = emptyList(),
    val isRunning: Boolean = false
)

class ProcessManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _sessions = MutableStateFlow<List<TerminalSession>>(listOf(TerminalSession(title = "Terminal 1")))
    val sessions: StateFlow<List<TerminalSession>> = _sessions.asStateFlow()

    private val _activeSessionId = MutableStateFlow<String>(_sessions.value.first().id)
    val activeSessionId: StateFlow<String> = _activeSessionId.asStateFlow()

    private val activeProcesses = mutableMapOf<String, Process>()
    private val activeWriters = mutableMapOf<String, BufferedWriter>()

    fun createSession(title: String = "Terminal ${_sessions.value.size + 1}"): String {
        val newSession = TerminalSession(title = title)
        _sessions.value = _sessions.value + newSession
        _activeSessionId.value = newSession.id
        return newSession.id
    }

    fun closeSession(sessionId: String) {
        killProcess(sessionId)
        val current = _sessions.value
        if (current.size > 1) {
            val updated = current.filter { it.id != sessionId }
            _sessions.value = updated
            if (_activeSessionId.value == sessionId) {
                _activeSessionId.value = updated.first().id
            }
        } else {
            clearSession(sessionId)
        }
    }

    fun selectSession(sessionId: String) {
        _activeSessionId.value = sessionId
    }

    fun clearSession(sessionId: String) {
        _sessions.value = _sessions.value.map { session ->
            if (session.id == sessionId) session.copy(lines = emptyList()) else session
        }
    }

    private fun appendLine(sessionId: String, line: String, type: LineType) {
        _sessions.value = _sessions.value.map { session ->
            if (session.id == sessionId) {
                session.copy(lines = (session.lines + TerminalLine(line, type)).takeLast(1000))
            } else session
        }
    }

    fun sendCommand(sessionId: String, command: String, workingDir: File) {
        if (command.isBlank()) return
        appendLine(sessionId, "$ $command", LineType.INPUT)

        // Handle internal clear command
        if (command.trim().equals("clear", ignoreCase = true)) {
            clearSession(sessionId)
            return
        }

        scope.launch {
            try {
                _sessions.value = _sessions.value.map {
                    if (it.id == sessionId) it.copy(isRunning = true) else it
                }

                val startTime = System.currentTimeMillis()
                val processBuilder = ProcessBuilder("/system/bin/sh", "-c", command)
                processBuilder.directory(if (workingDir.exists()) workingDir else context.filesDir)

                val env = processBuilder.environment()
                env["HOME"] = context.filesDir.absolutePath
                env["TMPDIR"] = context.cacheDir.absolutePath
                env["PATH"] = "${env["PATH"] ?: "/system/bin:/system/xbin"}:${context.filesDir.absolutePath}/bin"

                val process = processBuilder.start()
                activeProcesses[sessionId] = process

                val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
                val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

                launch {
                    var line: String?
                    while (stdoutReader.readLine().also { line = it } != null) {
                        appendLine(sessionId, line ?: "", LineType.OUTPUT)
                    }
                }

                launch {
                    var errLine: String?
                    while (stderrReader.readLine().also { errLine = it } != null) {
                        appendLine(sessionId, errLine ?: "", LineType.ERROR)
                    }
                }

                val exitCode = process.waitFor()
                val elapsed = System.currentTimeMillis() - startTime

                appendLine(sessionId, "[Process exited with code $exitCode in ${elapsed}ms]", LineType.SYSTEM)
            } catch (e: Exception) {
                appendLine(sessionId, "Error: ${e.message}", LineType.ERROR)
            } finally {
                activeProcesses.remove(sessionId)
                _sessions.value = _sessions.value.map {
                    if (it.id == sessionId) it.copy(isRunning = false) else it
                }
            }
        }
    }

    fun killProcess(sessionId: String) {
        activeProcesses[sessionId]?.let { process ->
            try {
                process.destroy()
                appendLine(sessionId, "[Process terminated by user]", LineType.SYSTEM)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        activeProcesses.remove(sessionId)
        _sessions.value = _sessions.value.map {
            if (it.id == sessionId) it.copy(isRunning = false) else it
        }
    }

    // Synchronous execution helper for Run / Build / Git pipelines
    suspend fun executeCommand(
        command: String,
        workingDir: File,
        extraEnv: Map<String, String> = emptyMap()
    ): BuildResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val stdoutBuilder = StringBuilder()
        val stderrBuilder = StringBuilder()

        try {
            val processBuilder = ProcessBuilder("/system/bin/sh", "-c", command)
            processBuilder.directory(if (workingDir.exists()) workingDir else context.filesDir)

            val env = processBuilder.environment()
            env["HOME"] = context.filesDir.absolutePath
            env["TMPDIR"] = context.cacheDir.absolutePath
            env["PATH"] = "${env["PATH"] ?: "/system/bin:/system/xbin"}:${context.filesDir.absolutePath}/bin"
            extraEnv.forEach { (k, v) -> env[k] = v }

            val process = processBuilder.start()

            val stdoutJob = launch {
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stdoutBuilder.append(line).append("\n")
                    }
                }
            }

            val stderrJob = launch {
                BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stderrBuilder.append(line).append("\n")
                    }
                }
            }

            val exitCode = process.waitFor()
            stdoutJob.join()
            stderrJob.join()

            val elapsed = System.currentTimeMillis() - startTime
            val usedMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024

            BuildResult(
                step = "Execution",
                command = command,
                workingDir = workingDir.absolutePath,
                exitCode = exitCode,
                stdout = stdoutBuilder.toString().trimEnd(),
                stderr = stderrBuilder.toString().trimEnd(),
                durationMs = elapsed,
                memoryKb = usedMem,
                isSuccess = exitCode == 0
            )
        } catch (e: Exception) {
            BuildResult(
                step = "Execution",
                command = command,
                workingDir = workingDir.absolutePath,
                exitCode = -1,
                stdout = stdoutBuilder.toString().trimEnd(),
                stderr = "Execution failure: ${e.message}",
                durationMs = System.currentTimeMillis() - startTime,
                isSuccess = false
            )
        }
    }
}
