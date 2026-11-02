package com.example.recentdock.analytics

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle

class AnalyticsLaunchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = intent.getStringExtra(EXTRA_COMPONENT)?.let(ComponentName::unflattenFromString)
        if (component == null) {
            finish()
            return
        }

        if (AnalyticsPreferences(this).enabled) {
            runCatching { AnalyticsRepository(this).recordDockLaunch(component.packageName, SOURCE_WIDGET) }
        }

        runCatching {
            startActivity(Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                this.component = component
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            })
        }
        finish()
        overridePendingTransition(0, 0)
    }

    companion object {
        const val EXTRA_COMPONENT = "target_component"
        const val SOURCE_WIDGET = "widget"
    }
}
