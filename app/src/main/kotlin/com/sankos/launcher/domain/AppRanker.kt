package com.sankos.launcher.domain

import com.sankos.launcher.data.model.AppCategory
import com.sankos.launcher.data.model.AppEntry
import com.sankos.launcher.data.model.AppUsage
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.min

/** An app with its current ranking score (higher = more prominent). */
data class RankedApp(val app: AppEntry, val score: Double)

/**
 * The adaptive drawer ranking.
 *
 * Design goals, in order:
 *  1. Intentional access stays fast: apps you genuinely rely on rise.
 *  2. Frequent is not the same as habitual: distraction-category apps are
 *     damped, so opening Instagram twenty times a day cannot buy prime
 *     drawer placement the way real work apps can.
 *  3. Explainable and local: three transparent signals (recency decay,
 *     log-scaled frequency, log-scaled duration) with a time-of-day
 *     affinity nudged by the hour histogram. No network, no profiles.
 *
 * The score is not gamified or displayed anywhere as a number; it only
 * orders the list.
 */
object AppRanker {

    /** How much raw usage we trust per category. Distraction is heavily damped. */
    private val categoryWeight: Map<AppCategory, Double> = mapOf(
        AppCategory.TOOL to 1.0,
        AppCategory.PRODUCTIVITY to 1.05,
        AppCategory.NAVIGATION to 0.95,
        AppCategory.MUSIC to 1.0,
        AppCategory.COMMUNICATION to 0.9,
        AppCategory.UNKNOWN to 0.8,
        AppCategory.ENTERTAINMENT to 0.7,
        AppCategory.DISTRACTION to 0.5,
    )

    /** Never-used apps get a neutral floor: reachable, never promoted. */
    const val NEUTRAL_SCORE = 0.05

    fun rank(
        apps: List<AppEntry>,
        usage: Map<String, AppUsage>,
        nowMs: Long,
        zone: TimeZone = TimeZone.getDefault(),
        pinned: Set<String> = emptySet(),
        hidden: Set<String> = emptySet(),
    ): List<RankedApp> = apps.asSequence()
        .filter { it.packageName !in hidden }
        .map { app ->
            RankedApp(
                app = app,
                score = scoreOf(
                    app = app,
                    usage = usage[app.packageName],
                    nowMs = nowMs,
                    zone = zone,
                    pinned = app.packageName in pinned,
                ),
            )
        }
        .sortedByDescending { it.score }
        .toList()

    fun scoreOf(
        app: AppEntry,
        usage: AppUsage?,
        nowMs: Long,
        zone: TimeZone = TimeZone.getDefault(),
        pinned: Boolean = false,
    ): Double {
        var score = if (usage == null) {
            NEUTRAL_SCORE
        } else {
            val hoursSinceUse = ((nowMs - usage.lastUsedEpochMs).coerceAtLeast(0)) / 3_600_000.0
            val recency = exp(-hoursSinceUse / 36.0)                          // half-life ≈ 25h
            val frequency = ln(1.0 + usage.launchCount) / ln(1.0 + 120.0)     // saturates ~120 launches
            val duration = ln(1.0 + usage.totalForegroundMs / 60_000.0) / ln(1.0 + 240.0)
            val category = categoryWeight[app.category] ?: 0.8
            val timeOfDay = timeOfDayAffinity(usage, nowMs, zone)
            (0.45 * recency + 0.35 * frequency + 0.20 * duration) * category * timeOfDay
        }
        if (pinned) score += PIN_BOOST
        return score
    }

    /**
     * Gentle boost when the user historically opens this app around the
     * current hour. Capped so a single hour-burst cannot dominate.
     */
    private fun timeOfDayAffinity(usage: AppUsage, nowMs: Long, zone: TimeZone): Double {
        val total = usage.hourHistogram.sum()
        if (total < 5) return 1.0
        val cal = Calendar.getInstance(zone).apply { timeInMillis = nowMs }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val shareAtHour = usage.hourHistogram.getOrElse(hour) { 0 }.toDouble() / total
        return 1.0 + min(shareAtHour, 0.5)
    }

    /** Boost applied to user-pinned apps, dwarfing behavioral scores. */
    const val PIN_BOOST = 100.0

    /** Minimum score for an app to count as "frequent" in the drawer UI. */
    const val FREQUENT_THRESHOLD = 0.12
}
