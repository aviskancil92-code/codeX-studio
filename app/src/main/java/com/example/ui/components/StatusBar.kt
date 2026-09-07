package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.BuildResult
import com.example.core.model.GitStatus
import com.example.ui.theme.*

@Composable
fun StatusBar(
    gitStatus: GitStatus,
    activeLanguage: String,
    cursorLine: Int,
    cursorCol: Int,
    buildResult: BuildResult?,
    isBuilding: Boolean,
    onStatusClick: () -> Unit,
    onDoctorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barColor = when {
        isBuilding -> CodeXStatusBarBuildBg
        buildResult?.isSuccess == false -> CodeXStatusBarDebugBg
        else -> CodeXStatusBarBg
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp),
        color = barColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Git & Diagnostics
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Git Branch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onStatusClick() }
                ) {
                    Icon(
                        Icons.Default.ForkRight,
                        contentDescription = "Git Branch",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(gitStatus.branch, fontSize = 11.sp, color = Color.White)
                }

                // Diagnostics summary
                val errorCount = buildResult?.diagnostics?.count { it.severity.name == "ERROR" } ?: 0
                val warnCount = buildResult?.diagnostics?.count { it.severity.name == "WARNING" } ?: 0

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Cancel, null, tint = Color.White, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("$errorCount", fontSize = 11.sp, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("$warnCount", fontSize = 11.sp, color = Color.White)
                }

                if (isBuilding) {
                    Text("Building...", fontSize = 11.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right: Line/Col, Encoding, Indent, Language, Doctor
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Ln $cursorLine, Col $cursorCol", fontSize = 11.sp, color = Color.White)
                Text("Spaces: 4", fontSize = 11.sp, color = Color.White)
                Text("UTF-8", fontSize = 11.sp, color = Color.White)
                Text(activeLanguage, fontSize = 11.sp, color = Color.White)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onDoctorClick() }
                ) {
                    Icon(Icons.Default.HealthAndSafety, "Doctor", tint = Color.White, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Doctor", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}
