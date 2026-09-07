package com.example.core.model

enum class AndroidCompatibility {
    SUPPORTED,
    PARTIALLY_SUPPORTED,
    REQUIRES_TOOLCHAIN,
    ANDROID_INCOMPATIBLE,
    NOT_INSTALLED;

    val label: String
        get() = when (this) {
            SUPPORTED -> "SUPPORTED"
            PARTIALLY_SUPPORTED -> "PARTIALLY SUPPORTED"
            REQUIRES_TOOLCHAIN -> "REQUIRES TOOLCHAIN"
            ANDROID_INCOMPATIBLE -> "ANDROID INCOMPATIBLE"
            NOT_INSTALLED -> "NOT INSTALLED"
        }
}

data class CapabilityMatrix(
    val language: String,
    val compile: Boolean,
    val run: Boolean,
    val debug: Boolean,
    val hotReload: String, // "full", "limited", "none", "compose-live-edit", "metro"
    val androidBuild: String, // "direct-apk", "framework-dependent", "ndk-so", "none"
    val packageManager: Boolean,
    val offlineSupport: Boolean,
    val compatibility: AndroidCompatibility,
    val notes: String = ""
)
