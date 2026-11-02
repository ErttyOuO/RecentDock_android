package com.example.recentdock.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.recentdock.R
import com.example.recentdock.data.RecentAppsCache
import com.example.recentdock.data.RecentAppsRepository
import com.example.recentdock.data.UsageAccess
import com.example.recentdock.ui.theme.RecentDockTheme
import com.example.recentdock.widget.RecentAppsWidget
import kotlinx.coroutines.launch

class UsageAccessActivity : ComponentActivity() {
    private var openedSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecentDockTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    UsageAccessScreen(onOpenSettings = ::openSettings)
                }
            }
        }

        if (!UsageAccess.hasPermission(this)) {
            window.decorView.post { openSettings() }
        }
    }

    override fun onResume() {
        super.onResume()
        if (openedSettings && UsageAccess.hasPermission(this)) {
            lifecycleScope.launch {
                val cache = RecentAppsCache(this@UsageAccessActivity)
                RecentAppsRepository(this@UsageAccessActivity).refreshCache(cache)
                RecentAppsWidget().updateAll(this@UsageAccessActivity)
                finish()
            }
        }
    }

    private fun openSettings() {
        openedSettings = true
        runCatching { startActivity(UsageAccess.settingsIntent()) }
    }
}

@Composable
private fun UsageAccessScreen(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = stringResource(R.string.usage_access_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.usage_access_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onOpenSettings,
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(stringResource(R.string.usage_access_button))
        }
    }
}
