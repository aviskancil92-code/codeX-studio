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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.BottomTab
import com.example.ui.CodeXViewModel
import com.example.ui.SidebarTab
import com.example.ui.theme.*

data class PaletteCommand(
    val title: String,
    val category: String,
    val icon: ImageVector,
    val action: () -> Unit
)

@Composable
fun CommandPaletteDialog(
    viewModel: CodeXViewModel,
    onDismiss: () -> Unit,
    onFormat: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val commands = listOf(
        PaletteCommand("Run: Execute Active File", "Run", Icons.Default.PlayArrow) {
            viewModel.runActiveFile()
            onDismiss()
        },
        PaletteCommand("Android: Build Debug APK", "Android", Icons.Default.Android) {
            viewModel.buildAndroidApk(false)
            onDismiss()
        },
        PaletteCommand("Android: Build Release APK", "Android", Icons.Default.Android) {
            viewModel.buildAndroidApk(true)
            onDismiss()
        },
        PaletteCommand("Android: Install Generated APK", "Android", Icons.Default.InstallMobile) {
            viewModel.installGeneratedApk()
            onDismiss()
        },
        PaletteCommand("Editor: Format Document", "Editor", Icons.Default.AutoFixHigh) {
            onFormat()
            onDismiss()
        },
        PaletteCommand("Editor: Save Active File", "File", Icons.Default.Save) {
            viewModel.saveActiveFile()
            onDismiss()
        },
        PaletteCommand("Editor: Save All Files", "File", Icons.Default.SaveAs) {
            viewModel.saveAll()
            onDismiss()
        },
        PaletteCommand("Editor: Toggle Word Wrap", "View", Icons.Default.WrapText) {
            viewModel.toggleWordWrap()
            onDismiss()
        },
        PaletteCommand("Editor: Toggle Minimap", "View", Icons.Default.Map) {
            viewModel.toggleMinimap()
            onDismiss()
        },
        PaletteCommand("View: Toggle Terminal", "Terminal", Icons.Default.Terminal) {
            viewModel.selectBottomTab(BottomTab.TERMINAL)
            onDismiss()
        },
        PaletteCommand("View: Toggle Logcat", "Debug", Icons.Default.BugReport) {
            viewModel.selectBottomTab(BottomTab.LOGCAT)
            onDismiss()
        },
        PaletteCommand("View: Toggle Problems", "Diagnostics", Icons.Default.Warning) {
            viewModel.selectBottomTab(BottomTab.PROBLEMS)
            onDismiss()
        },
        PaletteCommand("Git: Initialize Repository", "Git", Icons.Default.ForkRight) {
            viewModel.gitInit()
            onDismiss()
        },
        PaletteCommand("CodeX Doctor: Run Self-Diagnostics", "System", Icons.Default.HealthAndSafety) {
            viewModel.selectSidebarTab(SidebarTab.DOCTOR)
            viewModel.runDoctor()
            onDismiss()
        },
        PaletteCommand("Project: New Project from Template", "Workspace", Icons.Default.AutoAwesome) {
            viewModel.isNewProjectDialogOpen = true
            onDismiss()
        },
        PaletteCommand("File: New File", "Explorer", Icons.Default.NoteAdd) {
            viewModel.isNewFileDialogOpen = true
            onDismiss()
        },
        PaletteCommand("File: New Folder", "Explorer", Icons.Default.CreateNewFolder) {
            viewModel.isNewFolderDialogOpen = true
            onDismiss()
        },
        PaletteCommand("Theme: CodeX Dark (Default)", "Preferences", Icons.Default.Palette) {
            viewModel.setTheme("codex-dark")
            onDismiss()
        },
        PaletteCommand("Theme: Dracula Gothic", "Preferences", Icons.Default.Palette) {
            viewModel.setTheme("dracula")
            onDismiss()
        },
        PaletteCommand("Theme: Visual Studio Dark+", "Preferences", Icons.Default.Palette) {
            viewModel.setTheme("vs-dark")
            onDismiss()
        },
        PaletteCommand("Toolchain: Open SDK Manager", "Toolchains", Icons.Default.Handyman) {
            viewModel.selectSidebarTab(SidebarTab.TOOLCHAINS)
            onDismiss()
        },
        PaletteCommand("Packages: Manage Dependencies", "Packages", Icons.Default.Widgets) {
            viewModel.selectSidebarTab(SidebarTab.PACKAGES)
            onDismiss()
        }
    )

    val filtered = commands.filter {
        it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CodeXPanelBg)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Type a command or search (>)...", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Terminal, null, tint = CodeXPrimary, modifier = Modifier.size(18.dp))
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered) { cmd ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { cmd.action() }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(cmd.icon, null, tint = CodeXPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cmd.title, fontSize = 13.sp, color = CodeXTextPrimary)
                                Text(cmd.category, fontSize = 10.sp, color = CodeXTextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
