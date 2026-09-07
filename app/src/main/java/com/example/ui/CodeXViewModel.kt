package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.android.AndroidBuildEngine
import com.example.core.android.LogcatEntry
import com.example.core.android.LogcatManager
import com.example.core.build.BuildOrchestrator
import com.example.core.db.AppDatabase
import com.example.core.db.CodeXRepository
import com.example.core.db.ProjectEntity
import com.example.core.doctor.CodeXDoctor
import com.example.core.extensions.ExtensionManager
import com.example.core.fs.FileNode
import com.example.core.fs.WorkspaceManager
import com.example.core.git.GitManager
import com.example.core.languages.LanguageRegistry
import com.example.core.model.*
import com.example.core.packages.PackageManagerService
import com.example.core.process.ProcessManager
import com.example.core.toolchains.ToolchainManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

enum class SidebarTab {
    EXPLORER,
    SEARCH,
    SCM,
    RUN_DEBUG,
    EXTENSIONS,
    PACKAGES,
    TOOLCHAINS,
    DOCTOR,
    SETTINGS
}

enum class BottomTab {
    TERMINAL,
    PROBLEMS,
    OUTPUT,
    DEBUG,
    BUILD,
    LOGCAT
}

data class SearchMatch(
    val file: File,
    val lineNumber: Int,
    val lineText: String
)

class CodeXViewModel(application: Application) : AndroidViewModel(application) {

    val workspaceManager = WorkspaceManager(application)
    val processManager = ProcessManager(application)
    val buildOrchestrator = BuildOrchestrator(application, processManager)
    val androidBuildEngine = AndroidBuildEngine(application, processManager)
    val logcatManager = LogcatManager()
    val gitManager = GitManager(processManager)
    val doctor = CodeXDoctor(application)
    val toolchainManager = ToolchainManager(application)
    val extensionManager = ExtensionManager()
    val packageManagerService = PackageManagerService()

    private val db = AppDatabase.getInstance(application)
    val repository = CodeXRepository(db.codeXDao())

    // UI States
    private val _currentProject = MutableStateFlow<File?>(null)
    val currentProject: StateFlow<File?> = _currentProject.asStateFlow()

    private val _expandedPaths = MutableStateFlow<Set<String>>(emptySet())
    val expandedPaths: StateFlow<Set<String>> = _expandedPaths.asStateFlow()

    private val _fileTree = MutableStateFlow<FileNode?>(null)
    val fileTree: StateFlow<FileNode?> = _fileTree.asStateFlow()

    private val _openTabs = MutableStateFlow<List<File>>(emptyList())
    val openTabs: StateFlow<List<File>> = _openTabs.asStateFlow()

    private val _activeTab = MutableStateFlow<File?>(null)
    val activeTab: StateFlow<File?> = _activeTab.asStateFlow()

    private val _fileContents = MutableStateFlow<Map<String, String>>(emptyMap())
    val fileContents: StateFlow<Map<String, String>> = _fileContents.asStateFlow()

    private val _unsavedFiles = MutableStateFlow<Set<String>>(emptySet())
    val unsavedFiles: StateFlow<Set<String>> = _unsavedFiles.asStateFlow()

    private val _cursorLine = MutableStateFlow(1)
    val cursorLine: StateFlow<Int> = _cursorLine.asStateFlow()

    private val _cursorColumn = MutableStateFlow(1)
    val cursorColumn: StateFlow<Int> = _cursorColumn.asStateFlow()

    private val _activeSidebarTab = MutableStateFlow(SidebarTab.EXPLORER)
    val activeSidebarTab: StateFlow<SidebarTab> = _activeSidebarTab.asStateFlow()

    private val _isSidebarOpen = MutableStateFlow(true)
    val isSidebarOpen: StateFlow<Boolean> = _isSidebarOpen.asStateFlow()

    private val _activeBottomTab = MutableStateFlow(BottomTab.TERMINAL)
    val activeBottomTab: StateFlow<BottomTab> = _activeBottomTab.asStateFlow()

    private val _isBottomPanelOpen = MutableStateFlow(false)
    val isBottomPanelOpen: StateFlow<Boolean> = _isBottomPanelOpen.asStateFlow()

