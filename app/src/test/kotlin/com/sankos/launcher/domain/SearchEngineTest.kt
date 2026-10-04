package com.sankos.launcher.domain

import com.sankos.launcher.data.model.AppCategory
import com.sankos.launcher.data.model.AppEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchEngineTest {

    private val apps = listOf(
        AppEntry("com.spotify.music", "Spotify", AppCategory.MUSIC),
        AppEntry("com.android.settings", "Settings", AppCategory.TOOL),
        AppEntry("com.google.android.maps", "Maps", AppCategory.NAVIGATION),
    )

    @Test
    fun `empty query returns nothing`() {
        assertTrue(SearchEngine.search("  ", apps).isEmpty())
    }

    @Test
    fun `timer prefix parses minutes`() {
        val results = SearchEngine.search("timer 25", apps)
        val action = results.filterIsInstance<SearchResult.QuickAction>()
            .first { it.kind == ActionKind.TIMER }
        assertEquals("25", action.payload)
        assertTrue(action.title.contains("25"))
    }

    @Test
    fun `timer suffix parses`() {
        val results = SearchEngine.search("25 min timer", apps)
        assertTrue(
            results.filterIsInstance<SearchResult.QuickAction>()
                .any { it.kind == ActionKind.TIMER && it.payload == "25" },
        )
    }

    @Test
    fun `hours convert to minutes`() {
        val results = SearchEngine.search("timer 1h", apps)
        val action = results.filterIsInstance<SearchResult.QuickAction>()
            .first { it.kind == ActionKind.TIMER }
        assertEquals("60", action.payload)
    }

    @Test
    fun `timer rejects out of range`() {
        val results = SearchEngine.search("timer 5000", apps)
        assertTrue(
            results.filterIsInstance<SearchResult.QuickAction>()
                .none { it.kind == ActionKind.TIMER },
        )
    }

    @Test
    fun `call with digits produces dial action`() {
        val results = SearchEngine.search("call 9876543210", apps)
        val dial = results.filterIsInstance<SearchResult.QuickAction>()
            .first { it.kind == ActionKind.DIAL }
        assertEquals("9876543210", dial.payload)
    }

    @Test
    fun `bare number produces dial action`() {
        val results = SearchEngine.search("1800-100", apps)
        assertTrue(
            results.filterIsInstance<SearchResult.QuickAction>()
                .any { it.kind == ActionKind.DIAL },
        )
    }

    @Test
    fun `settings query surfaces sank settings action`() {
        val results = SearchEngine.search("settings", apps)
        assertTrue(
            results.filterIsInstance<SearchResult.QuickAction>()
                .any { it.kind == ActionKind.SANK_SETTINGS },
        )
        // system Settings app should also match
        assertTrue(
            results.filterIsInstance<SearchResult.AppHit>()
                .any { it.app.packageName == "com.android.settings" },
        )
    }

    @Test
    fun `prefix match beats substring match`() {
        val results = SearchEngine.search("spo", apps).filterIsInstance<SearchResult.AppHit>()
        assertEquals("com.spotify.music", results.first().app.packageName)
        assertTrue(results.first().matchScore >= 3.0)
    }

    @Test
    fun `subsequence match still finds apps`() {
        val results = SearchEngine.search("sptf", apps).filterIsInstance<SearchResult.AppHit>()
        assertEquals("com.spotify.music", results.first().app.packageName)
    }
}
