package com.sankos.launcher.data

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import com.sankos.launcher.data.model.AppUsage
import com.sankos.launcher.domain.UsageAnalysis
import com.sankos.launcher.domain.UsageEvent
import java.util.concurrent.TimeUnit

/**
 * Local app-usage signals for adaptive ranking.
 *
 * Requires the special PACKAGE_USAGE_STATS permission (granted by the user
 * in system settings). Everything stays on-device; without the permission
 * the drawer falls back to alphabetical order — never a hard failure.
 */
class UsageRepository(private val context: Context) {

    fun hasUsageAccess(): Boolean {
        val ops = context.getSystemService(AppOpsManager::class.java) ?: return false
        @Suppress("DEPRECATION") // still the correct read-only check; the replacement throws on denial
        val mode = runCatching {
            ops.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        }.getOrDefault(AppOpsManager.MODE_DEFAULT)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** System screen where the user grants usage access. */
    fun usageAccessIntent(): Intent =
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    /**
     * Aggregates the last [days] days of foreground transitions into
     * [AppUsage] per package.
     */
    fun loadUsage(days: Int = ANALYSIS_DAYS): Map<String, AppUsage> {
        if (!hasUsageAccess()) return emptyMap()
        val usm = context.getSystemService(UsageStatsManager::class.java) ?: return emptyMap()

        val now = System.currentTimeMillis()
        val start = now - TimeUnit.DAYS.toMillis(days.toLong())
        val events = ArrayList<UsageEvent>(1024)

        val box = usm.queryEvents(start, now)
        val event = UsageEvents.Event()
        while (box.hasNextEvent()) {
            box.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED ->
                    events.add(UsageEvent(event.packageName, event.timeStamp, isStart = true))
                UsageEvents.Event.ACTIVITY_PAUSED ->
                    events.add(UsageEvent(event.packageName, event.timeStamp, isStart = false))
            }
        }
        return UsageAnalysis.aggregate(events, now)
    }

    companion object {
        const val ANALYSIS_DAYS = 14

        @Deprecated("Use hasUsageAccess; kept to document the manifest permission name")
        const val MANIFEST_PERMISSION = Manifest.permission.PACKAGE_USAGE_STATS
    }
}
