package com.sankos.launcher.domain

import com.sankos.launcher.data.model.AppEntry

/** One row of drawer search output. */
sealed interface SearchResult {
    data class AppHit(val app: AppEntry, val matchScore: Double) : SearchResult
    data class QuickAction(
        val kind: ActionKind,
        val title: String,
        val subtitle: String,
        val payload: String,
    ) : SearchResult
}

enum class ActionKind { TIMER, DIAL, SANK_SETTINGS }

/**
 * The drawer doubles as a command interface.
 *
 * v0.1 supports genuinely useful, zero-permission actions:
 *  - `timer 25`, `25 min timer`, `1h` style countdowns → system timer
 *  - `call <number>` / bare numbers → dialer with number pre-filled
 *  - `settings` / `sank` → SankOS settings
 * plus fast app-name matching. Contact-name calling arrives with the
 * contacts integration in v0.2.
 */
object SearchEngine {

    private val TIMER_PREFIX = Regex("^timer\\s+(\\d{1,4})\\s*(h|hr|hour|hours|m|min|mins|minutes)?$")
    private val TIMER_SUFFIX = Regex("^(\\d{1,4})\\s*(h|hr|hour|hours|m|min|mins|minutes)?\\s*timer$")
    private val BARE_DURATION = Regex("^(\\d{1,4})\\s*(h|hr|hour|hours|m|min|mins|minutes)$")
    private val PHONE_NUMBER = Regex("^[+\\d][\\d\\s()\\-]{2,}$")

    fun search(query: String, apps: List<AppEntry>): List<SearchResult> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()

        val results = ArrayList<SearchResult>(8)

        // --- quick actions -------------------------------------------------
        parseTimerMinutes(q)?.let { minutes ->
            results.add(
                SearchResult.QuickAction(
                    kind = ActionKind.TIMER,
                    title = "$minutes minute timer",
                    subtitle = "START TIMER",
                    payload = minutes.toString(),
                ),
            )
        }
        if (PHONE_NUMBER.matches(q.replace("call", "").trim()) || PHONE_NUMBER.matches(q)) {
            val number = if (q.startsWith("call")) q.removePrefix("call").trim() else q
            results.add(
                SearchResult.QuickAction(
                    kind = ActionKind.DIAL,
                    title = "Call $number",
                    subtitle = "OPEN DIALER",
                    payload = number,
                ),
            )
        }
        if (q == "settings" || q == "sank" || q == "sankos" || q == "sank settings") {
            results.add(
                SearchResult.QuickAction(
                    kind = ActionKind.SANK_SETTINGS,
                    title = "SankOS settings",
                    subtitle = "SANKOS",
                    payload = "",
                ),
            )
        }

        // --- app matches ---------------------------------------------------
        val appHits = apps.asSequence()
            .mapNotNull { app -> matchScore(app.label, q)?.let { SearchResult.AppHit(app, it) } }
            .sortedWith(compareByDescending<SearchResult.AppHit> { it.matchScore }.thenBy { it.app.label.lowercase() })
            .toList()

        return results + appHits
    }

    /** Returns a positive score for a match, or null. Higher = better match. */
    private fun matchScore(label: String, query: String): Double? {
        val l = label.lowercase()
        return when {
            l.startsWith(query) -> 3.0
            l.split(' ', '-', '_').any { it.startsWith(query) } -> 2.0
            l.contains(query) -> 1.0
            isSubsequence(query, l) -> 0.5
            else -> null
        }
    }

    private fun isSubsequence(query: String, target: String): Boolean {
        if (query.length < 2) return false
        var i = 0
        for (ch in target) {
            if (i == query.length) return true
            if (ch == query[i]) i++
        }
        return i == query.length
    }

    private fun parseTimerMinutes(q: String): Int? {
        val match = TIMER_PREFIX.matchEntire(q)
            ?: TIMER_SUFFIX.matchEntire(q)
            ?: BARE_DURATION.matchEntire(q)
            ?: return null
        val amount = match.groupValues[1].toIntOrNull() ?: return null
        val unit = match.groupValues[2].lowercase()
        val minutes = when {
            unit.startsWith("h") -> amount * 60
            unit.startsWith("m") || unit.isEmpty() -> amount
            else -> return null
        }
        return if (minutes in 1..1440) minutes else null
    }
}
