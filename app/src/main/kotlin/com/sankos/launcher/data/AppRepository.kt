package com.sankos.launcher.data

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import com.sankos.launcher.data.model.AppCategory
import com.sankos.launcher.data.model.AppEntry

/**
 * Source of launchable applications and dock targets.
 *
 * Uses [LauncherApps] (the launcher-correct API) and resolves the dock
 * through standard intents so OEM dialer/messaging/camera apps are picked.
 */
class AppRepository(private val context: Context) {

    data class LoadedApps(
        val apps: List<AppEntry>,
        val icons: Map<String, Drawable>,
    )

    data class DockTarget(val entry: AppEntry, val icon: Drawable?)

    fun loadApps(): LoadedApps {
        val launcherApps = context.getSystemService(LauncherApps::class.java)
            ?: return LoadedApps(emptyList(), emptyMap())
        val profile = launcherApps.profiles.firstOrNull()
            ?: return LoadedApps(emptyList(), emptyMap())

        val self = context.packageName
        val apps = ArrayList<AppEntry>(64)
        val icons = HashMap<String, Drawable>(64)
        val seen = HashSet<String>()

        for (activity in launcherApps.getActivityList(null, profile)) {
            val pkg = activity.componentName.packageName
            if (pkg == self || !seen.add(pkg)) continue
            val label = runCatching { activity.label?.toString().orEmpty() }.getOrDefault("")
            if (label.isBlank()) continue
            runCatching { activity.getIcon(0) }.getOrNull()?.let { icons[pkg] = it }
            apps.add(AppEntry(packageName = pkg, label = label, category = categorize(pkg)))
        }
        return LoadedApps(apps.sortedBy { it.label.lowercase() }, icons)
    }

    /** Phone, messages, camera — resolved against the device's own handlers. */
    fun resolveDockApps(): List<DockTarget> {
        val pm = context.packageManager
        val intents = listOf(
            Intent(Intent.ACTION_DIAL),
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING),
            Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
        )
        return intents.mapNotNull { intent ->
            val resolved = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                ?: return@mapNotNull null
            val pkg = resolved.activityInfo?.packageName ?: return@mapNotNull null
            // The implicit-resolution fallback reports package "android"; skip it.
            if (pkg == "android") return@mapNotNull null
            val label = runCatching { resolved.loadLabel(pm).toString() }.getOrDefault("").ifBlank { pkg }
            DockTarget(
                entry = AppEntry(packageName = pkg, label = label, category = categorize(pkg)),
                icon = runCatching { resolved.loadIcon(pm) }.getOrNull(),
            )
        }
    }

    fun launchIntentFor(packageName: String): Intent? =
        context.packageManager.getLaunchIntentForPackage(packageName)?.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK,
        )

    companion object {
        /**
         * v0.1 category heuristic. Package-name based, coarse by design —
         * good enough to damp habitual apps; user overrides arrive in v0.2.
         */
        fun categorize(packageName: String): AppCategory {
            val p = packageName.lowercase()
            return when {
                containsAny(p, "dialer", "telecom", "phone", "messages", "messaging", "whatsapp",
                    "telegram", "signal", "discord", "slack", "gmail", "mail", "sms") ->
                    AppCategory.COMMUNICATION
                containsAny(p, "maps", "waze", "citymapper", "uber", "lyft", "ola") ->
                    AppCategory.NAVIGATION
                containsAny(p, "spotify", "music", "soundcloud", "podcast", "audible", "ytmusic") ->
                    AppCategory.MUSIC
                containsAny(p, "instagram", "tiktok", "snapchat", "facebook", "reddit", "twitter",
                    "threads", "pinterest", "linkedin", "xcorp") ->
                    AppCategory.DISTRACTION
                containsAny(p, "youtube", "netflix", "primevideo", "hotstar", "hulu", "twitch",
                    "sonyliv", "zee5", "jiocinema") ->
                    AppCategory.ENTERTAINMENT
                containsAny(p, "calendar", "notes", "keep", "todoist", "notion", "docs", "office",
                    "drive", "tasks", "obsidian") ->
                    AppCategory.PRODUCTIVITY
                else -> AppCategory.TOOL
            }
        }

        fun containsAny(target: String, vararg needles: String): Boolean =
            needles.any { target.contains(it) }

        /** Uri builder helper kept here so callers do not import android.net. */
        fun telUri(number: String): Uri = Uri.parse("tel:$number")
    }
}
