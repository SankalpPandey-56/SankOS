package com.sankos.launcher.launcher

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sankos.launcher.data.AppRepository
import com.sankos.launcher.data.ClockStyle
import com.sankos.launcher.data.PrefsRepository
import com.sankos.launcher.data.SankPrefs
import com.sankos.launcher.data.UsageRepository
import com.sankos.launcher.data.model.AppEntry
import com.sankos.launcher.domain.ActionKind
import com.sankos.launcher.domain.AppRanker
import com.sankos.launcher.domain.RankedApp
import com.sankos.launcher.domain.SearchEngine
import com.sankos.launcher.domain.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Launcher state holder. All heavy work (package scan, usage aggregation,
 * ranking) happens off the main thread; the UI subscribes to plain state.
 */
class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository(application)
    private val usageRepository = UsageRepository(application)
    private val prefsRepository = PrefsRepository(application)

    val prefs: StateFlow<SankPrefs> = prefsRepository.prefs
        .stateIn(viewModelScope, SharingStarted.Eagerly, SankPrefs())

    private val _apps = MutableStateFlow(AppRepository.LoadedApps(emptyList(), emptyMap()))
    val apps: StateFlow<AppRepository.LoadedApps> = _apps.asStateFlow()

    private val _ranked = MutableStateFlow<List<RankedApp>>(emptyList())
    val ranked: StateFlow<List<RankedApp>> = _ranked.asStateFlow()

    private val _dock = MutableStateFlow<List<AppRepository.DockTarget>>(emptyList())
    val dock: StateFlow<List<AppRepository.DockTarget>> = _dock.asStateFlow()

    private val _usageAccess = MutableStateFlow(false)
    val usageAccess: StateFlow<Boolean> = _usageAccess.asStateFlow()

    /** Monotonic signal to close the drawer (activity paused / app launched). */
    private val _drawerReset = MutableStateFlow(0L)
    val drawerReset: StateFlow<Long> = _drawerReset.asStateFlow()

    init {
        refresh()
    }

    /** Reload apps + usage; called on create and every resume. */
    fun refresh() {
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) { appRepository.loadApps() }
            _apps.value = loaded
            _dock.value = withContext(Dispatchers.IO) { appRepository.resolveDockApps() }
            _usageAccess.value = withContext(Dispatchers.IO) { usageRepository.hasUsageAccess() }
            recomputeRanking()
        }
    }

    private fun recomputeRanking() {
        viewModelScope.launch(Dispatchers.Default) {
            val usage = if (_usageAccess.value) usageRepository.loadUsage() else emptyMap()
            _ranked.value = AppRanker.rank(_apps.value.apps, usage, System.currentTimeMillis())
        }
    }

    fun search(query: String): List<SearchResult> =
        SearchEngine.search(query, _apps.value.apps)

    // --- actions ---------------------------------------------------------

    fun launchApp(context: Context, app: AppEntry) {
        appRepository.launchIntentFor(app.packageName)?.let(context::startActivity)
        requestDrawerReset()
    }

    fun launchDockTarget(context: Context, target: AppRepository.DockTarget) {
        appRepository.launchIntentFor(target.entry.packageName)?.let(context::startActivity)
    }

    fun executeAction(context: Context, action: SearchResult.QuickAction) {
        when (action.kind) {
            ActionKind.TIMER -> {
                val minutes = action.payload.toIntOrNull() ?: return
                val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                    putExtra(AlarmClock.EXTRA_LENGTH, minutes * 60)
                    putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                runCatching { context.startActivity(intent) }
            }
            ActionKind.DIAL -> {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${action.payload}"))
                runCatching { context.startActivity(intent) }
            }
            ActionKind.SANK_SETTINGS -> Unit // handled by UI (opens customize sheet)
        }
        requestDrawerReset()
    }

    fun openUsageAccessSettings(context: Context) {
        runCatching { context.startActivity(usageRepository.usageAccessIntent()) }
    }

    fun requestDrawerReset() {
        _drawerReset.value = System.currentTimeMillis()
    }

    // --- preferences -----------------------------------------------------

    fun setClockStyle(style: ClockStyle) = viewModelScope.launch {
        prefsRepository.setClockStyle(style)
    }

    fun setAccentEnabled(enabled: Boolean) = viewModelScope.launch {
        prefsRepository.setAccentEnabled(enabled)
    }

    fun setUse24Hour(enabled: Boolean) = viewModelScope.launch {
        prefsRepository.setUse24Hour(enabled)
    }

    fun markDrawerHintSeen() = viewModelScope.launch {
        prefsRepository.setDrawerHintSeen()
    }

    // --- factory -----------------------------------------------------------

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LauncherViewModel(application) as T
            }
    }
}
