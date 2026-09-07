package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.DiagnosticSeverity
import com.example.core.process.LineType
import com.example.ui.BottomTab
import com.example.ui.CodeXViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun BottomPanelView(
    viewModel: CodeXViewModel,
    onNavigateToFile: (file: File, line: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.activeBottomTab.collectAsState()
    val buildResult by viewModel.buildResult.collectAsState()
    val isBuilding by viewModel.isBuilding.collectAsState()

    val problemCount = buildResult?.diagnostics?.size ?: 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CodeXPanelBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(CodeXActivityBarBg)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tabs
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PanelTabItem("TERMINAL", activeTab == BottomTab.TERMINAL) {
                        viewModel.selectBottomTab(BottomTab.TERMINAL)
                    }
                    PanelTabItem("PROBLEMS ${if (problemCount > 0) "($problemCount)" else ""}", activeTab == BottomTab.PROBLEMS) {
                        viewModel.selectBottomTab(BottomTab.PROBLEMS)
                    }
                    PanelTabItem("OUTPUT", activeTab == BottomTab.OUTPUT) {
                        viewModel.selectBottomTab(BottomTab.OUTPUT)
                    }
                    PanelTabItem("DEBUG CONSOLE", activeTab == BottomTab.DEBUG) {
                        viewModel.selectBottomTab(BottomTab.DEBUG)
                    }
                    PanelTabItem("BUILD", activeTab == BottomTab.BUILD) {
                        viewModel.selectBottomTab(BottomTab.BUILD)
                    }
                    PanelTabItem("LOGCAT", activeTab == BottomTab.LOGCAT) {
                        viewModel.selectBottomTab(BottomTab.LOGCAT)
                    }
                }

                // Close / Minimize
                IconButton(
                    onClick = { viewModel.toggleBottomPanel() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, "Close Panel", tint = CodeXTextMuted, modifier = Modifier.size(16.dp))
                }
            }

            // Body
            Box(modifier = Modifier.fillMaxSize()) {
                when (activeTab) {
                    BottomTab.TERMINAL -> TerminalTab(viewModel)
                    BottomTab.PROBLEMS -> ProblemsTab(viewModel, onNavigateToFile)
                    BottomTab.OUTPUT -> OutputTab(viewModel)
                    BottomTab.DEBUG -> DebugConsoleTab(viewModel)
                    BottomTab.BUILD -> BuildTab(viewModel)
                    BottomTab.LOGCAT -> LogcatTab(viewModel)
                }
            }
        }
    }
}

@Composable
private fun PanelTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) CodeXPrimary else CodeXTextSecondary
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(CodeXPrimary)
            )
        }
    }
}

