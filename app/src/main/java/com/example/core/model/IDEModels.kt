package com.example.core.model

data class ToolchainInfo(
    val id: String,
    val name: String,
    val version: String,
    val status: String,
    val isInstalled: Boolean,
    val location: String,
    val architecture: String,
    val sizeMb: Double,
    val dependencies: List<String>,
    val abiSupported: Boolean,
    val isDefault: Boolean = false,
    val description: String = ""
)

data class DoctorCheck(
    val category: String,
    val title: String,
    val status: DoctorStatus,
    val message: String,
    val detail: String = "",
    val actionLabel: String? = null
)

enum class DoctorStatus {
    PASS,
    WARN,
    FAIL
}

data class GitCommit(
    val hash: String,
    val message: String,
    val author: String,
    val timestamp: Long
)

data class GitStatus(
    val branch: String,
    val isRepo: Boolean = true,
    val stagedFiles: List<String> = emptyList(),
    val unstagedFiles: List<String> = emptyList(),
    val untrackedFiles: List<String> = emptyList(),
    val commits: List<GitCommit> = emptyList(),
    val remoteUrl: String = ""
)

data class ExtensionItem(
    val id: String,
    val name: String,
    val version: String,
    val publisher: String,
    val description: String,
    val isInstalled: Boolean,
    val isEnabled: Boolean = true,
    val category: String,
    val compatibility: AndroidCompatibility,
    val compatibilityReason: String,
    val tags: List<String> = emptyList()
)

data class PackageItem(
    val name: String,
    val version: String,
    val description: String,
    val ecosystem: String,
    val isInstalled: Boolean,
    val latestVersion: String,
    val license: String = "MIT",
    val author: String = ""
)

data class ProjectTemplate(
    val id: String,
    val title: String,
    val language: String,
    val description: String,
    val defaultRunFile: String,
    val files: Map<String, String>
)
