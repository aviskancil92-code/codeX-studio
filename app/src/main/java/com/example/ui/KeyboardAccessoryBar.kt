package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun KeyboardAccessoryBar(
    onInsertText: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onFormat: () -> Unit,
    onFind: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickSymbols = listOf(
        "    ", "{", "}", "(", ")", "[", "]", ";", ":", "<", ">",
        "=", "+", "-", "*", "/", "\"", "'", "$", "&", "|", "!", "?", "->", "=>", "_"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        color = CodeXActivityBarBg,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Action Keys
            AccessoryIconButton(
                icon = Icons.Default.Undo,
                label = "Undo",
                onClick = onUndo
            )
            AccessoryIconButton(
                icon = Icons.Default.Redo,
                label = "Redo",
                onClick = onRedo
            )
            AccessoryTextButton(
                text = "TAB",
                isHighlighted = true,
                onClick = { onInsertText("    ") }
            )
            AccessoryIconButton(
                icon = Icons.Default.AutoFixHigh,
                label = "Format",
                onClick = onFormat
            )
            AccessoryIconButton(
                icon = Icons.Default.Search,
                label = "Find",
                onClick = onFind
            )

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(CodeXBorder)
            )

            // Symbols
            quickSymbols.drop(1).forEach { symbol ->
                AccessoryTextButton(
                    text = symbol,
                    onClick = { onInsertText(symbol) }
                )
            }
        }
    }
}

@Composable
private fun AccessoryTextButton(
    text: String,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 36.dp, minHeight = 36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isHighlighted) CodeXAccent else CodeXPanelBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isHighlighted) CodeXTextPrimary else CodeXTextPrimary,
            fontSize = 13.sp,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun AccessoryIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(CodeXPanelBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = CodeXTextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
