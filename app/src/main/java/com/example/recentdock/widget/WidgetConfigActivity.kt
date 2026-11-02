package com.example.recentdock.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import com.example.recentdock.R
import com.example.recentdock.data.WidgetPreferences
import com.example.recentdock.ui.RecentAppsStrip
import com.example.recentdock.ui.rememberRecentApps
import com.example.recentdock.ui.theme.RecentDockTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class WidgetConfigActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setResult(Activity.RESULT_CANCELED)
        appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            RecentDockTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WidgetConfigScreen(
                        onCancel = { finish() },
                        onSave = { count -> saveAndFinish(count) },
                    )
                }
            }
        }
    }

    private fun saveAndFinish(count: Int) {
        WidgetPreferences(this).displayCount = count
        lifecycleScope.launch {
            RecentAppsWidget().update(this@WidgetConfigActivity, appWidgetId)
            val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }
}


@Composable
private fun WidgetConfigScreen(
    onCancel: () -> Unit,
    onSave: (Int) -> Unit,
) {
    val context = LocalContext.current
    val preferences = remember { WidgetPreferences(context) }
    var count by remember { mutableFloatStateOf(preferences.displayCount.toFloat()) }
    val apps = rememberRecentApps(context, 10)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.config_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.display_count_help),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.widget_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    Text("↻   ›", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val previewCount = minOf(count.roundToInt(), 10)
                if (apps.isEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(if (previewCount > 5) 2 else 1) { rowIndex ->
                            val rowItems = if (rowIndex == 0) minOf(previewCount, 5) else previewCount - 5
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                repeat(rowItems.coerceAtLeast(1)) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RecentAppsStrip(
                            context = context,
                            apps = apps.take(5),
                            limit = minOf(previewCount, 5),
                            interactive = false,
                        )
                        if (previewCount > 5 && apps.size > 5) {
                            RecentAppsStrip(
                                context = context,
                                apps = apps.drop(5).take(5),
                                limit = minOf(previewCount - 5, 5),
                                interactive = false,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.display_count),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = count.roundToInt().toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Slider(
                value = count,
                onValueChange = { count = it },
                valueRange = 5f..15f,
                steps = 9,
            )

            Button(
                onClick = { onSave(count.roundToInt()) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 15.dp),
            ) {
                Text(stringResource(R.string.config_done))
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 15.dp),
            ) {
                Text(stringResource(R.string.config_cancel))
            }
        }
    }
}
