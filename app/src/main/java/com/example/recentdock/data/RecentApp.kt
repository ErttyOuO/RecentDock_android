package com.example.recentdock.data

import android.content.ComponentName
import android.content.Intent

data class RecentApp(
    val packageName: String,
    val label: String,
    val componentName: ComponentName,
    val firstInstallTime: Long,
    val lastUpdateTime: Long = 0L,
    val lastUsedTime: Long = 0L,
) {
    fun launchIntent(): Intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
        component = componentName
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    }
}
