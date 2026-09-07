package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.model.ProjectTemplate
import com.example.ui.CodeXViewModel
import com.example.ui.theme.*

@Composable
fun NewProjectDialog(
    viewModel: CodeXViewModel,
    onDismiss: () -> Unit
) {
    val templates = viewModel.workspaceManager.templates
    var selectedTemplate by remember { mutableStateOf(templates.first()) }
    var projectName by remember { mutableStateOf("MyProject") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CodeXPanelBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("CREATE NEW PROJECT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CodeXTextMuted)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("SELECT STARTER TEMPLATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CodeXTextMuted)
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(templates) { tmpl ->
                        val isSelected = tmpl.id == selectedTemplate.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedTemplate = tmpl },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CodeXBorderSubtle else CodeXSidebarBg
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tmpl.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CodeXTextPrimary)
                                    Text(tmpl.language, fontSize = 11.sp, color = CodeXPrimary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(tmpl.description, fontSize = 11.sp, color = CodeXTextSecondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = CodeXTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (projectName.isNotBlank()) {
                                viewModel.createProjectFromTemplate(selectedTemplate.id, projectName)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CodeXPrimary)
                    ) {
                        Text("Create Project", color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleInputDialog(
    title: String,
    label: String,
    initialValue: String = "",
    confirmText: String = "Create",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CodeXPanelBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CodeXTextMuted)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(label) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = CodeXTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (value.isNotBlank()) {
                                onConfirm(value.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CodeXPrimary)
                    ) {
                        Text(confirmText, color = Color.Black)
                    }
                }
            }
        }
    }
}
