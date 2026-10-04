package com.sankos.launcher.domain

import com.sankos.launcher.data.model.AppCategory
import com.sankos.launcher.data.model.AppEntry
import com.sankos.launcher.data.model.AppUsage
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRankerTest {

    private val zone = TimeZone.getTimeZone("UTC")
    private val now = 1_700_000_000_000L

    private fun usage(pkg: String, launches: Int, lastUsedMs: Long, foregroundMs: Long): AppUsage =
        AppUsage(
            packageName = pkg,
            launchCount = launches,
            lastUsedEpochMs = lastUsedMs,
            totalForegroundMs = foregroundMs,
            hourHistogram = List(24) { 0 },
        )

    @Test
    fun `frequent tool outranks equally-used distraction`() {
        val tool = AppEntry("com.example.notes", "Notes", AppCategory.TOOL)
        val distraction = AppEntry("com.instagram.android", "Instagram", AppCategory.DISTRACTION)
        val usage = mapOf(
            "com.example.notes" to usage("com.example.notes", 50, now - 3_600_000, 1_800_000),
            "com.instagram.android" to usage("com.instagram.android", 50, now - 3_600_000, 1_800_000),
        )
        val ranked = AppRanker.rank(listOf(distraction, tool), usage, now, zone)
        assertEquals("com.example.notes", ranked.first().app.packageName)
    }

    @Test
    fun `recency dominates between identical categories`() {
        val a = AppEntry("com.a", "A", AppCategory.TOOL)
        val b = AppEntry("com.b", "B", AppCategory.TOOL)
        val usage = mapOf(
            "com.a" to usage("com.a", 10, now - 30_000, 300_000),
            "com.b" to usage("com.b", 10, now - 86_400_000L * 10, 300_000),
        )
        val ranked = AppRanker.rank(listOf(b, a), usage, now, zone)
        assertEquals("com.a", ranked.first().app.packageName)
    }

    @Test
    fun `never used apps get neutral score and never beat used ones`() {
        val used = AppEntry("com.used", "Used", AppCategory.TOOL)
        val fresh = AppEntry("com.fresh", "Fresh", AppCategory.PRODUCTIVITY)
        val ranked = AppRanker.rank(listOf(fresh, used), mapOf("com.used" to usage("com.used", 1, now - 60_000, 30_000)), now, zone)
        assertEquals("com.used", ranked.first().app.packageName)
        assertEquals(AppRanker.NEUTRAL_SCORE, ranked.last().score, 1e-9)
    }

    @Test
    fun `pinned app always ranks first`() {
        val a = AppEntry("com.a", "A", AppCategory.TOOL)
        val pinned = AppEntry("com.pinned", "Pinned", AppCategory.TOOL)
        val usage = mapOf(
            "com.a" to usage("com.a", 120, now - 1_000, 3_600_000),
            "com.pinned" to usage("com.pinned", 1, now - 86_400_000L * 30, 10_000),
        )
        val ranked = AppRanker.rank(listOf(a, pinned), usage, now, zone, pinned = setOf("com.pinned"))
        assertEquals("com.pinned", ranked.first().app.packageName)
    }

    @Test
    fun `hidden apps are excluded`() {
        val a = AppEntry("com.a", "A", AppCategory.TOOL)
        val b = AppEntry("com.b", "B", AppCategory.TOOL)
        val ranked = AppRanker.rank(listOf(a, b), emptyMap(), now, zone, hidden = setOf("com.b"))
        assertEquals(listOf("com.a"), ranked.map { it.app.packageName })
    }

    @Test
    fun `time of day affinity boosts hour-concentrated apps`() {
        // histogram: 5 launches, all at hour 10
        val hist = AppUsage("com.c", 5, now - 60_000, 100_000, List(24) { h -> if (h == 10) 1 else 0 })
        val app = AppEntry("com.c", "C", AppCategory.TOOL)

        val cal = java.util.Calendar.getInstance(zone).apply { timeInMillis = now; set(java.util.Calendar.HOUR_OF_DAY, 10) }
        val atTen = AppRanker.scoreOf(app, hist, cal.timeInMillis, zone)
        val cal2 = java.util.Calendar.getInstance(zone).apply { timeInMillis = now; set(java.util.Calendar.HOUR_OF_DAY, 23) }
        val atElevenPm = AppRanker.scoreOf(app, hist, cal2.timeInMillis, zone)

        assertTrue("expected boost at hour 10 ($atTen vs $atElevenPm)", atTen > atElevenPm)
    }
}