    private val _isCommandPaletteOpen = MutableStateFlow(false)
    val isCommandPaletteOpen: StateFlow<Boolean> = _isCommandPaletteOpen.asStateFlow()

    private val _buildResult = MutableStateFlow<BuildResult?>(null)
    val buildResult: StateFlow<BuildResult?> = _buildResult.asStateFlow()

    private val _isBuilding = MutableStateFlow(false)
    val isBuilding: StateFlow<Boolean> = _isBuilding.asStateFlow()

    private val _gitStatus = MutableStateFlow(GitStatus(branch = "main"))
    val gitStatus: StateFlow<GitStatus> = _gitStatus.asStateFlow()

    private val _doctorChecks = MutableStateFlow<List<DoctorCheck>>(emptyList())
    val doctorChecks: StateFlow<List<DoctorCheck>> = _doctorChecks.asStateFlow()

    private val _isDoctorRunning = MutableStateFlow(false)
    val isDoctorRunning: StateFlow<Boolean> = _isDoctorRunning.asStateFlow()

    private val _toolchains = MutableStateFlow<List<ToolchainInfo>>(emptyList())
    val toolchains: StateFlow<List<ToolchainInfo>> = _toolchains.asStateFlow()

    val extensions: StateFlow<List<ExtensionItem>> = extensionManager.extensions

    private val _packages = MutableStateFlow<List<PackageItem>>(emptyList())
    val packages: StateFlow<List<PackageItem>> = _packages.asStateFlow()

    private val _logcatEntries = MutableStateFlow<List<LogcatEntry>>(emptyList())
    val logcatEntries: StateFlow<List<LogcatEntry>> = _logcatEntries.asStateFlow()

    private val _logcatFilter = MutableStateFlow("ALL")
    val logcatFilter: StateFlow<String> = _logcatFilter.asStateFlow()

    private val _logcatSearch = MutableStateFlow("")
    val logcatSearch: StateFlow<String> = _logcatSearch.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchMatch>>(emptyList())
    val searchResults: StateFlow<List<SearchMatch>> = _searchResults.asStateFlow()

    // Settings
    private val _theme = MutableStateFlow("codex-dark")
    val theme: StateFlow<String> = _theme.asStateFlow()

    private val _fontSize = MutableStateFlow(14)
    val fontSize: StateFlow<Int> = _fontSize.asStateFlow()

    private val _wordWrap = MutableStateFlow(true)
    val wordWrap: StateFlow<Boolean> = _wordWrap.asStateFlow()

    private val _minimap = MutableStateFlow(true)
    val minimap: StateFlow<Boolean> = _minimap.asStateFlow()

    // Dialogs
    var isNewProjectDialogOpen by mutableStateOf(false)
    var isNewFileDialogOpen by mutableStateOf(false)
    var isNewFolderDialogOpen by mutableStateOf(false)
    var isRenameDialogOpen by mutableStateOf(false)
    var targetFileForAction: File? by mutableStateOf(null)

    init {
        viewModelScope.launch {
            _toolchains.value = toolchainManager.getToolchains()
            val initialProject = workspaceManager.ensureInitialWorkspace()
            openProject(initialProject)
            runDoctor()
        }
    }

    fun openProject(projectDir: File) {
        _currentProject.value = projectDir
        _expandedPaths.value = setOf(projectDir.absolutePath)
        refreshFileTree()
        refreshGitStatus()
        refreshPackages()

        // Open default entry file
        val entry = findDefaultFile(projectDir)
        if (entry != null && entry.exists()) {
            openFile(entry)
        }

        viewModelScope.launch {
            repository.saveProject(
                ProjectEntity(
                    id = projectDir.name,
                    name = projectDir.name,
                    path = projectDir.absolutePath,
                    language = LanguageRegistry.detectByExtension(entry?.name ?: "").name
                )
            )
        }
    }

    private fun findDefaultFile(dir: File): File? {
        val candidates = listOf(
            "main.py", "MainActivity.kt", "Main.java", "main.cpp",
            "index.js", "src/main.rs", "main.dart", "main.go", "index.html", "script.sh"
        )
        for (rel in candidates) {
            val f = File(dir, rel)
            if (f.exists()) return f
        }
        return dir.listFiles()?.firstOrNull { !it.isDirectory }
    }

