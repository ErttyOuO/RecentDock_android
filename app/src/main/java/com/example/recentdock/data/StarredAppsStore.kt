package com.example.recentdock.data

import android.content.Context

class StarredAppsStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getStarredPackages(): Set<String> =
        preferences.getStringSet(KEY_PACKAGES, emptySet())?.toSet().orEmpty()

    fun isStarred(packageName: String): Boolean = packageName in getStarredPackages()

    fun setStarred(packageName: String, starred: Boolean) {
        val updated = getStarredPackages().toMutableSet().apply {
            if (starred) add(packageName) else remove(packageName)
        }
        preferences.edit().putStringSet(KEY_PACKAGES, updated).apply()
    }

    fun toggle(packageName: String): Boolean {
        val newValue = !isStarred(packageName)
        setStarred(packageName, newValue)
        return newValue
    }

    fun retainInstalled(installedPackages: Set<String>) {
        val current = getStarredPackages()
        val retained = current.intersect(installedPackages)
        if (retained != current) preferences.edit().putStringSet(KEY_PACKAGES, retained).apply()
    }

    companion object {
        private const val PREFS_NAME = "recent_dock_starred_apps"
        private const val KEY_PACKAGES = "packages"
    }
}
