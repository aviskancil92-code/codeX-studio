package com.example.core.extensions

import com.example.core.model.AndroidCompatibility
import com.example.core.model.ExtensionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExtensionManager {

    private val _extensions = MutableStateFlow<List<ExtensionItem>>(
        listOf(
            ExtensionItem(
                id = "ms-python.python",
                name = "Python Language Pack",
                version = "2024.4.1",
                publisher = "Microsoft",
                description = "IntelliSense, linting, code formatting, debugging, and pip package management for Python.",
                isInstalled = true,
                isEnabled = true,
                category = "Programming Languages",
                compatibility = AndroidCompatibility.SUPPORTED,
                compatibilityReason = "Pure LSP and language configuration compatible with Android runner.",
                tags = listOf("python", "linting", "intellisense")
            ),
            ExtensionItem(
                id = "fwcd.kotlin",
                name = "Kotlin Language Server",
                version = "0.2.35",
                publisher = "fwcd",
                description = "Kotlin code completion, diagnostics, symbol outline, and Gradle build integration.",
                isInstalled = true,
                isEnabled = true,
                category = "Programming Languages",
                compatibility = AndroidCompatibility.SUPPORTED,
                compatibilityReason = "Java bytecode based language server runs directly on Android ART runtime.",
                tags = listOf("kotlin", "jvm", "gradle")
            ),
            ExtensionItem(
                id = "ms-vscode.cpptools",
                name = "C/C++ & NDK Clang Tools",
                version = "1.19.6",
                publisher = "Microsoft",
                description = "C/C++ IntelliSense, header indexing, and LLDB native debugging architecture.",
                isInstalled = true,
                isEnabled = true,
                category = "Programming Languages",
                compatibility = AndroidCompatibility.PARTIALLY_SUPPORTED,
                compatibilityReason = "Uses Clang compiler headers. Full LLDB debugging requires Android NDK r26d.",
                tags = listOf("c", "cpp", "ndk", "clang")
            ),
            ExtensionItem(
                id = "rust-lang.rust-analyzer",
                name = "rust-analyzer",
                version = "0.3.1890",
                publisher = "The Rust Programming Language",
                description = "Modular compiler front-end for the Rust language with type hints and cargo integration.",
                isInstalled = false,
                isEnabled = false,
                category = "Programming Languages",
                compatibility = AndroidCompatibility.REQUIRES_TOOLCHAIN,
                compatibilityReason = "Requires aarch64-linux-android rust-analyzer binary installed via Toolchain Manager.",
                tags = listOf("rust", "cargo")
            ),
            ExtensionItem(
                id = "dart-code.flutter",
                name = "Flutter & Dart Support",
                version = "3.86.0",
                publisher = "Dart Code",
                description = "Flutter development tools, widget inspector, and hot reload workflow.",
                isInstalled = false,
                isEnabled = false,
                category = "Mobile Development",
                compatibility = AndroidCompatibility.PARTIALLY_SUPPORTED,
                compatibilityReason = "Supports Dart syntax and widget structure. Full device deployment requires Flutter SDK.",
                tags = listOf("flutter", "dart", "mobile")
            ),
            ExtensionItem(
                id = "dbaeumer.vscode-eslint",
                name = "ESLint & Prettier Formatter",
                version = "2.4.4",
                publisher = "Microsoft",
                description = "Integrates ESLint and Prettier into Monaco Editor for JavaScript and TypeScript.",
                isInstalled = true,
                isEnabled = true,
                category = "Linters",
                compatibility = AndroidCompatibility.SUPPORTED,
                compatibilityReason = "WASM and JS bundle runs in embedded Monaco runtime.",
                tags = listOf("javascript", "typescript", "lint")
            ),
            ExtensionItem(
                id = "eamodio.gitlens",
                name = "GitLens Mobile",
                version = "14.2.0",
                publisher = "GitKraken",
                description = "Visualize code authorship, Git blame annotations, branch histories, and repository diffs.",
                isInstalled = true,
                isEnabled = true,
                category = "SCM Providers",
                compatibility = AndroidCompatibility.SUPPORTED,
                compatibilityReason = "Integrates with CodeX Studio GitManager and local repository logs.",
                tags = listOf("git", "scm", "history")
            ),
            ExtensionItem(
                id = "dracula-theme.theme-dracula",
                name = "Dracula Official Theme",
                version = "2.24.3",
                publisher = "Dracula Theme",
                description = "A dark theme for Monaco Editor and CodeX Studio UI with distinctive gothic contrasts.",
                isInstalled = true,
                isEnabled = true,
                category = "Themes",
                compatibility = AndroidCompatibility.SUPPORTED,
                compatibilityReason = "CSS theme token mapping works natively in Monaco WebView.",
                tags = listOf("theme", "dracula", "dark")
            ),
            ExtensionItem(
                id = "pkief.material-icon-theme",
                name = "Material Icon Theme",
                version = "4.34.0",
                publisher = "Philipp Kief",
                description = "File and folder icons for Python, Kotlin, C++, Rust, Go, Dart, Docker, and more.",
                isInstalled = true,
                isEnabled = true,
                category = "Themes",
                compatibility = AndroidCompatibility.SUPPORTED,
                compatibilityReason = "Vector iconography fully supported in Jetpack Compose file tree.",
                tags = listOf("icons", "filetree")
            ),
            ExtensionItem(
                id = "github.copilot-desktop-binary",
                name = "Desktop Native LSP Bridge",
                version = "1.0.0",
                publisher = "ThirdParty",
                description = "Demonstration of an incompatible desktop extension requiring x86_64 Electron binaries.",
                isInstalled = false,
                isEnabled = false,
                category = "Other",
                compatibility = AndroidCompatibility.ANDROID_INCOMPATIBLE,
                compatibilityReason = "Requires desktop Electron.js node-gyp native binaries which cannot execute on Android Bionic libc.",
                tags = listOf("incompatible", "desktop")
            )
        )
    )
    val extensions: StateFlow<List<ExtensionItem>> = _extensions.asStateFlow()

    fun toggleExtension(id: String) {
        _extensions.value = _extensions.value.map { ext ->
            if (ext.id == id) {
                val newInstalled = !ext.isInstalled
                ext.copy(isInstalled = newInstalled, isEnabled = newInstalled)
            } else ext
        }
    }
}