    fun refreshFileTree() {
        val proj = _currentProject.value ?: return
        _fileTree.value = workspaceManager.buildFileTree(proj, _expandedPaths.value)
    }

    fun toggleDirectoryExpanded(path: String) {
        val current = _expandedPaths.value.toMutableSet()
        if (current.contains(path)) {
            current.remove(path)
        } else {
            current.add(path)
        }
        _expandedPaths.value = current
        refreshFileTree()
    }

    fun openFile(file: File) {
        if (!file.exists() || file.isDirectory) return
        val currentTabs = _openTabs.value.toMutableList()
        if (!currentTabs.any { it.absolutePath == file.absolutePath }) {
            currentTabs.add(file)
            _openTabs.value = currentTabs
        }
        _activeTab.value = file

        if (!_fileContents.value.containsKey(file.absolutePath)) {
            val content = workspaceManager.readFile(file)
            _fileContents.value = _fileContents.value + (file.absolutePath to content)
        }
    }

    fun closeTab(file: File) {
        val currentTabs = _openTabs.value.toMutableList()
        val index = currentTabs.indexOfFirst { it.absolutePath == file.absolutePath }
        if (index != -1) {
            currentTabs.removeAt(index)
            _openTabs.value = currentTabs
            if (_activeTab.value?.absolutePath == file.absolutePath) {
                _activeTab.value = currentTabs.getOrNull(index.coerceAtMost(currentTabs.size - 1))
            }
        }
    }

    fun updateContent(newContent: String) {
        val active = _activeTab.value ?: return
        val existing = _fileContents.value[active.absolutePath]
        if (existing != newContent) {
            _fileContents.value = _fileContents.value + (active.absolutePath to newContent)
            _unsavedFiles.value = _unsavedFiles.value + active.absolutePath
        }
    }

    fun saveActiveFile() {
        val active = _activeTab.value ?: return
        val content = _fileContents.value[active.absolutePath] ?: return
        workspaceManager.writeFile(active, content)
        _unsavedFiles.value = _unsavedFiles.value - active.absolutePath
        refreshGitStatus()
    }

    fun saveAll() {
        _unsavedFiles.value.forEach { path ->
            val f = File(path)
            val content = _fileContents.value[path]
            if (content != null) {
                workspaceManager.writeFile(f, content)
            }
        }
        _unsavedFiles.value = emptySet()
        refreshGitStatus()
    }

    fun setCursor(line: Int, col: Int) {
        _cursorLine.value = line
        _cursorColumn.value = col
    }

    fun selectSidebarTab(tab: SidebarTab) {
        if (_activeSidebarTab.value == tab && _isSidebarOpen.value) {
            _isSidebarOpen.value = false
        } else {
            _activeSidebarTab.value = tab
            _isSidebarOpen.value = true
        }
    }

    fun toggleSidebar() {
        _isSidebarOpen.value = !_isSidebarOpen.value
    }

    fun selectBottomTab(tab: BottomTab) {
        _activeBottomTab.value = tab
        _isBottomPanelOpen.value = true
    }

    fun toggleBottomPanel() {
        _isBottomPanelOpen.value = !_isBottomPanelOpen.value
    }

    fun toggleCommandPalette() {
        _isCommandPaletteOpen.value = !_isCommandPaletteOpen.value
    }

    // Run & Build
    fun runActiveFile() {
        val active = _activeTab.value ?: return
        val project = _currentProject.value ?: active.parentFile ?: return
        saveActiveFile()

        viewModelScope.launch {
            _isBuilding.value = true
            selectBottomTab(BottomTab.BUILD)
            val result = buildOrchestrator.runFile(active, project)
            _buildResult.value = result
            _isBuilding.value = false
        }
    }

    fun buildAndroidApk(isRelease: Boolean = false) {
        val project = _currentProject.value ?: return
        saveAll()

        viewModelScope.launch {
            _isBuilding.value = true
            selectBottomTab(BottomTab.BUILD)
            val result = androidBuildEngine.buildApk(project, isRelease)
            _buildResult.value = result
            _isBuilding.value = false
        }
    }

