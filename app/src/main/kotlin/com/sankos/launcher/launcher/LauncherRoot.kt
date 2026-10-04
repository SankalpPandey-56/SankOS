package com.sankos.launcher.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sankos.launcher.designsystem.motion.SankMotion
import com.sankos.launcher.designsystem.theme.Ink
import kotlinx.coroutines.launch

/**
 * The SankOS shell: home and drawer are one continuous space.
 *
 * The drawer is not a screen that replaces home — it is a panel that the
 * home surface yields to. The gesture drives an [Animatable] progress
 * directly (finger-following), and releasing hands off to a spring.
 */
@Composable
fun LauncherRoot(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val dragScale = screenHeightPx / 2.6f // fraction of screen dragged for full open

    val progress = remember { Animatable(0f) }
    var drawerInComposition by remember { mutableStateOf(false) }

    var customizeSheetOpen by remember { mutableStateOf(false) }

    val batteryLevel = rememberBatteryLevel()
    val drawerReset by viewModel.drawerReset.collectAsState()

    fun openDrawer(fromProgress: Float = progress.value) {
        drawerInComposition = true
        scope.launch {
            progress.animateTo(1f, spring(dampingRatio = 0.86f, stiffness = 380f))
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        if (fromProgress == 0f) viewModel.markDrawerHintSeen()
    }

    fun closeDrawer() {
        scope.launch {
            progress.animateTo(0f, spring(dampingRatio = 0.92f, stiffness = 300f))
            drawerInComposition = false
        }
    }

    // Leaving the launcher (app launched, screen off) always resets to home.
    LaunchedEffect(drawerReset) {
        if (drawerReset > 0 && (progress.value > 0f || drawerInComposition)) {
            progress.snapTo(0f)
            drawerInComposition = false
        }
    }

    BackHandler(enabled = drawerInComposition || customizeSheetOpen) {
        if (customizeSheetOpen) customizeSheetOpen = false else closeDrawer()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink),
    ) {
        // --- home -----------------------------------------------------------
        HomeScreen(
            viewModel = viewModel,
            batteryLevel = batteryLevel,
            progressProvider = { progress.value },
            onOpenDrawer = { openDrawer() },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val p = progress.value
                    scaleX = 1f - 0.05f * p
                    scaleY = 1f - 0.05f * p
                    alpha = 1f - 0.5f * p
                }
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { customizeSheetOpen = true })
                }
                .pointerInput(dragScale) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            val next = (progress.value - dragAmount / dragScale).coerceIn(0f, 1f)
                            scope.launch { progress.snapTo(next) }
                        },
                        onDragEnd = {
                            if (progress.value > 0.32f) openDrawer() else closeDrawer()
                        },
                        onDragCancel = {
                            if (progress.value > 0.32f) openDrawer() else closeDrawer()
                        },
                    )
                },
        )

        // --- drawer ---------------------------------------------------------
        if (drawerInComposition) {
            DrawerScreen(
                viewModel = viewModel,
                progressProvider = { progress.value },
                onClose = { closeDrawer() },
                onSankSettings = {
                    closeDrawer()
                    customizeSheetOpen = true
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val p = progress.value
                        alpha = p
                        translationY = (1f - p) * 240f
                        scaleX = 0.96f + 0.04f * p
                        scaleY = 0.96f + 0.04f * p
                    }
                    .pointerInput(dragScale) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                val next = (progress.value + dragAmount / dragScale).coerceIn(0f, 1f)
                                scope.launch { progress.snapTo(next) }
                            },
                            onDragEnd = {
                                if (progress.value < 0.68f) closeDrawer() else openDrawer()
                            },
                            onDragCancel = {
                                if (progress.value < 0.68f) closeDrawer() else openDrawer()
                            },
                        )
                    },
            )
        }

        // --- customize sheet --------------------------------------------------
        if (customizeSheetOpen) {
            CustomizeSheet(
                viewModel = viewModel,
                usageAccessGranted = viewModel.usageAccess.collectAsState().value,
                onOpenUsageAccess = { viewModel.openUsageAccessSettings(context) },
                onDismiss = { customizeSheetOpen = false },
            )
        }
    }
}

/** Battery percentage, updated live while the launcher is visible. */
@Composable
private fun rememberBatteryLevel(): Int {
    val context = LocalContext.current
    var level by remember { mutableStateOf(initialBatteryLevel(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent) {
                val newLevel = batteryLevelFrom(intent)
                if (newLevel >= 0) level = newLevel
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onDispose { context.unregisterReceiver(receiver) }
    }
    return level
}

private fun initialBatteryLevel(context: Context): Int =
    batteryLevelFrom(
        context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)),
    )

private fun batteryLevelFrom(intent: Intent?): Int {
    intent ?: return -1
    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    if (level < 0 || scale <= 0) return -1
    return (level * 100 / scale)
}
