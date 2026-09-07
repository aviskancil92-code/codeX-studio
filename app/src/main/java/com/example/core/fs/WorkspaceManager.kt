package com.example.core.fs

import android.content.Context
import com.example.core.model.ProjectTemplate
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class FileNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val children: List<FileNode> = emptyList(),
    val sizeBytes: Long = 0,
    val isExpanded: Boolean = false,
    val extension: String = ""
)

class WorkspaceManager(private val context: Context) {

    val rootDir: File = File(context.filesDir, "workspaces").apply {
        if (!exists()) mkdirs()
    }

    fun getProjectDir(projectName: String): File {
        val dir = File(rootDir, projectName)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun listProjects(): List<File> {
        return rootDir.listFiles { file -> file.isDirectory }?.toList() ?: emptyList()
    }

    fun buildFileTree(directory: File, expandedPaths: Set<String> = emptySet()): FileNode {
        val isExpanded = expandedPaths.contains(directory.absolutePath)
        val files = directory.listFiles() ?: emptyArray()
        
        // Sort: directories first, then alphabetically
        val sortedFiles = files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        
        val children = if (isExpanded || directory == rootDir) {
            sortedFiles.map { file ->
                if (file.isDirectory) {
                    buildFileTree(file, expandedPaths)
                } else {
                    FileNode(
                        name = file.name,
                        path = file.absolutePath,
                        isDirectory = false,
                        sizeBytes = file.length(),
                        extension = file.extension
                    )
                }
            }
        } else {
            emptyList()
        }

        return FileNode(
            name = directory.name,
            path = directory.absolutePath,
            isDirectory = true,
            children = children,
            isExpanded = isExpanded
        )
    }

    fun createFile(parentDir: File, name: String, content: String = ""): File {
        val file = File(parentDir, name)
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.writeText(content)
        }
        return file
    }

