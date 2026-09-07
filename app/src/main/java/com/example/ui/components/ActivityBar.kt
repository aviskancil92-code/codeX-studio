package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SidebarTab
import com.example.ui.theme.*

@Composable
fun ActivityBar(
    selectedTab: SidebarTab,
    isOpen: Boolean,
    onSelectTab: (SidebarTab) -> Unit,
    onOpenCommandPalette: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(48.dp)
            .fillMaxHeight(),
        color = CodeXActivityBarBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ActivityBarItem(
                    icon = Icons.Default.Folder,
                    label = "Explorer",
                    isSelected = selectedTab == SidebarTab.EXPLORER && isOpen,
                    onClick = { onSelectTab(SidebarTab.EXPLORER) }
                )
                ActivityBarItem(
                    icon = Icons.Default.Search,
                    label = "Search",
                    isSelected = selectedTab == SidebarTab.SEARCH && isOpen,
                    onClick = { onSelectTab(SidebarTab.SEARCH) }
                )
                ActivityBarItem(
                    icon = Icons.Default.ForkRight,
                    label = "Source Control",
                    isSelected = selectedTab == SidebarTab.SCM && isOpen,
                    onClick = { onSelectTab(SidebarTab.SCM) }
                )
                ActivityBarItem(
                    icon = Icons.Default.PlayArrow,
                    label = "Run & Debug",
                    isSelected = selectedTab == SidebarTab.RUN_DEBUG && isOpen,
                    onClick = { onSelectTab(SidebarTab.RUN_DEBUG) }
                )
                ActivityBarItem(
                    icon = Icons.Default.Extension,
                    label = "Extensions",
                    isSelected = selectedTab == SidebarTab.EXTENSIONS && isOpen,
                    onClick = { onSelectTab(SidebarTab.EXTENSIONS) }
                )
                ActivityBarItem(
                    icon = Icons.Default.Widgets,
                    label = "Packages",
                    isSelected = selectedTab == SidebarTab.PACKAGES && isOpen,
                    onClick = { onSelectTab(SidebarTab.PACKAGES) }
                )
                ActivityBarItem(
                    icon = Icons.Default.Handyman,
                    label = "Toolchains",
                    isSelected = selectedTab == SidebarTab.TOOLCHAINS && isOpen,
                    onClick = { onSelectTab(SidebarTab.TOOLCHAINS) }
                )
            }

            // Bottom action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ActivityBarItem(
                    icon = Icons.Default.HealthAndSafety,
                    label = "CodeX Doctor",
                    isSelected = selectedTab == SidebarTab.DOCTOR && isOpen,
                    badgeColor = CodeXSecondary,
                    onClick = { onSelectTab(SidebarTab.DOCTOR) }
                )
                ActivityBarItem(
                    icon = Icons.Default.Terminal,
                    label = "Command Palette",
                    isSelected = false,
                    onClick = onOpenCommandPalette
                )
                ActivityBarItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    isSelected = selectedTab == SidebarTab.SETTINGS && isOpen,
                    onClick = { onSelectTab(SidebarTab.SETTINGS) }
                )
            }
        }
    }
}

@Composable
private fun ActivityBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    badgeColor: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // Active left indicator bar
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(2.5.dp)
                    .height(26.dp)
                    .background(CodeXPrimary)
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) CodeXPrimary else CodeXTextSecondary,
            modifier = Modifier.size(22.dp)
        )
    }
}
