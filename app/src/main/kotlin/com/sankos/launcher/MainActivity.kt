package com.sankos.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sankos.launcher.data.SankPrefs
import com.sankos.launcher.designsystem.theme.SankTheme
import com.sankos.launcher.launcher.LauncherRoot
import com.sankos.launcher.launcher.LauncherViewModel

/**
 * The single SankOS surface. Also registered as the device HOME activity.
 *
 * - singleTask + stateNotNeeded: launcher semantics, never stacked.
 * - configChanges: handled in Compose; rotation does not recreate the shell.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels { LauncherViewModel.factory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs by viewModel.prefs.collectAsState(initial = SankPrefs())
            SankTheme(accentEnabled = prefs.accentEnabled) {
                LauncherRoot(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onPause() {
        super.onPause()
        // Returning from an app or the lock screen should present a calm home,
        // not a half-open drawer.
        viewModel.requestDrawerReset()
    }
}