    fun createDirectory(parentDir: File, name: String): File {
        val dir = File(parentDir, name)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun readFile(file: File): String {
        return if (file.exists() && file.isFile) {
            file.readText()
        } else ""
    }

    fun writeFile(file: File, content: String) {
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    fun rename(file: File, newName: String): File {
        val target = File(file.parentFile, newName)
        file.renameTo(target)
        return target
    }

    fun delete(file: File): Boolean {
        return if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    // ZIP Project Export
    fun exportToZip(projectDir: File, outputZip: File): Boolean {
        return try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(outputZip))).use { zos ->
                val baseUri = projectDir.toURI()
                projectDir.walkTopDown().forEach { file ->
                    val relativePath = baseUri.relativize(file.toURI()).path
                    if (file.isDirectory) {
                        if (relativePath.isNotEmpty()) {
                            zos.putNextEntry(ZipEntry("$relativePath/"))
                            zos.closeEntry()
                        }
                    } else {
                        zos.putNextEntry(ZipEntry(relativePath))
                        file.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ZIP Project Import
    fun importFromZip(zipFile: File, destinationDir: File): Boolean {
        return try {
            destinationDir.mkdirs()
            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val targetFile = File(destinationDir, entry.name)
                    // Security check against Zip Slip
                    if (!targetFile.canonicalPath.startsWith(destinationDir.canonicalPath)) {
                        throw SecurityException("Zip entry attempted directory traversal: ${entry.name}")
                    }
                    if (entry.isDirectory) {
                        targetFile.mkdirs()
                    } else {
                        targetFile.parentFile?.mkdirs()
                        FileOutputStream(targetFile).use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    val templates: List<ProjectTemplate> = listOf(
        ProjectTemplate(
            id = "python_cli",
            title = "Python CLI & Data App",
            language = "Python",
            description = "Standalone Python application with CLI parser, data manipulation, and pip requirements.",
            defaultRunFile = "main.py",
            files = mapOf(
                "main.py" to """import sys
import os
import math

def calculate_stats(data):
    total = sum(data)
    mean = total / len(data) if data else 0
    variance = sum((x - mean) ** 2 for x in data) / len(data) if data else 0
    std_dev = math.sqrt(variance)
    return {
        "count": len(data),
        "sum": total,
        "mean": round(mean, 2),
        "std_dev": round(std_dev, 2)
    }

def main():
    print("========================================")
    print("      CodeX Studio - Python Runner      ")
    print("========================================")
    print(f"Executing with Python: {sys.version.split()[0]}")
    print(f"Current Directory: {os.getcwd()}")
    
    sample_data = [12, 45, 67, 89, 23, 56, 78, 90, 34, 11]
    stats = calculate_stats(sample_data)
    
    print("\n[Analysis Results]")
    for key, val in stats.items():
        print(f" • {key.capitalize()}: {val}")
        
    print("\n✓ Process completed with status code 0.")

if __name__ == '__main__':
    main()
""",
                "requirements.txt" to """# CodeX Python Dependencies
requests>=2.28.0
numpy>=1.24.0
""",
                "README.md" to """# Python CLI Project
Created with **CodeX Studio Mobile IDE**.
Run using the top **▶ Run** button or via Terminal: `python main.py`.
"""
            )
        ),
        ProjectTemplate(
            id = "android_kotlin",
            title = "Android Kotlin Application",
            language = "Kotlin",
            description = "Android mobile application layout with Kotlin Activity, AndroidManifest, and resources.",
            defaultRunFile = "MainActivity.kt",
            files = mapOf(
                "MainActivity.kt" to """package com.example.codexdemo

class MainActivity {
    fun onCreate() {
        println("=== CodeX Android Kotlin Runtime ===")
        println("Android SDK: 35 (VanillaIceCream)")
        println("Initializing UI Components...")
        renderGreeting("CodeX Developer")
    }

    private fun renderGreeting(user: String) {
        val message = "Welcome to CodeX Studio, ${'$'}user!"
        println("[View Rendered]: ${'$'}message")
        println("✓ Activity state: RESUMED")
    }
}

fun main() {
    val activity = MainActivity()
    activity.onCreate()
}
""",
                "AndroidManifest.xml" to """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.codexdemo">
    <application
        android:label="CodeX Demo"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
""",
                "build.gradle.kts" to """plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.codexdemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.codexdemo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}
""",
                "README.md" to """# Android Kotlin App
Full Android application source structure ready for compilation and DEX/APK packaging.
"""
            )
        ),
        ProjectTemplate(
            id = "cpp_ndk",
            title = "C++ Native / NDK Module",
            language = "C / C++",
            description = "C++ project with CMakeLists.txt for native Android JNI or binary compilation.",
            defaultRunFile = "main.cpp",
            files = mapOf(
                "main.cpp" to """#include <iostream>
#include <string>
#include <vector>

class AlgorithmBenchmark {
public:
    static void run() {
        std::cout << "--- CodeX C++ NDK Engine ---" << std::endl;
        std::vector<std::string> modules = {"Bionic libc", "LLVM Clang", "CMake 3.28", "JNI Bridge"};
        
        std::cout << "Active Native Subsystems:" << std::endl;
        for (const auto& mod : modules) {
            std::cout << " [+] " << mod << std::endl;
        }
        std::cout << "Native execution exit code: 0" << std::endl;
    }
};

int main() {
    AlgorithmBenchmark::run();
    return 0;
}
""",
                "CMakeLists.txt" to """cmake_minimum_required(VERSION 3.22.1)
project("codex_native" CXX)

set(CMAKE_CXX_STANDARD 17)

add_executable(codex_native main.cpp)
""",
                "README.md" to """# C++ NDK Native Project
Compile with Android Clang / CMake toolchain to produce ELF executables or .so libraries.
"""
            )
        ),
        ProjectTemplate(
            id = "node_js",
            title = "Node.js / Express Server",
            language = "JavaScript / TypeScript",
            description = "Node.js application with package.json, async handling, and JSON REST routes.",
            defaultRunFile = "index.js",
            files = mapOf(
                "index.js" to """// CodeX Studio - Node.js Runtime
const os = require('os');

console.log("========================================");
console.log("   CodeX Studio Node.js Environment     ");
console.log("========================================");
console.log(`Node Version: ${'$'}{process.version}`);
console.log(`Architecture: ${'$'}{process.arch}`);
console.log(`Total Memory: ${'$'}{Math.round(os.totalmem() / (1024 * 1024))} MB`);
console.log(`Free Memory:  ${'$'}{Math.round(os.freemem() / (1024 * 1024))} MB`);

const serverSimulation = () => {
    const endpoints = [
        { route: "/api/status", method: "GET", status: 200 },
        { route: "/api/compile", method: "POST", status: 201 },
        { route: "/api/terminal", method: "WS", status: 101 }
    ];
    console.log("\nRegistered API Endpoints:");
    endpoints.forEach(e => console.log(` [${'$'}{e.method}] ${'$'}{e.route} -> ${'$'}{e.status}`));
    console.log("\n✓ Microservice listening on port 8080");
};

serverSimulation();
""",
                "package.json" to """{
  "name": "codex-node-app",
  "version": "1.0.0",
  "description": "Node.js app built inside CodeX Studio Mobile IDE",
  "main": "index.js",
  "scripts": {
    "start": "node index.js"
  },
  "dependencies": {
    "express": "^4.18.2"
  }
}
""",
                "README.md" to """# Node.js Project
Run with **▶ Run** or via Terminal with `node index.js`.
"""
            )
        ),
        ProjectTemplate(
            id = "rust_cargo",
            title = "Rust Console Binary",
            language = "Rust",
            description = "Standard Rust Cargo package with Cargo.toml and typed structures.",
            defaultRunFile = "src/main.rs",
            files = mapOf(
                "src/main.rs" to """// CodeX Studio - Rust Toolchain
struct SystemSpecs {
    arch: &'static str,
    target: &'static str,
    optimizations: bool,
}

fn main() {
    println!("=== CodeX Studio Rust Engine ===");
    let specs = SystemSpecs {
        arch: "aarch64",
        target: "aarch64-linux-android",
        optimizations: true,
    };
    
    println!("Target Architecture: {}", specs.arch);
    println!("Compilation Target:   {}", specs.target);
    println!("Memory Safety:        Guaranteed by borrow checker");
    println!("✓ Rust binary finished execution.");
}
""",
                "Cargo.toml" to """[package]
name = "codex_rust_app"
version = "0.1.0"
edition = "2021"

[dependencies]
serde = { version = "1.0", features = ["derive"] }
""",
                "README.md" to """# Rust Cargo Project
Build with Cargo toolchain: `cargo build` or `cargo run`.
"""
            )
        ),
        ProjectTemplate(
            id = "web_frontend",
            title = "Web Frontend (HTML/CSS/JS)",
            language = "HTML / Web",
            description = "Responsive HTML5/CSS3 application with interactive JavaScript DOM events.",
            defaultRunFile = "index.html",
            files = mapOf(
                "index.html" to """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>CodeX Web App</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="card">
    <h1>CodeX Studio Web</h1>
    <p>Live Web Application running in Android.</p>
    <div id="counter-box">
      <span id="counter">0</span>
      <div class="btn-group">
        <button id="inc-btn">+</button>
        <button id="reset-btn">Reset</button>
      </div>
    </div>
  </div>
  <script src="app.js"></script>
</body>
</html>
""",
                "style.css" to """body {
  background: #0f141c;
  color: #e6edf3;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  margin: 0;
}
.card {
  background: #161b22;
  border: 1px solid #30363d;
  padding: 24px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 8px 24px rgba(0,0,0,0.4);
}
h1 { color: #58a6ff; margin-bottom: 8px; }
#counter { font-size: 48px; font-weight: bold; color: #7ee787; display: block; margin: 16px 0; }
button {
  background: #238636;
  border: none;
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  margin: 4px;
}
#reset-btn { background: #da3633; }
""",
                "app.js" to """let count = 0;
const counterEl = document.getElementById('counter');
document.getElementById('inc-btn').addEventListener('click', () => {
  count++;
  counterEl.textContent = count;
});
document.getElementById('reset-btn').addEventListener('click', () => {
  count = 0;
  counterEl.textContent = count;
});
console.log("Web Application initialized successfully in CodeX Studio!");
"""
            )
        )
    )

    fun createFromTemplate(templateId: String, projectName: String): File {
        val template = templates.firstOrNull { it.id == templateId } ?: templates.first()
        val dir = getProjectDir(projectName)
        template.files.forEach { (relPath, content) ->
            val targetFile = File(dir, relPath)
            targetFile.parentFile?.mkdirs()
            targetFile.writeText(content)
        }
        return dir
    }

    // Default initialization on first launch
    fun ensureInitialWorkspace(): File {
        val existing = listProjects()
        return if (existing.isNotEmpty()) {
            existing.first()
        } else {
            createFromTemplate("python_cli", "WelcomeProject")
        }
    }
}
