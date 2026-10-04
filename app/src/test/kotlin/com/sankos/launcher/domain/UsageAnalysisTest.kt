package com.sankos.launcher.domain

import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class UsageAnalysisTest {

    private val zone = TimeZone.getTimeZone("UTC")

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance(zone).apply {
            set(year, month - 1, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    @Test
    fun `aggregates closed sessions into launches and duration`() {
        // 10:00-10:05 and 10:10-10:12 on 2026-10-01
        val s1 = at(2026, 10, 1, 10, 0)
        val e1 = at(2026, 10, 1, 10, 5)
        val s2 = at(2026, 10, 1, 10, 10)
        val e2 = at(2026, 10, 1, 10, 12)
        val events = listOf(
            UsageEvent("com.a", s1, true),
            UsageEvent("com.a", e1, false),
            UsageEvent("com.a", s2, true),
            UsageEvent("com.a", e2, false),
        )
        val result = UsageAnalysis.aggregate(events, nowMs = e2 + 1_000, zone)
        val usage = result.getValue("com.a")
        assertEquals(2, usage.launchCount)
        assertEquals(7 * 60_000L, usage.totalForegroundMs)
        assertEquals(e2, usage.lastUsedEpochMs)
    }

    @Test
    fun `open session at window edge counts until now`() {
        val s1 = at(2026, 10, 1, 22, 50)
        val now = at(2026, 10, 1, 23, 0)
        val events = listOf(UsageEvent("com.b", s1, true))
        val usage = UsageAnalysis.aggregate(events, now, zone).getValue("com.b")
        assertEquals(1, usage.launchCount)
        assertEquals(10 * 60_000L, usage.totalForegroundMs)
    }

    @Test
    fun `duplicate starts do not double count`() {
        val s1 = at(2026, 10, 1, 9, 0)
        val e1 = at(2026, 10, 1, 9, 3)
        val events = listOf(
            UsageEvent("com.c", s1, true),
            UsageEvent("com.c", s1 + 1_000, true),
            UsageEvent("com.c", e1, false),
        )
        val usage = UsageAnalysis.aggregate(events, e1 + 1, zone).getValue("com.c")
        assertEquals(1, usage.launchCount)
        assertEquals(3 * 60_000L, usage.totalForegroundMs)
    }

    @Test
    fun `hour histogram buckets launch hours`() {
        val events = listOf(
            UsageEvent("com.d", at(2026, 10, 1, 8, 15), true),
            UsageEvent("com.d", at(2026, 10, 1, 8, 45), false),
            UsageEvent("com.d", at(2026, 10, 1, 21, 5), true),
            UsageEvent("com.d", at(2026, 10, 1, 21, 20), false),
        )
        val usage = UsageAnalysis.aggregate(events, at(2026, 10, 1, 22, 0), zone).getValue("com.d")
        assertEquals(2, usage.launchCount)
        assertEquals(1, usage.hourHistogram[8])
        assertEquals(1, usage.hourHistogram[21])
        assertEquals(2, usage.hourHistogram.sum())
    }

    @Test
    fun `stray pause without start is ignored`() {
        val e1 = at(2026, 10, 1, 12, 0)
        val events = listOf(UsageEvent("com.e", e1, false))
        val usage = UsageAnalysis.aggregate(events, e1 + 1, zone).getValue("com.e")
        assertEquals(0, usage.launchCount)
        assertEquals(0L, usage.totalForegroundMs)
    }
}