    fun installGeneratedApk() {
        val result = _buildResult.value ?: return
        val path = result.artifactPath ?: return
        androidBuildEngine.installApk(File(path))
    }

    // Terminal
    fun sendTerminalCommand(cmd: String) {
        val activeSession = processManager.activeSessionId.value
        val dir = _currentProject.value ?: getApplication<Application>().filesDir
        processManager.sendCommand(activeSession, cmd, dir)
        viewModelScope.launch {
            repository.recordCommand(cmd)
        }
    }

    // Git
    fun refreshGitStatus() {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            _gitStatus.value = gitManager.getStatus(proj)
        }
    }

    fun gitCommit(message: String) {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            gitManager.commit(proj, message)
            refreshGitStatus()
        }
    }

    fun gitInit() {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            gitManager.initRepo(proj)
            refreshGitStatus()
        }
    }

    // Doctor
    fun runDoctor() {
        viewModelScope.launch {
            _isDoctorRunning.value = true
            _doctorChecks.value = doctor.runDiagnostics()
            _isDoctorRunning.value = false
        }
    }

    // Packages
    fun refreshPackages() {
        val proj = _currentProject.value ?: return
        val eco = packageManagerService.detectEcosystem(proj)
        _packages.value = packageManagerService.getPopularPackages(eco)
    }

    fun installPackage(pkg: PackageItem) {
        val proj = _currentProject.value ?: return
        if (packageManagerService.installPackage(proj, pkg)) {
            refreshPackages()
            refreshFileTree()
            // reload active file if affected
            _activeTab.value?.let { active ->
                _fileContents.value = _fileContents.value + (active.absolutePath to workspaceManager.readFile(active))
            }
        }
    }

    // Extensions
    fun toggleExtension(id: String) {
        extensionManager.toggleExtension(id)
    }

    // Logcat
    fun refreshLogcat() {
        viewModelScope.launch {
            _logcatEntries.value = logcatManager.readLogs(_logcatFilter.value, _logcatSearch.value)
        }
    }

    fun setLogcatFilter(level: String) {
        _logcatFilter.value = level
        refreshLogcat()
    }

    fun setLogcatSearch(query: String) {
        _logcatSearch.value = query
        refreshLogcat()
    }

    fun clearLogcat() {
        viewModelScope.launch {
            logcatManager.clearLogs()
            _logcatEntries.value = emptyList()
        }
    }

    // Search in workspace
    fun searchInWorkspace(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        val proj = _currentProject.value ?: return
        val results = mutableListOf<SearchMatch>()
        proj.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.forEach { file ->
            try {
                file.useLines { lines ->
                    lines.forEachIndexed { index, line ->
                        if (line.contains(query, ignoreCase = true)) {
                            results.add(SearchMatch(file, index + 1, line.trim()))
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore binary files
            }
        }
        _searchResults.value = results.take(100)
    }

    // Create New Project from Template
    fun createProjectFromTemplate(templateId: String, name: String) {
        val newDir = workspaceManager.createFromTemplate(templateId, name)
        openProject(newDir)
    }

    // Create New File / Folder
    fun createNewFile(name: String, parentDir: File? = null) {
        val parent = parentDir ?: _currentProject.value ?: return
        val file = workspaceManager.createFile(parent, name)
        refreshFileTree()
        openFile(file)
    }

    fun createNewFolder(name: String, parentDir: File? = null) {
        val parent = parentDir ?: _currentProject.value ?: return
        workspaceManager.createDirectory(parent, name)
        refreshFileTree()
    }

    fun deleteFile(file: File) {
        closeTab(file)
        workspaceManager.delete(file)
        refreshFileTree()
        refreshGitStatus()
    }

    fun renameFile(file: File, newName: String) {
        val target = workspaceManager.rename(file, newName)
        closeTab(file)
        refreshFileTree()
        openFile(target)
    }

    // Settings
    fun setTheme(newTheme: String) {
        _theme.value = newTheme
    }

    fun setFontSize(size: Int) {
        _fontSize.value = size
    }

    fun toggleWordWrap() {
        _wordWrap.value = !_wordWrap.value
    }

    fun toggleMinimap() {
        _minimap.value = !_minimap.value
    }
}
