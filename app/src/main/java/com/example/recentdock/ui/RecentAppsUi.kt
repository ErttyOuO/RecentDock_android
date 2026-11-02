package com.example.recentdock.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.recentdock.data.RecentApp
import com.example.recentdock.data.RecentAppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class AppWithIcon(
    val app: RecentApp,
    val icon: Bitmap,
)

@Composable
internal fun rememberRecentApps(context: Context, limit: Int): List<AppWithIcon> {
    var apps by remember(context, limit) { mutableStateOf(emptyList<AppWithIcon>()) }

    LaunchedEffect(context, limit) {
        apps = withContext(Dispatchers.IO) {
            val repository = RecentAppsRepository(context)
            repository.getRecentApps(limit).mapNotNull { app ->
                runCatching { AppWithIcon(app, repository.loadIconBitmap(app, 144)) }.getOrNull()
            }
        }
    }
    return apps
}

@Composable
internal fun RecentAppsStrip(
    context: Context,
    apps: List<AppWithIcon>,
    limit: Int,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        apps.take(limit).forEach { item ->
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (interactive) {
                            Modifier.clickable {
                                runCatching { context.startActivity(item.app.launchIntent()) }
                            }
                        } else Modifier
                    )
            ) {
                Image(
                    bitmap = item.icon.asImageBitmap(),
                    contentDescription = item.app.label,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}
