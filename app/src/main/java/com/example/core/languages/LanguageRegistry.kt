package com.example.core.languages

import com.example.core.model.AndroidCompatibility
import com.example.core.model.CapabilityMatrix
import com.example.core.model.LanguageDefinition

object LanguageRegistry {

    private val languages: List<LanguageDefinition> = listOf(
        LanguageDefinition(
            id = "python",
            name = "Python",
            extensions = listOf(".py", ".pyw", ".pyx"),
            runtime = "Python 3.11 / Android Native Runner",
            compiler = null,
            buildSystem = "pip / pyproject / Buildozer",
            packageManager = "pip",
            runner = "Python Process Runner",
            debugger = "debugpy (GDB bridge)",
            capabilities = CapabilityMatrix(
                language = "Python",
                compile = false,
                run = true,
                debug = true,
                hotReload = "process-restart",
                androidBuild = "framework-dependent (Buildozer/Chaquopy)",
                packageManager = true,
                offlineSupport = true,
                compatibility = AndroidCompatibility.SUPPORTED,
                notes = "Runs directly on Android shell / native Python engine. Supports standard library, scripts, and pip requirements."
            ),
            defaultFileName = "main.py",
            sampleCode = """# CodeX Studio - Python Runner
import sys
import os
import platform

def main():
    print(f"=== CodeX Python Engine ===")
    print(f"Python Version: {sys.version.split()[0]}")
    print(f"OS Platform: {platform.system()} ({sys.platform})")
    print(f"Working Dir: {os.getcwd()}")
    
    numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
    evens = [x for x in numbers if x % 2 == 0]
    print(f"Computed Evens: {evens}")
    print("Execution completed successfully!")

if __name__ == "__main__":
    main()
"""
        ),
        LanguageDefinition(
            id = "kotlin",
            name = "Kotlin",
            extensions = listOf(".kt", ".kts"),
            runtime = "ART / Android JVM",
            compiler = "kotlinc / AGP 8+",
            buildSystem = "Gradle",
            packageManager = "Maven / Gradle",
            runner = "Android APK Launcher / D8 Runner",
            debugger = "JDWP / Android Studio Debugger",
            capabilities = CapabilityMatrix(
                language = "Kotlin",
                compile = true,
                run = true,
                debug = true,
                hotReload = "compose-live-edit",
                androidBuild = "direct-apk",
                packageManager = true,
                offlineSupport = true,
                compatibility = AndroidCompatibility.SUPPORTED,
                notes = "Full Android first-class citizen. Compiles to DEX/APK with Android Gradle Plugin & Compose support."
            ),
            defaultFileName = "MainActivity.kt",
            sampleCode = """package com.example.codex

fun main() {
    println("=== CodeX Studio Kotlin Engine ===")
    val greeting = "Hello from Android Native Kotlin!"
    println(greeting)
    
    val primes = (2..20).filter { n ->
        (2 until n).none { n % it == 0 }
    }
    println("Primes up to 20: ${'$'}primes")
}
"""
        ),
        LanguageDefinition(
            id = "java",
            name = "Java",
            extensions = listOf(".java"),
            runtime = "OpenJDK 17/21 / Dalvik ART",
            compiler = "javac / ECJ",
            buildSystem = "Gradle / Maven",
            packageManager = "Maven / Gradle",
            runner = "D8 / Android APK Runner",
            debugger = "JDWP",
            capabilities = CapabilityMatrix(
                language = "Java",
                compile = true,
                run = true,
                debug = true,
                hotReload = "apply-changes",
                androidBuild = "direct-apk",
                packageManager = true,
                offlineSupport = true,
                compatibility = AndroidCompatibility.SUPPORTED,
                notes = "Native Android platform support. Compiles with javac / ECJ and packages to APK via D8/R8."
            ),
            defaultFileName = "Main.java",
            sampleCode = """package com.example.codex;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== CodeX Studio Java Engine ===");
        System.out.println("Java Runtime: " + System.getProperty("java.version", "Android ART"));
        System.out.println("Memory Available: " + Runtime.getRuntime().freeMemory() / 1024 + " KB");
    }
}
"""
        ),
        LanguageDefinition(
            id = "cpp",
            name = "C / C++",
            extensions = listOf(".cpp", ".c", ".h", ".hpp", ".cc"),
            runtime = "Android Bionic libc / NDK",
            compiler = "Clang / LLVM",
            buildSystem = "CMake / ndk-build",
            packageManager = "vcpkg / CMake FetchContent",
            runner = "Native ELF / lib.so Executable",
            debugger = "LLDB",
            capabilities = CapabilityMatrix(
                language = "C / C++",
                compile = true,
                run = true,
                debug = true,
                hotReload = "none",
                androidBuild = "ndk-so",
                packageManager = false,
                offlineSupport = true,
                compatibility = AndroidCompatibility.SUPPORTED,
                notes = "Integrated with Android NDK toolchain (Clang + CMake). Produces .so shared libraries and native binaries."
            ),
            defaultFileName = "main.cpp",
            sampleCode = """#include <iostream>
#include <vector>
#include <numeric>

int main() {
    std::cout << "=== CodeX Studio C++ / NDK Engine ===" << std::endl;
    std::vector<int> data = {10, 20, 30, 40, 50};
    int sum = std::accumulate(data.begin(), data.end(), 0);
    std::cout << "Vector Sum: " << sum << std::endl;
    std::cout << "Clang Architecture: " << __VERSION__ << std::endl;
    return 0;
}
"""
        ),
        LanguageDefinition(
            id = "javascript",
            name = "JavaScript / TypeScript",
            extensions = listOf(".js", ".jsx", ".ts", ".tsx"),
            runtime = "Node.js / Android V8 / Hermes",
            compiler = "tsc / Babel",
            buildSystem = "npm / Metro / Vite",
            packageManager = "npm / yarn / pnpm",
            runner = "Node.js / React Native Metro",
            debugger = "V8 Inspector / Chrome DevTools",
            capabilities = CapabilityMatrix(
                language = "JavaScript / TypeScript",
                compile = true,
                run = true,
                debug = true,
                hotReload = "metro-fast-refresh",
                androidBuild = "react-native-apk",
                packageManager = true,
                offlineSupport = true,
                compatibility = AndroidCompatibility.SUPPORTED,
                notes = "Runs in embedded JavaScript engine or Node.js environment. Supports React Native and npm packages."
            ),
            defaultFileName = "index.js",
            sampleCode = """// CodeX Studio - JavaScript / TypeScript
console.log("=== CodeX JavaScript Engine ===");

const fibonacci = (n) => {
  let [a, b] = [0, 1];
  const res = [];
  for (let i = 0; i < n; i++) {
    res.push(a);
    [a, b] = [b, a + b];
  }
  return res;
};

console.log("Fibonacci(8):", fibonacci(8));
console.log("Date:", new Date().toISOString());
"""
        ),
        LanguageDefinition(
            id = "rust",
            name = "Rust",
            extensions = listOf(".rs"),
            runtime = "Native Android Binary",
            compiler = "rustc",
            buildSystem = "Cargo / cargo-ndk",
            packageManager = "Cargo (crates.io)",
            runner = "Cargo Runner",
            debugger = "LLDB",
            capabilities = CapabilityMatrix(
                language = "Rust",
                compile = true,
                run = true,
                debug = true,
                hotReload = "none",
                androidBuild = "cargo-ndk-so",
                packageManager = true,
                offlineSupport = true,
                compatibility = AndroidCompatibility.REQUIRES_TOOLCHAIN,
                notes = "Requires Rust toolchain target (aarch64-linux-android / x86_64). Generates native JNI .so libraries."
            ),
            defaultFileName = "main.rs",
            sampleCode = """// CodeX Studio - Rust Runner
fn main() {
    println!("=== CodeX Studio Rust Engine ===");
    let numbers = vec![1, 2, 3, 4, 5];
    let squares: Vec<i32> = numbers.iter().map(|&x| x * x).collect();
    println!("Squared numbers: {:?}", squares);
}
"""
        ),
        LanguageDefinition(
            id = "dart",
            name = "Dart / Flutter",
            extensions = listOf(".dart"),
            runtime = "Dart VM / Flutter Engine",
            compiler = "dart2native / flutter build",
            buildSystem = "pub / flutter",
            packageManager = "pub (pub.dev)",
            runner = "Flutter Device Runner",
            debugger = "Dart DevTools",
            capabilities = CapabilityMatrix(
                language = "Dart / Flutter",
                compile = true,
                run = true,
                debug = true,
                hotReload = "flutter-hot-reload",
                androidBuild = "direct-apk",
                packageManager = true,
                offlineSupport = true,
                compatibility = AndroidCompatibility.PARTIALLY_SUPPORTED,
                notes = "Compiles Dart AOT and Flutter APKs. ARM64 Flutter engine requires toolchain bundle."
            ),
            defaultFileName = "main.dart",
            sampleCode = """// CodeX Studio - Dart / Flutter
void main() {
  print("=== CodeX Studio Dart Engine ===");
  final items = ["Android", "Flutter", "Monaco", "Kotlin"];
  items.forEach((item) => print("• ${'$'}item"));
}
"""
        ),
        LanguageDefinition(
            id = "go",
            name = "Go",
            extensions = listOf(".go"),
            runtime = "Go Runtime",
            compiler = "go build / gomobile",
            buildSystem = "go modules",
            packageManager = "go get",
            runner = "Go Runner",
            debugger = "Delve",
            capabilities = CapabilityMatrix(
                language = "Go",
                compile = true,
                run = true,
                debug = false,
                hotReload = "none",
                androidBuild = "gomobile-aar",
                packageManager = true,
                offlineSupport = true,
                compatibility = AndroidCompatibility.REQUIRES_TOOLCHAIN,
                notes = "Go cross-compilation target GOOS=android GOARCH=arm64. Gomobile binds Go packages to Android AAR."
            ),
            defaultFileName = "main.go",
            sampleCode = """package main

import (
	"fmt"
	"time"
)

func main() {
	fmt.Println("=== CodeX Studio Go Engine ===")
	fmt.Printf("Current Time: %s\n", time.Now().Format(time.RFC3339))
}
"""
        ),
        LanguageDefinition(
            id = "csharp",
            name = "C# / .NET",
            extensions = listOf(".cs"),
            runtime = "CoreCLR / Mono Runtime",
            compiler = "Roslyn (csc)",
            buildSystem = "dotnet build / MSBuild",
            packageManager = "NuGet",
            runner = "dotnet run",
            debugger = "vsdbg",
            capabilities = CapabilityMatrix(
                language = "C#",
                compile = true,
                run = true,
                debug = false,
                hotReload = "dotnet-watch",
                androidBuild = "maui-apk",
                packageManager = true,
                offlineSupport = true,
                compatibility = AndroidCompatibility.REQUIRES_TOOLCHAIN,
                notes = ".NET Android and MAUI SDK. Requires CoreCLR/Mono runtime binaries for Android ABI."
            ),
            defaultFileName = "Program.cs",
            sampleCode = """using System;

namespace CodeXApp {
    class Program {
        static void Main(string[] args) {
            Console.WriteLine("=== CodeX Studio C# Engine ===");
            Console.WriteLine($"Platform: {Environment.OSVersion}");
        }
    }
}
"""
        ),
        LanguageDefinition(
            id = "lua",
            name = "Lua",
            extensions = listOf(".lua"),
            runtime = "Lua 5.4 / LuaJIT",
            compiler = "luac",
            buildSystem = "Luarocks / Solar2D",
            packageManager = "Luarocks",
            runner = "Lua Interpreter",
            debugger = "MobDebug",
            capabilities = CapabilityMatrix(
                language = "Lua",
                compile = false,
                run = true,
                debug = true,
                hotReload = "script-reload",
                androidBuild = "solar2d-apk",
                packageManager = false,
                offlineSupport = true,
                compatibility = AndroidCompatibility.SUPPORTED,
                notes = "Lightweight embedded scripting. Runs natively in embedded C/Android JNI runtime."
            ),
            defaultFileName = "main.lua",
            sampleCode = """-- CodeX Studio - Lua Script
print("=== CodeX Studio Lua Engine ===")
print("Lua Version: " .. _VERSION)

local t = {10, 20, 30, 40}
local sum = 0
for i, v in ipairs(t) do
    sum = sum + v
end
print("Array Sum: " .. sum)
"""
        ),
        LanguageDefinition(
            id = "shell",
            name = "Shell / Bash",
            extensions = listOf(".sh", ".bash"),
            runtime = "/system/bin/sh",
            compiler = null,
            buildSystem = "Make / Script",
            packageManager = "apt / pkg (Termux)",
            runner = "Android Bionic Shell",
            debugger = "bash -x",
            capabilities = CapabilityMatrix(
                language = "Shell",
                compile = false,
                run = true,
                debug = true,
                hotReload = "immediate",
                androidBuild = "none",
                packageManager = false,
                offlineSupport = true,
                compatibility = AndroidCompatibility.SUPPORTED,
                notes = "Direct native execution using Android's system shell (/system/bin/sh). Accesses device environment."
            ),
            defaultFileName = "script.sh",
            sampleCode = """#!/system/bin/sh
echo "=== CodeX Studio Shell ==="
echo "User: $(id)"
echo "Kernel: $(uname -a)"
echo "Uptime: $(uptime)"
"""
        ),
        LanguageDefinition(
            id = "html",
            name = "HTML / Web",
            extensions = listOf(".html", ".htm", ".css"),
            runtime = "Android WebView / Browser Engine",
            compiler = null,
            buildSystem = "Vite / Webpack",
            packageManager = "npm",
            runner = "In-IDE WebView Preview",
            debugger = "Chrome Remote Debugging",
            capabilities = CapabilityMatrix(
                language = "HTML / Web",
                compile = false,
                run = true,
                debug = true,
                hotReload = "live-reload",
                androidBuild = "capacitor-apk",
                packageManager = true,
                offlineSupport = true,
                compatibility = AndroidCompatibility.SUPPORTED,
                notes = "Instant live rendering in WebView pane with DOM inspection and CSS editing."
            ),
            defaultFileName = "index.html",
            sampleCode = """<!DOCTYPE html>
<html>
<head>
  <style>
    body { font-family: sans-serif; background: #121212; color: #fff; padding: 20px; }
    h1 { color: #58a6ff; }
  </style>
</head>
<body>
  <h1>CodeX Web View</h1>
  <p>Live preview running directly inside Android WebView.</p>
</body>
</html>
"""
        )
    )

    fun getAll(): List<LanguageDefinition> = languages

    fun getById(id: String): LanguageDefinition? {
        return languages.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }

    fun detectByExtension(fileName: String): LanguageDefinition {
        val ext = "." + fileName.substringAfterLast('.', "")
        return languages.firstOrNull { lang ->
            lang.extensions.any { it.equals(ext, ignoreCase = true) }
        } ?: getById("python")!!
    }

    fun getMonacoLanguageId(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "py" -> "python"
            "kt", "kts" -> "kotlin"
            "java" -> "java"
            "cpp", "c", "h", "hpp", "cc" -> "cpp"
            "js", "jsx" -> "javascript"
            "ts", "tsx" -> "typescript"
            "rs" -> "rust"
            "dart" -> "dart"
            "go" -> "go"
            "cs" -> "csharp"
            "lua" -> "lua"
            "sh", "bash" -> "shell"
            "html", "htm" -> "html"
            "css" -> "css"
            "json" -> "json"
            "xml" -> "xml"
            "yaml", "yml" -> "yaml"
            "md" -> "markdown"
            "sql" -> "sql"
            else -> "plaintext"
        }
    }
}
