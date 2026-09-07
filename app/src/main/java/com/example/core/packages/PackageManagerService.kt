package com.example.core.packages

import com.example.core.model.PackageItem
import java.io.File

class PackageManagerService {

    fun detectEcosystem(projectDir: File): String {
        return when {
            File(projectDir, "requirements.txt").exists() || File(projectDir, "pyproject.toml").exists() -> "pip"
            File(projectDir, "package.json").exists() -> "npm"
            File(projectDir, "build.gradle.kts").exists() || File(projectDir, "build.gradle").exists() -> "Gradle / Maven"
            File(projectDir, "Cargo.toml").exists() -> "Cargo"
            File(projectDir, "pubspec.yaml").exists() -> "pub"
            File(projectDir, "go.mod").exists() -> "Go Modules"
            else -> "pip"
        }
    }

    fun getPopularPackages(ecosystem: String): List<PackageItem> {
        return when (ecosystem.lowercase()) {
            "pip" -> listOf(
                PackageItem("requests", "2.31.0", "A simple, yet elegant HTTP library for Python.", "pip", true, "2.31.0", "Apache-2.0"),
                PackageItem("numpy", "1.26.4", "Fundamental package for scientific computing with Python arrays.", "pip", false, "1.26.4", "BSD-3-Clause"),
                PackageItem("flask", "3.0.3", "A lightweight WSGI web application framework.", "pip", false, "3.0.3", "BSD-3-Clause"),
                PackageItem("beautifulsoup4", "4.12.3", "Screen-scraping library for parsing HTML and XML.", "pip", false, "4.12.3", "MIT"),
                PackageItem("pytest", "8.1.1", "Robust framework for writing small and complex tests.", "pip", false, "8.1.1", "MIT")
            )
            "npm" -> listOf(
                PackageItem("express", "4.19.2", "Fast, unopinionated, minimalist web framework for Node.js.", "npm", true, "4.19.2", "MIT"),
                PackageItem("axios", "1.6.8", "Promise based HTTP client for the browser and node.js.", "npm", false, "1.6.8", "MIT"),
                PackageItem("react", "18.3.1", "The library for web and native user interfaces.", "npm", false, "18.3.1", "MIT"),
                PackageItem("typescript", "5.4.5", "TypeScript is a language for application-scale JavaScript.", "npm", false, "5.4.5", "Apache-2.0")
            )
            "gradle / maven" -> listOf(
                PackageItem("androidx.core:core-ktx", "1.13.1", "Kotlin extensions for core Android libraries.", "Gradle", true, "1.13.1", "Apache-2.0"),
                PackageItem("org.jetbrains.kotlinx:kotlinx-coroutines-android", "1.8.0", "Coroutines support libraries for Android.", "Gradle", true, "1.8.0", "Apache-2.0"),
                PackageItem("com.squareup.okhttp3:okhttp", "4.12.0", "Square's meticulous HTTP client for the JVM and Android.", "Gradle", false, "4.12.0", "Apache-2.0"),
                PackageItem("com.google.code.gson:gson", "2.10.1", "Java serialization/deserialization library for JSON.", "Gradle", false, "2.10.1", "Apache-2.0")
            )
            "cargo" -> listOf(
                PackageItem("serde", "1.0.203", "A generic serialization/deserialization framework for Rust.", "Cargo", true, "1.0.203", "MIT OR Apache-2.0"),
                PackageItem("tokio", "1.38.0", "An event-driven, non-blocking I/O platform for writing asynchronous applications.", "Cargo", false, "1.38.0", "MIT"),
                PackageItem("jni", "0.21.1", "Rust bindings to the JNI (Java Native Interface) for Android.", "Cargo", false, "0.21.1", "MIT OR Apache-2.0")
            )
            else -> listOf(
                PackageItem("requests", "2.31.0", "HTTP client library", ecosystem, true, "2.31.0")
            )
        }
    }

    fun installPackage(projectDir: File, pkg: PackageItem): Boolean {
        return try {
            when (pkg.ecosystem.lowercase()) {
                "pip" -> {
                    val reqFile = File(projectDir, "requirements.txt")
                    val existing = if (reqFile.exists()) reqFile.readText() else ""
                    if (!existing.contains(pkg.name)) {
                        reqFile.appendText("\n${pkg.name}>=${pkg.version}\n")
                    }
                    true
                }
                "npm" -> {
                    val pkgFile = File(projectDir, "package.json")
                    if (pkgFile.exists()) {
                        val content = pkgFile.readText()
                        if (content.contains("\"dependencies\": {")) {
                            val updated = content.replace(
                                "\"dependencies\": {",
                                "\"dependencies\": {\n    \"${pkg.name}\": \"^${pkg.version}\","
                            )
                            pkgFile.writeText(updated)
                        }
                    }
                    true
                }
                "gradle", "gradle / maven" -> {
                    val gradleFile = File(projectDir, "build.gradle.kts")
                    if (gradleFile.exists()) {
                        val content = gradleFile.readText()
                        if (content.contains("dependencies {")) {
                            val updated = content.replace(
                                "dependencies {",
                                "dependencies {\n    implementation(\"${pkg.name}:${pkg.version}\")"
                            )
                            gradleFile.writeText(updated)
                        }
                    }
                    true
                }
                "cargo" -> {
                    val cargoFile = File(projectDir, "Cargo.toml")
                    if (cargoFile.exists()) {
                        val content = cargoFile.readText()
                        if (content.contains("[dependencies]")) {
                            val updated = content.replace(
                                "[dependencies]",
                                "[dependencies]\n${pkg.name} = \"${pkg.version}\""
                            )
                            cargoFile.writeText(updated)
                        }
                    }
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
