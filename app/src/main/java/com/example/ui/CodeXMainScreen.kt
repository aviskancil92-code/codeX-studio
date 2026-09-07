package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.languages.LanguageRegistry
import com.example.ui.components.*
import com.example.ui.theme.*
import java.io.File

@Composable
fun CodeXMainScreen(
    viewModel: CodeXViewModel,
    modifier: Modifier = Modifier
) {
    val currentProject by viewModel.currentProject.collectAsState()
    val openTabs by viewModel.openTabs.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val fileContents by viewModel.fileContents.collectAsState()
    val unsavedFiles by viewModel.unsavedFiles.collectAsState()
    val cursorLine by viewModel.cursorLine.collectAsState()
    val cursorCol by viewModel.cursorColumn.collectAsState()

    val activeSidebarTab by viewModel.activeSidebarTab.collectAsState()
    val isSidebarOpen by viewModel.isSidebarOpen.collectAsState()
    val isBottomPanelOpen by viewModel.isBottomPanelOpen.collectAsState()
    val isCommandPaletteOpen by viewModel.isCommandPaletteOpen.collectAsState()

    val gitStatus by viewModel.gitStatus.collectAsState()
    val buildResult by viewModel.buildResult.collectAsState()
    val isBuilding by viewModel.isBuilding.collectAsState()

    val theme by viewModel.theme.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val wordWrap by viewModel.wordWrap.collectAsState()
    val minimap by viewModel.minimap.collectAsState()

    val monacoController = remember { MonacoControllerImpl(null) }

    // Sync settings to Monaco when changed
    LaunchedEffect(theme) { monacoController.setTheme(theme) }
    LaunchedEffect(fontSize) { monacoController.setFontSize(fontSize) }
    LaunchedEffect(wordWrap) { monacoController.setWordWrap(wordWrap) }
    LaunchedEffect(minimap) { monacoController.setMinimap(minimap) }

    val activeLanguage = activeTab?.let { LanguageRegistry.detectByExtension(it.name).monacoLanguage } ?: "plaintext"
    val activeContent = activeTab?.let { fileContents[it.absolutePath] } ?: ""

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            StatusBar(
                gitStatus = gitStatus,
                activeLanguage = activeTab?.let { LanguageRegistry.detectByExtension(it.name).name } ?: "Plain Text",
                cursorLine = cursorLine,
                cursorCol = cursorCol,
                buildResult = buildResult,
                isBuilding = isBuilding,
                onStatusClick = { viewModel.selectSidebarTab(SidebarTab.SCM) },
                onDoctorClick = { viewModel.selectSidebarTab(SidebarTab.DOCTOR) }
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Activity Bar (Navigation Rail)
            ActivityBar(
                selectedTab = activeSidebarTab,
                isOpen = isSidebarOpen,
                onSelectTab = { tab -> viewModel.selectSidebarTab(tab) },
                onOpenCommandPalette = { viewModel.toggleCommandPalette() }
            )

            // 2. Sidebar View
            if (isSidebarOpen) {
                SidebarView(
                    viewModel = viewModel,
                    modifier = Modifier.width(240.dp)
                )

                // Divider line
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(CodeXBorder)
                )
            }

            // 3. Central Editor & Bottom Panel Area
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                // Tab Strip & Breadcrumbs
                EditorTabs(
                    openTabs = openTabs,
                    activeTab = activeTab,
                    unsavedFiles = unsavedFiles,
                    projectName = currentProject?.name ?: "No Project",
                    onTabClick = { file -> viewModel.openFile(file) },
                    onCloseTab = { file -> viewModel.closeTab(file) },
                    onRunFile = { viewModel.runActiveFile() },
                    onSaveFile = { viewModel.saveActiveFile() },
                    onToggleTerminal = { viewModel.toggleBottomPanel() },
                    onNewFile = { viewModel.isNewFileDialogOpen = true }
                )

                // Editor or Empty State
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (activeTab != null) {
                        MonacoEditorView(
                            controller = monacoController,
                            initialContent = activeContent,
                            language = activeLanguage,
                            filePath = activeTab!!.absolutePath,
                            onContentChange = { newText -> viewModel.updateContent(newText) },
                            onCursorPosition = { line, col -> viewModel.setCursor(line, col) },
                            onSaveRequested = { viewModel.saveActiveFile() }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CodeXEditorBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "CodeX Studio",
                                    fontSize = 20.sp,
                                    color = CodeXPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Select a file from Explorer to begin editing",
                                    fontSize = 13.sp,
                                    color = CodeXTextMuted
                                )
                            }
                        }
                    }
                }

                // Keyboard Accessory Bar
                KeyboardAccessoryBar(
                    onInsertText = { monacoController.insertText(it) },
                    onUndo = { monacoController.undo() },
                    onRedo = { monacoController.redo() },
                    onFormat = { monacoController.formatDocument() },
                    onFind = { monacoController.find() }
                )

                // 4. Bottom Panel (Terminal, Problems, Output, Build, Logcat)
                if (isBottomPanelOpen) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        BottomPanelView(
                            viewModel = viewModel,
                            onNavigateToFile = { file, line, col ->
                                viewModel.openFile(file)
                                monacoController.gotoLine(line, col)
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (isCommandPaletteOpen) {
        CommandPaletteDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.toggleCommandPalette() },
            onFormat = { monacoController.formatDocument() }
        )
    }

    if (viewModel.isNewProjectDialogOpen) {
        NewProjectDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.isNewProjectDialogOpen = false }
        )
    }

    if (viewModel.isNewFileDialogOpen) {
        SimpleInputDialog(
            title = "New File",
            label = "File Name (e.g. main.py, App.kt)",
            onConfirm = { name -> viewModel.createNewFile(name) },
            onDismiss = { viewModel.isNewFileDialogOpen = false }
        )
    }

    if (viewModel.isNewFolderDialogOpen) {
        SimpleInputDialog(
            title = "New Folder",
            label = "Folder Name",
            onConfirm = { name -> viewModel.createNewFolder(name) },
            onDismiss = { viewModel.isNewFolderDialogOpen = false }
        )
    }
}
