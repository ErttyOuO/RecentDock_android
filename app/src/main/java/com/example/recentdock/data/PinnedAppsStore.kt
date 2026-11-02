package com.example.recentdock.data

import android.content.Context

class PinnedAppsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPinnedPackages(): Set<String> = prefs.getStringSet(KEY_PACKAGES, emptySet()).orEmpty().toSet()

    fun toggle(packageName: String): Boolean {
        val updated = getPinnedPackages().toMutableSet()
        val pinned = if (packageName in updated) {
            updated.remove(packageName)
            false
        } else {
            updated.add(packageName)
            true
        }
        prefs.edit().putStringSet(KEY_PACKAGES, updated).apply()
        return pinned
    }

    fun retainInstalled(installedPackages: Set<String>) {
        val current = getPinnedPackages()
        val retained = current.intersect(installedPackages)
        if (retained != current) prefs.edit().putStringSet(KEY_PACKAGES, retained).apply()
    }

    companion object {
        private const val PREFS_NAME = "pinned_apps"
        private const val KEY_PACKAGES = "packages"
    }
}
