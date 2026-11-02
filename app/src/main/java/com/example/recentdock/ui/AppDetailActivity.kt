package com.example.recentdock.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.recentdock.R
import com.example.recentdock.analytics.AnalyticsRepository
import com.example.recentdock.data.AppCategory
import com.example.recentdock.data.AppIconCache
import com.example.recentdock.data.RecentApp
import com.example.recentdock.data.RecentAppsCache
import com.example.recentdock.ui.theme.RecentDockTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class AppDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME).orEmpty()
        setContent { RecentDockTheme { AppDetailScreen(packageName) } }
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "package_name"
    }
}

@Composable
private fun AppDetailScreen(packageName: String) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val app = remember(packageName) {
        RecentAppsCache(appContext).get(AppCategory.RECENTLY_INSTALLED)
            .firstOrNull { it.packageName == packageName }
    }
    val iconCache = remember { AppIconCache(appContext) }
    var icon by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var dockLaunchCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(app) {
        val target = app ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            icon = runCatching { iconCache.load(target, 160) }.getOrNull()
            dockLaunchCount = AnalyticsRepository(appContext).dockLaunchCount(target.packageName)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10161F))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .widthIn(max = 720.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(stringResource(R.string.app_details), style = MaterialTheme.typography.headlineMedium, color = Color(0xFFE4DED3))
        if (app == null) {
            Text(stringResource(R.string.app_details_unavailable), color = Color(0xFF8E98A6))
            return@Column
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            icon?.let { Image(it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(72.dp)) }
            Column {
                Text(app.label, style = MaterialTheme.typography.titleLarge, color = Color(0xFFE4DED3))
                Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8E98A6))
            }
        }
        DetailCard(app, dockLaunchCount)
        Spacer(Modifier.height(4.dp))
        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}")))
        }) { Text(stringResource(R.string.open_android_app_info)) }
    }
}

@Composable
private fun DetailCard(app: RecentApp, dockLaunchCount: Int) {
    val context = LocalContext.current
    val formatter = DateFormat.getMediumDateFormat(context)
    val neverText = stringResource(R.string.never)
    fun date(value: Long): String = if (value > 0L) formatter.format(Date(value)) else neverText
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2430)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailLine(stringResource(R.string.package_name), app.packageName)
            DetailLine(stringResource(R.string.install_time), date(app.firstInstallTime))
            DetailLine(stringResource(R.string.last_opened), date(app.lastUsedTime))
            DetailLine(stringResource(R.string.recentdock_launch_count), dockLaunchCount.toString())
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF8E98A6))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = Color(0xFFE4DED3))
    }
}
