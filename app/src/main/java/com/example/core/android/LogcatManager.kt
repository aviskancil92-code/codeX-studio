package com.example.core.android

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

data class LogcatEntry(
    val time: String,
    val pid: String,
    val tid: String,
    val level: String, // V, D, I, W, E, F
    val tag: String,
    val message: String
)

class LogcatManager {

    private val logPattern = Pattern.compile("^(\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+(\\d+)\\s+(\\d+)\\s+([VDIWEF])\\s+([^:]+):\\s+(.*)$")

    suspend fun readLogs(
        filterLevel: String = "ALL",
        searchQuery: String = "",
        limit: Int = 300
    ): List<LogcatEntry> = withContext(Dispatchers.IO) {
        val entries = mutableListOf<LogcatEntry>()
        try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/bin/logcat", "-d", "-v", "time"))
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val raw = line ?: continue
                    val matcher = logPattern.matcher(raw)
                    if (matcher.matches()) {
                        val time = matcher.group(1) ?: ""
                        val pid = matcher.group(2) ?: ""
                        val tid = matcher.group(3) ?: ""
                        val level = matcher.group(4) ?: "I"
                        val tag = matcher.group(5)?.trim() ?: ""
                        val message = matcher.group(6) ?: ""

                        // Check Level Filter
                        if (filterLevel != "ALL" && !matchesLevel(level, filterLevel)) {
                            continue
                        }

                        // Check Search Filter
                        if (searchQuery.isNotBlank() &&
                            !tag.contains(searchQuery, ignoreCase = true) &&
                            !message.contains(searchQuery, ignoreCase = true)
                        ) {
                            continue
                        }

                        entries.add(LogcatEntry(time, pid, tid, level, tag, message))
                    } else if (raw.isNotBlank()) {
                        // Unstructured log fallback
                        if (searchQuery.isBlank() || raw.contains(searchQuery, ignoreCase = true)) {
                            entries.add(LogcatEntry("", "", "", "I", "System", raw))
                        }
                    }
                }
            }
            process.waitFor()
        } catch (e: Exception) {
            entries.add(LogcatEntry("", "", "", "E", "LogcatManager", "Failed to query system logcat: ${e.message}"))
        }

        entries.takeLast(limit)
    }

    private fun matchesLevel(actual: String, filter: String): Boolean {
        val ranks = mapOf("V" to 1, "D" to 2, "I" to 3, "W" to 4, "E" to 5, "F" to 6)
        val filterRank = when (filter.uppercase()) {
            "VERBOSE", "V" -> 1
            "DEBUG", "D" -> 2
            "INFO", "I" -> 3
            "WARN", "WARNING", "W" -> 4
            "ERROR", "E" -> 5
            "FATAL", "F" -> 6
            else -> 1
        }
        val actualRank = ranks[actual] ?: 1
        return actualRank >= filterRank
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        try {
            Runtime.getRuntime().exec(arrayOf("/system/bin/logcat", "-c")).waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
