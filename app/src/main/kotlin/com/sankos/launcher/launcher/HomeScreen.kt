package com.sankos.launcher.launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.sankos.launcher.data.ClockStyle
import com.sankos.launcher.data.SankPrefs
import com.sankos.launcher.designsystem.dotmatrix.DotMetrics
import com.sankos.launcher.designsystem.dotmatrix.DotText
import com.sankos.launcher.designsystem.icons.SankIcons
import com.sankos.launcher.designsystem.theme.InkElevated
import com.sankos.launcher.designsystem.theme.InkLine
import com.sankos.launcher.designsystem.theme.Paper
import com.sankos.launcher.designsystem.theme.PaperDim
import com.sankos.launcher.designsystem.theme.PaperFaint
import com.sankos.launcher.designsystem.theme.SankType
import com.sankos.launcher.designsystem.theme.sankAccent
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private data class ClockReadout(
    val timeText: String,
    val dateText: String,
)

/**
 * The home surface: a clock, a date, a battery line, three essential apps.
 * Nothing else. The emptiness is the feature.
 */
@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    batteryLevel: Int,
    progressProvider: () -> Float,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val prefs by viewModel.prefs.collectAsState()
    val dock by viewModel.dock.collectAsState()
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val accent = sankAccent()

    val clock by rememberClock(prefs.use24Hour)

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp),
    ) {
        // status strip: brand-quiet, battery only
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                text = if (batteryLevel >= 0) "$batteryLevel%" else "—",
                style = SankType.StatusMono,
                color = PaperDim,
            )
        }

        Spacer(Modifier.weight(0.85f))

        // clock + date
        Column(horizontalAlignment = Alignment.Start) {
            when (prefs.clockStyle) {
                ClockStyle.BIG -> Text(
                    text = clock.timeText,
                    style = SankType.ClockLarge,
                    color = Paper,
                    maxLines = 1,
                )
                ClockStyle.DOT -> DotText(
                    text = clock.timeText.filter { it.isDigit() || it == ':' },
                    dot = 5.dp,
                    gap = 2.5.dp,
                    color = Paper,
                    modifier = Modifier.height(DotMetrics.height(5.dp, 2.5.dp)),
                )
            }
            Spacer(Modifier.height(14.dp))
            DotText(
                text = clock.dateText,
                dot = 1.8.dp,
                gap = 1.5.dp,
                color = accent,
                gridColor = InkLine,
                modifier = Modifier.height(DotMetrics.height(1.8.dp, 1.5.dp)),
            )
        }

        Spacer(Modifier.weight(1.15f))

        // dock
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            dock.forEach { target ->
                DockChip(
                    label = target.entry.label,
                    icon = target.icon,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.launchDockTarget(context, target)
                    },
                )
            }
        }

        Spacer(Modifier.height(34.dp))

        // gesture hint — shown until the drawer has been opened once
        if (!prefs.hasSeenDrawerHint) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = SankIcons.Expand,
                    contentDescription = null,
                    tint = PaperFaint,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.height(6.dp))
                Text("SWIPE UP", style = SankType.SectionLabel, color = PaperFaint)
            }
        } else {
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun DockChip(
    label: String,
    icon: android.graphics.drawable.Drawable?,
    onClick: () -> Unit,
) {
    val bitmap = remember(icon) {
        runCatching { icon?.toBitmap(96, 96)?.asImageBitmap() }.getOrNull()
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        var pressed by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .size(58.dp)
                .scale(if (pressed) 0.9f else 1f)
                .clip(CircleShape)
                .background(InkElevated)
                .clickable(
                    interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource(),
                    indication = null,
                ) {
                    pressed = true
                    onClick()
                },
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Image(bitmap = bitmap, contentDescription = label, modifier = Modifier.size(30.dp))
            } else {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(PaperFaint))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = SankType.StatusMono, color = PaperFaint, maxLines = 1)
    }
}

/** Ticking clock readout; recomposes once per second only while visible. */
@Composable
private fun rememberClock(use24Hour: Boolean): androidx.compose.runtime.State<ClockReadout> {
    val readout = remember { mutableStateOf(readClock(use24Hour)) }
    LaunchedEffect(use24Hour) {
        while (true) {
            delay(1000)
            readout.value = readClock(use24Hour)
        }
    }
    // recompute immediately when the 24h toggle flips
    LaunchedEffect(use24Hour) { readout.value = readClock(use24Hour) }
    return readout
}

private fun readClock(use24Hour: Boolean): ClockReadout {
    val now = LocalDateTime.now()
    val hour = now.hour
    val hourText = if (use24Hour) {
        "%02d".format(hour)
    } else {
        val h = hour % 12
        (if (h == 0) 12 else h).toString()
    }
    val minuteText = "%02d".format(now.minute)
    val amPm = if (!use24Hour) {
        if (hour < 12) " AM" else " PM"
    } else {
        ""
    }
    val weekday = now.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.ENGLISH).uppercase(Locale.ENGLISH)
    val month = now.month.getDisplayName(JavaTextStyle.SHORT, Locale.ENGLISH).uppercase(Locale.ENGLISH)
    val dateText = "$weekday · %02d $month".format(now.dayOfMonth)
    return ClockReadout(timeText = "$hourText:$minuteText$amPm", dateText = dateText)
}
