package com.sankos.launcher.data.model

/**
 * Behavioral category used by the ranking engine to separate *useful
 * frequency* from *habitual frequency*. v0.1 assigns categories with a
 * package-name heuristic; later phases let the user override them.
 */
enum class AppCategory {
    TOOL,
    COMMUNICATION,
    PRODUCTIVITY,
    NAVIGATION,
    MUSIC,
    ENTERTAINMENT,
    DISTRACTION,
    UNKNOWN,
}

/** One launchable app. Icons are kept separately as platform drawables. */
data class AppEntry(
    val packageName: String,
    val label: String,
    val category: AppCategory = AppCategory.TOOL,
)

/**
 * Aggregated local usage signals for one app over the analysis window.
 * Computed on-device by [com.sankos.launcher.domain.UsageAnalysis]; never
 * leaves the device.
 */
data class AppUsage(
    val packageName: String,
    val launchCount: Int,
    val lastUsedEpochMs: Long,
    val totalForegroundMs: Long,
    /** launches per hour-of-day, 24 buckets, used for time-of-day affinity */
    val hourHistogram: List<Int>,
)
