package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.io.File

@Composable
fun EditorTabs(
    openTabs: List<File>,
    activeTab: File?,
    unsavedFiles: Set<String>,
    projectName: String,
    onTabClick: (File) -> Unit,
    onCloseTab: (File) -> Unit,
    onRunFile: () -> Unit,
    onSaveFile: () -> Unit,
    onToggleTerminal: () -> Unit,
    onNewFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Horizontal Tab Strip
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            color = CodeXActivityBarBg
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                openTabs.forEach { file ->
                    val isActive = file.absolutePath == activeTab?.absolutePath
                    val isUnsaved = unsavedFiles.contains(file.absolutePath)

                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(if (isActive) CodeXEditorBg else CodeXActivityBarBg)
                            .clickable { onTabClick(file) }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Top active indicator if active
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Top)
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(CodeXPrimary)
                            )
                        }

                        Text(
                            text = file.name,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isActive) CodeXTextPrimary else CodeXTextSecondary
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        if (isUnsaved) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(CodeXPrimary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        IconButton(
                            onClick = { onCloseTab(file) },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = CodeXTextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Add Tab button
                IconButton(
                    onClick = onNewFile,
                    modifier = Modifier.size(32.dp).padding(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Add, "New Tab", tint = CodeXTextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Breadcrumbs & Quick Action Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            color = CodeXSidebarBg
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Breadcrumbs
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(projectName, fontSize = 11.sp, color = CodeXTextMuted)
                    if (activeTab != null) {
                        Text(" > ", fontSize = 11.sp, color = CodeXTextMuted)
                        Text(activeTab.name, fontSize = 11.sp, color = CodeXTextPrimary, fontWeight = FontWeight.Medium)
                    }
                }

                // Action icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onRunFile,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, "Run", tint = CodeXSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onSaveFile,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Save, "Save", tint = CodeXPrimary, modifier = Modifier.size(15.dp))
                    }
                    IconButton(
                        onClick = onToggleTerminal,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Terminal, "Terminal", tint = CodeXTextSecondary, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}
