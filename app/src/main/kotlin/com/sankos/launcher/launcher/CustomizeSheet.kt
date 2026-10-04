package com.sankos.launcher.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sankos.launcher.data.ClockStyle
import com.sankos.launcher.designsystem.icons.SankIcons
import com.sankos.launcher.designsystem.theme.InkElevated
import com.sankos.launcher.designsystem.theme.InkRaised
import com.sankos.launcher.designsystem.theme.InkLine
import com.sankos.launcher.designsystem.theme.Paper
import com.sankos.launcher.designsystem.theme.PaperDim
import com.sankos.launcher.designsystem.theme.PaperFaint
import com.sankos.launcher.designsystem.theme.SankType
import com.sankos.launcher.designsystem.theme.sankAccent

/**
 * SankOS settings: a short, flat sheet — not a settings maze.
 * Every control here is functional and persisted via DataStore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeSheet(
    viewModel: LauncherViewModel,
    usageAccessGranted: Boolean,
    onOpenUsageAccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    val prefs by viewModel.prefs.collectAsState()
    val accent = sankAccent()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = InkElevated,
        contentColor = Paper,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 34.dp),
        ) {
            Text("CUSTOMIZE", style = SankType.SectionLabel, color = PaperFaint)
            Spacer(Modifier.height(18.dp))

            // --- clock style ------------------------------------------------
            Text("Clock", style = SankType.Title, color = Paper)
            Spacer(Modifier.height(10.dp))
            Row {
                ClockStyle.entries.forEach { style ->
                    val selected = prefs.clockStyle == style
                    Text(
                        text = if (style == ClockStyle.BIG) "Numerals" else "Dot matrix",
                        style = SankType.Body,
                        color = if (selected) Paper else PaperDim,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setClockStyle(style) }
                            .background(if (selected) InkRaised else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                }
            }

            Spacer(Modifier.height(22.dp))
            Divider()

            // --- accent -------------------------------------------------------
            ToggleRow(
                title = "Orange accent",
                subtitle = "Only 2% of the interface, but yours",
                checked = prefs.accentEnabled,
                onCheckedChange = { viewModel.setAccentEnabled(it) },
            )
            ToggleRow(
                title = "24-hour clock",
                subtitle = "Military-style time everywhere",
                checked = prefs.use24Hour,
                onCheckedChange = { viewModel.setUse24Hour(it) },
            )

            Spacer(Modifier.height(6.dp))
            Divider()

            // --- usage access ---------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !usageAccessGranted, onClick = onOpenUsageAccess)
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Usage access", style = SankType.Title, color = Paper)
                    Text(
                        text = if (usageAccessGranted) {
                            "Granted — ranking adapts locally, on this device only"
                        } else {
                            "Enable so the drawer can learn how you actually use apps"
                        },
                        style = SankType.Body,
                        color = PaperDim,
                    )
                }
                if (usageAccessGranted) {
                    Icon(SankIcons.Check, contentDescription = null, tint = accent)
                } else {
                    Icon(SankIcons.ChevronRight, contentDescription = null, tint = PaperFaint)
                }
            }

            Divider()

            Spacer(Modifier.height(10.dp))
            Text("SankOS 0.1 · local only · no network permission", style = SankType.StatusMono, color = PaperFaint)
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = SankType.Title, color = Paper)
            Text(subtitle, style = SankType.Body, color = PaperDim)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = sankAccent(),
                checkedThumbColor = InkElevated,
                uncheckedTrackColor = InkRaised,
                uncheckedThumbColor = PaperFaint,
                uncheckedBorderColor = InkLine,
            ),
        )
    }
}

@Composable
private fun Divider() {
    androidx.compose.material3.HorizontalDivider(color = InkLine, thickness = 0.5.dp)
}