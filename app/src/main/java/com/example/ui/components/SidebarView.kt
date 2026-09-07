package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.fs.FileNode
import com.example.core.languages.LanguageRegistry
import com.example.core.model.*
import com.example.ui.CodeXViewModel
import com.example.ui.SidebarTab
import com.example.ui.theme.*
import java.io.File

@Composable
fun SidebarView(
    viewModel: CodeXViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.activeSidebarTab.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxHeight(),
        color = CodeXSidebarBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (activeTab) {
                SidebarTab.EXPLORER -> ExplorerPane(viewModel)
                SidebarTab.SEARCH -> SearchPane(viewModel)
                SidebarTab.SCM -> GitPane(viewModel)
                SidebarTab.RUN_DEBUG -> RunDebugPane(viewModel)
                SidebarTab.EXTENSIONS -> ExtensionsPane(viewModel)
                SidebarTab.PACKAGES -> PackagesPane(viewModel)
                SidebarTab.TOOLCHAINS -> ToolchainsPane(viewModel)
                SidebarTab.DOCTOR -> DoctorPane(viewModel)
                SidebarTab.SETTINGS -> SettingsPane(viewModel)
            }
        }
    }
}

// 1. EXPLORER PANE
@Composable
private fun ExplorerPane(viewModel: CodeXViewModel) {
    val currentProject by viewModel.currentProject.collectAsState()
    val fileTree by viewModel.fileTree.collectAsState()
    val activeFile by viewModel.activeTab.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Pane Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "EXPLORER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CodeXTextMuted,
                letterSpacing = 1.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { viewModel.isNewFileDialogOpen = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.NoteAdd, "New File", tint = CodeXTextSecondary, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = { viewModel.isNewFolderDialogOpen = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.CreateNewFolder, "New Folder", tint = CodeXTextSecondary, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = { viewModel.isNewProjectDialogOpen = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, "New Project", tint = CodeXPrimary, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = { viewModel.refreshFileTree() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Refresh, "Refresh", tint = CodeXTextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Project Root Label
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CodeXBorderSubtle.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.FolderOpen, "Project", tint = CodeXPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = currentProject?.name?.uppercase() ?: "NO WORKSPACE",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = CodeXTextPrimary
            )
        }

        // Recursive Tree List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp)
        ) {
            fileTree?.children?.let { nodes ->
                items(nodes) { node ->
                    FileTreeItem(
                        node = node,
                        depth = 0,
                        activeFilePath = activeFile?.absolutePath,
                        onFileClick = { file -> viewModel.openFile(file) },
                        onToggleDir = { path -> viewModel.toggleDirectoryExpanded(path) },
                        onDelete = { file -> viewModel.deleteFile(file) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FileTreeItem(
    node: FileNode,
    depth: Int,
    activeFilePath: String?,
    onFileClick: (File) -> Unit,
    onToggleDir: (String) -> Unit,
    onDelete: (File) -> Unit
) {
    val isSelected = node.path == activeFilePath
    var showMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isSelected) CodeXBorderSubtle else Color.Transparent)
                .clickable {
                    if (node.isDirectory) {
                        onToggleDir(node.path)
                    } else {
                        onFileClick(File(node.path))
                    }
                }
                .padding(start = (12 + depth * 14).dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (node.isDirectory) {
                Icon(
                    imageVector = if (node.isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = CodeXTextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = if (node.isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color(0xFFEBCB8B),
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(18.dp))
                FileIcon(node.extension)
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = node.name,
                fontSize = 13.sp,
                color = if (isSelected) CodeXPrimary else CodeXTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Delete action
            IconButton(
                onClick = { onDelete(File(node.path)) },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(Icons.Default.Close, "Delete", tint = CodeXTextMuted.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
            }
        }

        if (node.isDirectory && node.isExpanded) {
            node.children.forEach { child ->
                FileTreeItem(
                    node = child,
                    depth = depth + 1,
                    activeFilePath = activeFilePath,
                    onFileClick = onFileClick,
                    onToggleDir = onToggleDir,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun FileIcon(extension: String) {
    val (icon, color) = when (extension.lowercase()) {
        "py" -> Icons.Default.Code to Color(0xFF3776AB)
        "kt", "kts" -> Icons.Default.Code to Color(0xFF7F52FF)
        "java" -> Icons.Default.Code to Color(0xFFEA2D2E)
        "cpp", "c", "h", "hpp" -> Icons.Default.Code to Color(0xFF00599C)
        "js", "jsx", "ts", "tsx" -> Icons.Default.Javascript to Color(0xFFF7DF1E)
        "rs" -> Icons.Default.Code to Color(0xFFDEA584)
        "dart" -> Icons.Default.Code to Color(0xFF0175C2)
        "go" -> Icons.Default.Code to Color(0xFF00ADD8)
        "html", "htm" -> Icons.Default.Language to Color(0xFFE34F26)
        "css" -> Icons.Default.Style to Color(0xFF1572B6)
        "json" -> Icons.Default.DataArray to Color(0xFFCBCB41)
        "md" -> Icons.Default.Article to Color(0xFF58A6FF)
        "sh", "bash" -> Icons.Default.Terminal to Color(0xFF4EAA25)
        else -> Icons.Default.Description to CodeXTextSecondary
    }
    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
}

// 2. SEARCH PANE
@Composable
private fun SearchPane(viewModel: CodeXViewModel) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text("SEARCH WORKSPACE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CodeXTextMuted)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.searchInWorkspace(it) },
            placeholder = { Text("Search text across all files...", fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchInWorkspace("") }) {
                        Icon(Icons.Default.Clear, "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("${results.size} matches found", fontSize = 12.sp, color = CodeXTextSecondary)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(results) { match ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.openFile(match.file) },
                    colors = CardDefaults.cardColors(containerColor = CodeXPanelBg)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(match.file.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CodeXPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(":${match.lineNumber}", fontSize = 12.sp, color = CodeXTextMuted)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = match.lineText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CodeXTextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// 3. SCM (GIT) PANE
@Composable
private fun GitPane(viewModel: CodeXViewModel) {
    val gitStatus by viewModel.gitStatus.collectAsState()
    var commitMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SOURCE CONTROL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CodeXTextMuted)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ForkRight, null, tint = CodeXSecondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(gitStatus.branch, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CodeXSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = commitMessage,
            onValueChange = { commitMessage = it },
            placeholder = { Text("Commit message (Ctrl+Enter to commit)", fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.gitCommit(commitMessage)
                commitMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CodeXPrimary)
        ) {
            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Commit Changes", fontSize = 13.sp, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("COMMIT HISTORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CodeXTextMuted)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(gitStatus.commits) { commit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = commit.hash,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CodeXPrimary,
                        modifier = Modifier
                            .background(CodeXBorderSubtle, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = commit.message,
                        fontSize = 12.sp,
                        color = CodeXTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// 4. RUN & DEBUG PANE
@Composable
private fun RunDebugPane(viewModel: CodeXViewModel) {
    val activeFile by viewModel.activeTab.collectAsState()
    val isBuilding by viewModel.isBuilding.collectAsState()
    val lang = activeFile?.name?.let { LanguageRegistry.detectByExtension(it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text("RUN & DEBUG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CodeXTextMuted)
        Spacer(modifier = Modifier.height(12.dp))

        // Main Run Button
        Button(
            onClick = { viewModel.runActiveFile() },
            enabled = !isBuilding && activeFile != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CodeXSecondary)
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Run ${activeFile?.name ?: "File"}", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Android APK Build Button
        OutlinedButton(
            onClick = { viewModel.buildAndroidApk(false) },
            enabled = !isBuilding,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Android, null, tint = CodeXPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Build Android APK (Debug)", color = CodeXPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (lang != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CodeXPanelBg)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("EXECUTION CAPABILITIES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CodeXTextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    CapabilityRow("Language", lang.name)
                    CapabilityRow("Runtime", lang.runtime)
                    CapabilityRow("Compiler", lang.compiler ?: "Direct / Interpreter")
                    CapabilityRow("Build System", lang.buildSystem)
                    CapabilityRow("Hot Reload", lang.capabilities.hotReload)
                    CapabilityRow("Android Build", lang.capabilities.androidBuild)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(lang.capabilities.notes, fontSize = 11.sp, color = CodeXTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun CapabilityRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = CodeXTextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = CodeXTextPrimary)
    }
}

// 5. EXTENSIONS PANE
@Composable
private fun ExtensionsPane(viewModel: CodeXViewModel) {
    val extensions by viewModel.extensions.collectAsState()
    var searchExt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text("EXTENSIONS & PLUGINS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CodeXTextMuted)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchExt,
            onValueChange = { searchExt = it },
            placeholder = { Text("Search Extensions...", fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        val filtered = extensions.filter {
            it.name.contains(searchExt, ignoreCase = true) || it.description.contains(searchExt, ignoreCase = true)
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered) { ext ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CodeXPanelBg)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(ext.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CodeXTextPrimary)
                            Text(ext.version, fontSize = 11.sp, color = CodeXTextMuted)
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(ext.description, fontSize = 12.sp, color = CodeXTextSecondary, maxLines = 2)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Compatibility Badge
                            CompatibilityBadge(ext.compatibility)

                            Button(
                                onClick = { viewModel.toggleExtension(ext.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (ext.isInstalled) CodeXBorderSubtle else CodeXPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (ext.isInstalled) "Disable" else "Install",
                                    fontSize = 12.sp,
                                    color = if (ext.isInstalled) CodeXTextPrimary else Color.Black
                                )
                            }
                        }

                        if (ext.compatibility != AndroidCompatibility.SUPPORTED) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(ext.compatibilityReason, fontSize = 10.sp, color = CodeXWarning)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompatibilityBadge(compat: AndroidCompatibility) {
    val (color, text) = when (compat) {
        AndroidCompatibility.SUPPORTED -> CodeXSuccess to "SUPPORTED"
        AndroidCompatibility.PARTIALLY_SUPPORTED -> CodeXWarning to "PARTIAL"
        AndroidCompatibility.REQUIRES_TOOLCHAIN -> CodeXInfo to "REQUIRES SDK"
        AndroidCompatibility.ANDROID_INCOMPATIBLE -> CodeXError to "INCOMPATIBLE"
        AndroidCompatibility.NOT_INSTALLED -> CodeXTextMuted to "UNINSTALLED"
    }

    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

// 6. PACKAGES PANE
@Composable
private fun PackagesPane(viewModel: CodeXViewModel) {
    val currentProject by viewModel.currentProject.collectAsState()
    val packages by viewModel.packages.collectAsState()
    val eco = currentProject?.let { viewModel.packageManagerService.detectEcosystem(it) } ?: "pip"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("PACKAGE MANAGER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CodeXTextMuted)
            Text(
                text = eco.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CodeXPrimary,
                modifier = Modifier
                    .background(CodeXPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(packages) { pkg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CodeXPanelBg)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CodeXTextPrimary)
                            Text(pkg.version, fontSize = 11.sp, color = CodeXTextMuted)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pkg.description, fontSize = 12.sp, color = CodeXTextSecondary, maxLines = 2)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { viewModel.installPackage(pkg) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (pkg.isInstalled) CodeXBorderSubtle else CodeXSecondary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (pkg.isInstalled) "Added" else "Add to Project",
                                    fontSize = 12.sp,
                                    color = if (pkg.isInstalled) CodeXTextPrimary else Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 7. TOOLCHAINS PANE
@Composable
private fun ToolchainsPane(viewModel: CodeXViewModel) {
    val toolchains by viewModel.toolchains.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text("SDK & TOOLCHAIN MANAGER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CodeXTextMuted)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(toolchains) { tool ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CodeXPanelBg)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(tool.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CodeXTextPrimary)
                            Text(tool.version, fontSize = 11.sp, color = CodeXPrimary)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(tool.description, fontSize = 11.sp, color = CodeXTextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ABI: ${tool.architecture} • ${tool.sizeMb.toInt()} MB",
                                fontSize = 11.sp,
                                color = CodeXTextMuted
                            )

                            Text(
                                text = tool.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tool.isInstalled) CodeXSuccess else CodeXWarning
                            )
                        }
                    }
                }
            }
        }
    }
}

// 8. CODEX DOCTOR PANE
@Composable
private fun DoctorPane(viewModel: CodeXViewModel) {
    val checks by viewModel.doctorChecks.collectAsState()
    val isRunning by viewModel.isDoctorRunning.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("CODEX DOCTOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CodeXTextMuted)
            IconButton(
                onClick = { viewModel.runDoctor() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Refresh, "Rerun Doctor", tint = CodeXPrimary, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isRunning) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CodeXPrimary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(checks) { check ->
                val (icon, color) = when (check.status) {
                    DoctorStatus.PASS -> Icons.Default.CheckCircle to CodeXSuccess
                    DoctorStatus.WARN -> Icons.Default.Warning to CodeXWarning
                    DoctorStatus.FAIL -> Icons.Default.Cancel to CodeXError
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CodeXPanelBg)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(check.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CodeXTextPrimary)
                            Text(check.message, fontSize = 12.sp, color = color)
                            if (check.detail.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(check.detail, fontSize = 11.sp, color = CodeXTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 9. SETTINGS PANE
@Composable
private fun SettingsPane(viewModel: CodeXViewModel) {
    val theme by viewModel.theme.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val wordWrap by viewModel.wordWrap.collectAsState()
    val minimap by viewModel.minimap.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text("PREFERENCES & SETTINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CodeXTextMuted)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Color Theme", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CodeXTextPrimary)
        Spacer(modifier = Modifier.height(6.dp))

        val themes = listOf(
            "codex-dark" to "CodeX Dark (Default)",
            "vs-dark" to "Visual Studio Dark+",
            "dracula" to "Dracula Theme",
            "hc-black" to "High Contrast"
        )
        themes.forEach { (id, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setTheme(id) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = theme == id,
                    onClick = { viewModel.setTheme(id) },
                    colors = RadioButtonDefaults.colors(selectedColor = CodeXPrimary)
                )
                Text(label, fontSize = 13.sp, color = CodeXTextPrimary)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp), color = CodeXBorder)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Font Size ($fontSize px)", fontSize = 13.sp, color = CodeXTextPrimary)
            Row {
                IconButton(onClick = { if (fontSize > 10) viewModel.setFontSize(fontSize - 1) }) {
                    Icon(Icons.Default.Remove, "Decrease", tint = CodeXTextSecondary)
                }
                IconButton(onClick = { if (fontSize < 24) viewModel.setFontSize(fontSize + 1) }) {
                    Icon(Icons.Default.Add, "Increase", tint = CodeXTextSecondary)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Word Wrap", fontSize = 13.sp, color = CodeXTextPrimary)
            Switch(
                checked = wordWrap,
                onCheckedChange = { viewModel.toggleWordWrap() }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Monaco Minimap", fontSize = 13.sp, color = CodeXTextPrimary)
            Switch(
                checked = minimap,
                onCheckedChange = { viewModel.toggleMinimap() }
            )
        }
    }
}
