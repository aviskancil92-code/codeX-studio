package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.android.AndroidBuildEngine
import com.example.core.android.LogcatManager
import com.example.core.build.BuildOrchestrator
import com.example.core.db.AppDatabase
import com.example.core.db.ProjectEntity
import com.example.core.db.SettingsEntity
import com.example.core.doctor.CodeXDoctor
import com.example.core.extensions.ExtensionManager
import com.example.core.fs.WorkspaceManager
import com.example.core.git.GitManager
import com.example.core.languages.LanguageRegistry
import com.example.core.model.DiagnosticSeverity
import com.example.core.model.DoctorStatus
import com.example.core.model.PackageItem
import com.example.core.packages.PackageManagerService
import com.example.core.process.ProcessManager
import com.example.core.toolchains.ToolchainManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.zip.ZipFile

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CodeXFeatureAuditTest {

    private lateinit var context: Context
    private lateinit var workspaceManager: WorkspaceManager
    private lateinit var processManager: ProcessManager
    private lateinit var packageManagerService: PackageManagerService
    private lateinit var extensionManager: ExtensionManager
    private lateinit var toolchainManager: ToolchainManager
    private lateinit var logcatManager: LogcatManager
    private lateinit var doctor: CodeXDoctor

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        workspaceManager = WorkspaceManager(context)
        processManager = ProcessManager(context)
        packageManagerService = PackageManagerService()
        extensionManager = ExtensionManager()
        toolchainManager = ToolchainManager(context)
        logcatManager = LogcatManager()
        doctor = CodeXDoctor(context)
    }

    // ==========================================
    // 1. WORKSPACE & FILE SYSTEM AUDIT
    // ==========================================
    @Test
    fun `test workspace project template creation and file tree`() {
        val proj = workspaceManager.createFromTemplate("python_cli", "AuditTestPyProject")
        assertTrue(proj.exists())
        assertTrue(File(proj, "main.py").exists())
        assertTrue(File(proj, "requirements.txt").exists())

        // File operations
        val subDir = workspaceManager.createDirectory(proj, "src")
        assertTrue(subDir.exists() && subDir.isDirectory)

        val newFile = workspaceManager.createFile(subDir, "utils.py")
        assertTrue(newFile.exists() && newFile.isFile)

        workspaceManager.writeFile(newFile, "def add(a, b):\n    return a + b\n")
        val content = workspaceManager.readFile(newFile)
        assertTrue(content.contains("def add(a, b):"))

        // Build file tree with expanded root path
        val tree = workspaceManager.buildFileTree(proj, setOf(proj.absolutePath))
        assertEquals("AuditTestPyProject", tree.name)
        assertTrue(tree.children.any { it.name == "main.py" })
        assertTrue(tree.children.any { it.name == "src" })

        // Rename file
        val renamed = workspaceManager.rename(newFile, "helpers.py")
        assertFalse(newFile.exists())
        assertTrue(renamed.exists())
        assertEquals("helpers.py", renamed.name)

        // Delete file
        val deleted = workspaceManager.delete(renamed)
        assertTrue(deleted)
        assertFalse(renamed.exists())
    }

    // ==========================================
    // 2. MULTI-LANGUAGE REGISTRY AUDIT (12 LANGUAGES)
    // ==========================================
    @Test
    fun `test multi-language detection and capabilities for all 12 supported languages`() {
        val testFiles = mapOf(
            "app.py" to "Python",
            "Main.kt" to "Kotlin",
            "Program.java" to "Java",
            "native.cpp" to "C / C++",
            "native.c" to "C / C++",
            "index.js" to "JavaScript / TypeScript",
            "types.ts" to "JavaScript / TypeScript",
            "main.rs" to "Rust",
            "main.dart" to "Dart / Flutter",
            "server.go" to "Go",
            "App.cs" to "C# / .NET",
            "script.lua" to "Lua",
            "run.sh" to "Shell / Bash",
            "index.html" to "HTML / Web"
        )

        testFiles.forEach { (filename, expectedName) ->
            val lang = LanguageRegistry.detectByExtension(filename)
            assertEquals("Testing $filename", expectedName, lang.name)
            assertTrue("Sample code must not be blank for ${lang.name}", lang.sampleCode.isNotBlank())
            assertTrue("Monaco language identifier must be present for ${lang.name}", lang.monacoLanguage.isNotBlank())
            assertNotNull(lang.capabilities)
        }

        // Test fallback for unknown extension defaults to python
        val unknown = LanguageRegistry.detectByExtension("data.xyz123")
        assertEquals("Python", unknown.name)
        assertEquals("python", unknown.monacoLanguage)
    }

    // ==========================================
    // 3. PROCESS MANAGER & MULTI-SESSION TERMINAL
    // ==========================================
    @Test
    fun `test terminal session lifecycle management`() {
        assertEquals(1, processManager.sessions.value.size)
        val initialId = processManager.activeSessionId.value

        // Create new terminal session
        val secondSessionId = processManager.createSession("Terminal 2")
        assertEquals(2, processManager.sessions.value.size)
        assertEquals(secondSessionId, processManager.activeSessionId.value)

        // Switch back to initial
        processManager.selectSession(initialId)
        assertEquals(initialId, processManager.activeSessionId.value)

        // Close second session
        processManager.closeSession(secondSessionId)
        assertEquals(1, processManager.sessions.value.size)
        assertEquals(initialId, processManager.activeSessionId.value)
    }

    // ==========================================
    // 4. ANDROID APK BUILD ENGINE PIPELINE
    // ==========================================
    @Test
    fun `test android build engine fails gracefully when manifest missing`() = runBlocking {
        val buildEngine = AndroidBuildEngine(context, processManager)
        val dummyDir = File(context.cacheDir, "DummyNoManifestProj").apply { mkdirs() }
        val result = buildEngine.buildApk(dummyDir, isRelease = false)
        assertFalse("Build must fail if AndroidManifest.xml is missing", result.isSuccess)
        assertTrue(result.stderr.contains("Missing AndroidManifest.xml"))
    }

    @Test
    fun `test android build engine packages valid APK archive`() = runBlocking {
        val buildEngine = AndroidBuildEngine(context, processManager)
        val projDir = workspaceManager.createFromTemplate("android_kotlin", "AndroidBuildTest")
        val result = buildEngine.buildApk(projDir, isRelease = false)

        assertTrue("Build must succeed for template project: ${result.stderr}", result.isSuccess)
        assertNotNull(result.artifactPath)
        val apkFile = File(result.artifactPath!!)
        assertTrue("APK file must exist", apkFile.exists())
        assertTrue("APK size must be greater than 0", apkFile.length() > 0)

        // Verify APK internal ZIP structure contains manifest and classes.dex
        ZipFile(apkFile).use { zip ->
            assertNotNull("APK must contain AndroidManifest.xml", zip.getEntry("AndroidManifest.xml"))
            assertNotNull("APK must contain classes.dex", zip.getEntry("classes.dex"))
            assertNotNull("APK must contain signature file", zip.getEntry("META-INF/CERT.SF"))
        }
    }

    // ==========================================
    // 5. GIT VERSION CONTROL AUDIT
    // ==========================================
    @Test
    fun `test git manager init status and commit workflow`() = runBlocking {
        val gitManager = GitManager(processManager)
        val projDir = workspaceManager.createFromTemplate("python_cli", "GitAuditProj")

        val initialStatus = gitManager.getStatus(projDir)
        assertFalse("Initially should not be a repo before git init", initialStatus.isRepo)

        // Initialize git
        val initSuccess = gitManager.initRepo(projDir)
        assertTrue("Git init should succeed", initSuccess)

        val repoStatus = gitManager.getStatus(projDir)
        assertTrue("Project should now be identified as a git repo", repoStatus.isRepo)
        assertEquals("main", repoStatus.branch)

        // Create a commit
        val commitMsg = "feat: initial test commit"
        val commitResult = gitManager.commit(projDir, commitMsg)
        assertTrue(commitResult.contains("Committed") || commitResult.isNotBlank())
    }

    // ==========================================
    // 6. PACKAGE MANAGER ECOSYSTEM AUDIT
    // ==========================================
    @Test
    fun `test package manager ecosystem detection and dependency injection`() {
        val tempDir = File(context.cacheDir, "PkgAuditDir").apply { mkdirs() }

        // Test PIP ecosystem
        File(tempDir, "requirements.txt").writeText("requests>=2.31.0\n")
        assertEquals("pip", packageManagerService.detectEcosystem(tempDir))

        val numpyPkg = PackageItem("numpy", "1.26.4", "NumPy array computing", "pip", false, "1.26.4")
        val pipInstalled = packageManagerService.installPackage(tempDir, numpyPkg)
        assertTrue(pipInstalled)
        assertTrue(File(tempDir, "requirements.txt").readText().contains("numpy>=1.26.4"))

        // Test NPM ecosystem
        File(tempDir, "requirements.txt").delete()
        File(tempDir, "package.json").writeText("{\n  \"dependencies\": {\n  }\n}")
        assertEquals("npm", packageManagerService.detectEcosystem(tempDir))

        val expressPkg = PackageItem("express", "4.19.2", "Express web framework", "npm", false, "4.19.2")
        val npmInstalled = packageManagerService.installPackage(tempDir, expressPkg)
        assertTrue(npmInstalled)
        assertTrue(File(tempDir, "package.json").readText().contains("\"express\": \"^4.19.2\""))

        // Test Cargo ecosystem
        File(tempDir, "package.json").delete()
        File(tempDir, "Cargo.toml").writeText("[dependencies]\n")
        assertEquals("Cargo", packageManagerService.detectEcosystem(tempDir))

        val serdePkg = PackageItem("serde", "1.0.203", "Serde serialization", "Cargo", false, "1.0.203")
        val cargoInstalled = packageManagerService.installPackage(tempDir, serdePkg)
        assertTrue(cargoInstalled)
        assertTrue(File(tempDir, "Cargo.toml").readText().contains("serde = \"1.0.203\""))
    }

    // ==========================================
    // 7. TOOLCHAIN & RUNTIME AUDIT
    // ==========================================
    @Test
    fun `test toolchain manager inventories toolchains and detects architecture`() = runBlocking {
        val toolchains = toolchainManager.getToolchains()
        assertTrue(toolchains.size >= 8)

        val names = toolchains.map { it.name }
        assertTrue(names.any { it.contains("Python") })
        assertTrue(names.any { it.contains("Node") })
        assertTrue(names.any { it.contains("NDK") || it.contains("Clang") })
        assertTrue(names.any { it.contains("Rust") })
        assertTrue(names.any { it.contains("JDK") || it.contains("Java") })

        val verifyMsg = toolchainManager.verifyToolchain("android_sdk")
        assertTrue(verifyMsg.contains("Verified"))
    }

    // ==========================================
    // 8. CODEX DOCTOR SYSTEM DIAGNOSTICS AUDIT
    // ==========================================
    @Test
    fun `test codex doctor evaluates system health`() = runBlocking {
        val checks = doctor.runDiagnostics()
        assertTrue("Doctor must return at least 5 diagnostic checks", checks.size >= 5)

        val osCheck = checks.firstOrNull { it.title.contains("Android OS") }
        assertNotNull(osCheck)
        assertEquals(DoctorStatus.PASS, osCheck?.status)

        val ramCheck = checks.firstOrNull { it.title.contains("RAM") }
        assertNotNull(ramCheck)

        val storageCheck = checks.firstOrNull { it.title.contains("Storage") }
        assertNotNull(storageCheck)
    }

    // ==========================================
    // 9. EXTENSIONS MANAGER AUDIT
    // ==========================================
    @Test
    fun `test extension manager list and toggle status`() {
        val extensions = extensionManager.extensions.value
        assertTrue(extensions.isNotEmpty())

        val pyExt = extensions.first { it.id == "ms-python.python" }
        val originalState = pyExt.isEnabled

        extensionManager.toggleExtension(pyExt.id)
        val toggledExt = extensionManager.extensions.value.first { it.id == pyExt.id }
        assertEquals(!originalState, toggledExt.isEnabled)

        // Toggle back
        extensionManager.toggleExtension(pyExt.id)
        val restoredExt = extensionManager.extensions.value.first { it.id == pyExt.id }
        assertEquals(originalState, restoredExt.isEnabled)
    }

    // ==========================================
    // 10. ROOM DATABASE PERSISTENCE AUDIT
    // ==========================================
    @Test
    fun `test room database persistence for projects and settings`() = runBlocking {
        val inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = inMemoryDb.codeXDao()

        // Test project entity persistence
        val project = ProjectEntity(
            id = "proj-123",
            name = "TestProject",
            path = "/data/user/0/com.example/files/TestProject",
            language = "Kotlin",
            isFavorite = true
        )
        dao.insertProject(project)

        val retrieved = dao.getProjectById("proj-123")
        assertNotNull(retrieved)
        assertEquals("TestProject", retrieved?.name)
        assertEquals("Kotlin", retrieved?.language)
        assertTrue(retrieved?.isFavorite == true)

        // Test settings persistence
        dao.setSetting(SettingsEntity("editor.fontSize", "16"))
        val fontSizeSetting = dao.getSetting("editor.fontSize")
        assertEquals("16", fontSizeSetting)

        inMemoryDb.close()
    }
}