// 1. TERMINAL TAB
@Composable
private fun TerminalTab(viewModel: CodeXViewModel) {
    val sessions by viewModel.processManager.sessions.collectAsState()
    val activeSessionId by viewModel.processManager.activeSessionId.collectAsState()
    val activeSession = sessions.firstOrNull { it.id == activeSessionId } ?: sessions.firstOrNull()

    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(activeSession?.lines?.size) {
        if ((activeSession?.lines?.size ?: 0) > 0) {
            listState.animateScrollToItem(activeSession!!.lines.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Terminal Session Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CodeXBorderSubtle)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                sessions.forEach { sess ->
                    Text(
                        text = sess.title,
                        fontSize = 11.sp,
                        color = if (sess.id == activeSessionId) CodeXPrimary else CodeXTextSecondary,
                        modifier = Modifier
                            .background(
                                if (sess.id == activeSessionId) CodeXPanelBg else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { viewModel.processManager.selectSession(sess.id) }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.processManager.createSession() },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(Icons.Default.Add, "New Session", tint = CodeXTextMuted, modifier = Modifier.size(14.dp))
                }
            }

            // Kill active process
            if (activeSession?.isRunning == true) {
                Button(
                    onClick = { viewModel.processManager.killProcess(activeSession.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = CodeXError),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Kill", fontSize = 10.sp, color = Color.White)
                }
            }
        }

        // Terminal Output Lines
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            items(activeSession?.lines ?: emptyList()) { line ->
                val color = when (line.type) {
                    LineType.INPUT -> CodeXSecondary
                    LineType.OUTPUT -> CodeXTextPrimary
                    LineType.ERROR -> CodeXError
                    LineType.SYSTEM -> CodeXPrimary
                }
                Text(
                    text = line.text,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = color,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }

        // Command Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CodeXActivityBarBg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$ ", fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = CodeXSecondary)
            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                placeholder = { Text("Enter command...", fontSize = 12.sp, color = CodeXTextMuted) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = CodeXTextPrimary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (commandInput.isNotBlank()) {
                            viewModel.sendTerminalCommand(commandInput)
                            commandInput = ""
                        }
                    }
                )
            )
            IconButton(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        viewModel.sendTerminalCommand(commandInput)
                        commandInput = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, "Send", tint = CodeXPrimary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// 2. PROBLEMS TAB
@Composable
private fun ProblemsTab(
    viewModel: CodeXViewModel,
    onNavigateToFile: (file: File, line: Int, col: Int) -> Unit
) {
    val buildResult by viewModel.buildResult.collectAsState()
    val diagnostics = buildResult?.diagnostics ?: emptyList()
    val proj = viewModel.currentProject.collectAsState().value

    if (diagnostics.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No problems detected in workspace", fontSize = 12.sp, color = CodeXTextMuted)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            items(diagnostics) { diag ->
                val icon = if (diag.severity == DiagnosticSeverity.ERROR) Icons.Default.Cancel else Icons.Default.Warning
                val color = if (diag.severity == DiagnosticSeverity.ERROR) CodeXError else CodeXWarning

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable {
                            val f = File(proj, diag.file)
                            if (f.exists()) {
                                onNavigateToFile(f, diag.line, diag.column)
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = CodeXSidebarBg)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(diag.file, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CodeXPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("[${diag.line}, ${diag.column}]", fontSize = 11.sp, color = CodeXTextMuted)
                            }
                            Text(diag.message, fontSize = 12.sp, color = CodeXTextPrimary)
                            if (diag.suggestedFix != null) {
                                Text("Suggested Fix: ${diag.suggestedFix}", fontSize = 11.sp, color = CodeXSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. OUTPUT TAB
@Composable
private fun OutputTab(viewModel: CodeXViewModel) {
    val result by viewModel.buildResult.collectAsState()

    if (result == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No execution output yet. Press ▶ Run to see output.", fontSize = 12.sp, color = CodeXTextMuted)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Command: ${result?.command}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = CodeXPrimary)
                    Text(
                        text = if (result?.isSuccess == true) "EXIT 0" else "EXIT ${result?.exitCode}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result?.isSuccess == true) CodeXSuccess else CodeXError
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Execution Time: ${result?.durationMs} ms • Memory: ${result?.memoryKb ?: 0} KB",
                    fontSize = 11.sp,
                    color = CodeXTextMuted
                )
                Divider(modifier = Modifier.padding(vertical = 6.dp), color = CodeXBorder)
            }

            if (!result?.stdout.isNullOrBlank()) {
                item {
                    Text(
                        text = result!!.stdout,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CodeXTextPrimary
                    )
                }
            }

            if (!result?.stderr.isNullOrBlank()) {
                item {
                    Text(
                        text = result!!.stderr,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CodeXError
                    )
                }
            }
        }
    }
}

// 4. DEBUG CONSOLE TAB
@Composable
private fun DebugConsoleTab(viewModel: CodeXViewModel) {
    var debugInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Text(
                    "CodeX Debug Engine (ART / LLDB Subsystem)\nNo active breakpoint paused.",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CodeXTextMuted
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = debugInput,
                onValueChange = { debugInput = it },
                placeholder = { Text("Evaluate expression...", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(onClick = { debugInput = "" }) {
                Icon(Icons.Default.PlayArrow, "Eval", tint = CodeXSecondary)
            }
        }
    }
}

// 5. BUILD TAB
@Composable
private fun BuildTab(viewModel: CodeXViewModel) {
    val result by viewModel.buildResult.collectAsState()
    val isBuilding by viewModel.isBuilding.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        if (isBuilding) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CodeXPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Compiling Android application & packaging APK...", fontSize = 12.sp, color = CodeXTextSecondary)
        } else if (result != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (result!!.isSuccess) "✓ Build Successful" else "✗ Build Failed",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (result!!.isSuccess) CodeXSuccess else CodeXError
                )

                if (result!!.artifactPath != null) {
                    Button(
                        onClick = { viewModel.installGeneratedApk() },
                        colors = ButtonDefaults.buttonColors(containerColor = CodeXPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.InstallMobile, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Install APK", fontSize = 12.sp, color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        text = result!!.stdout + (if (result!!.stderr.isNotBlank()) "\n" + result!!.stderr else ""),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (result!!.isSuccess) CodeXTextPrimary else CodeXError
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Press 'Build Android APK' in Run tab to initiate packaging", fontSize = 12.sp, color = CodeXTextMuted)
            }
        }
    }
}

// 6. LOGCAT TAB
@Composable
private fun LogcatTab(viewModel: CodeXViewModel) {
    val entries by viewModel.logcatEntries.collectAsState()
    val currentFilter by viewModel.logcatFilter.collectAsState()
    val searchQuery by viewModel.logcatSearch.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshLogcat()
    }

    Column(modifier = Modifier.fillMaxSize().padding(6.dp)) {
        // Logcat Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Level Filter
            val levels = listOf("ALL", "V", "D", "I", "W", "E")
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                levels.forEach { lvl ->
                    Text(
                        text = lvl,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentFilter == lvl) Color.Black else CodeXTextSecondary,
                        modifier = Modifier
                            .background(
                                if (currentFilter == lvl) CodeXPrimary else CodeXBorderSubtle,
                                RoundedCornerShape(3.dp)
                            )
                            .clickable { viewModel.setLogcatFilter(lvl) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setLogcatSearch(it) },
                placeholder = { Text("Filter logs...", fontSize = 11.sp) },
                modifier = Modifier.weight(1f).height(40.dp),
                singleLine = true
            )

            IconButton(onClick = { viewModel.refreshLogcat() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Refresh, "Refresh", tint = CodeXPrimary, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = { viewModel.clearLogcat() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.DeleteSweep, "Clear", tint = CodeXTextSecondary, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(entries) { entry ->
                val levelColor = when (entry.level) {
                    "E", "F" -> CodeXError
                    "W" -> CodeXWarning
                    "I" -> CodeXPrimary
                    "D" -> CodeXSecondary
                    else -> CodeXTextMuted
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = entry.level,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor,
                        modifier = Modifier.width(14.dp)
                    )
                    Text(
                        text = entry.tag.take(15),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CodeXPrimary,
                        maxLines = 1,
                        modifier = Modifier.width(90.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = entry.message,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CodeXTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
