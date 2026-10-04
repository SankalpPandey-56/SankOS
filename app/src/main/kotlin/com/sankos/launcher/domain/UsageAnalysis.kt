package com.sankos.launcher.domain

import com.sankos.launcher.data.model.AppUsage
import java.util.Calendar
import java.util.TimeZone

/** One foreground transition pulled from UsageStatsManager. */
data class UsageEvent(
    val packageName: String,
    val timestampMs: Long,
    val isStart: Boolean,
)

/**
 * Pure, platform-free aggregation of raw foreground transitions.
 *
 * Produces per-app launch counts, foreground duration, most-recent use and
 * an hour-of-day histogram. Unit tested against synthetic event streams.
 */
object UsageAnalysis {

    fun aggregate(
        events: List<UsageEvent>,
        nowMs: Long,
        zone: TimeZone = TimeZone.getDefault(),
    ): Map<String, AppUsage> {
        val byPackage = LinkedHashMap<String, MutableList<UsageEvent>>()
        for (event in events) {
            byPackage.getOrPut(event.packageName) { mutableListOf() }.add(event)
        }

        val result = HashMap<String, AppUsage>(byPackage.size)
        for ((pkg, unsorted) in byPackage) {
            val sorted = unsorted.sortedBy { it.timestampMs }
            var launches = 0
            var foregroundMs = 0L
            var lastUsed = 0L
            val hours = IntArray(24)
            var openSince: Long? = null

            for (event in sorted) {
                if (event.isStart) {
                    if (openSince == null) {
                        launches++
                        openSince = event.timestampMs
                        val cal = Calendar.getInstance(zone).apply { timeInMillis = event.timestampMs }
                        hours[cal.get(Calendar.HOUR_OF_DAY)]++
                    }
                } else {
                    openSince?.let { start ->
                        foregroundMs += (event.timestampMs - start).coerceAtLeast(0)
                        openSince = null
                    }
                }
                if (event.timestampMs > lastUsed) lastUsed = event.timestampMs
            }
            // Session still open at the edge of the query window.
            openSince?.let { start -> foregroundMs += (nowMs - start).coerceAtLeast(0) }

            result[pkg] = AppUsage(
                packageName = pkg,
                launchCount = launches,
                lastUsedEpochMs = lastUsed,
                totalForegroundMs = foregroundMs,
                hourHistogram = hours.toList(),
            )
        }
        return result
    }
}
