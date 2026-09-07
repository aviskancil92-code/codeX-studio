package com.example.core.model

enum class DiagnosticSeverity {
    ERROR,
    WARNING,
    INFORMATION,
    HINT
}

data class DiagnosticItem(
    val file: String,
    val line: Int,
    val column: Int,
    val severity: DiagnosticSeverity,
    val message: String,
    val suggestedFix: String? = null
)

data class BuildResult(
    val step: String,
    val command: String,
    val workingDir: String,
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val durationMs: Long,
    val memoryKb: Long = 0,
    val isSuccess: Boolean = exitCode == 0,
    val diagnostics: List<DiagnosticItem> = emptyList(),
    val artifactPath: String? = null
)
