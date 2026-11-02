package com.example.recentdock.analytics

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.recentdock.R
import com.example.recentdock.data.AppIconCache
import com.example.recentdock.data.AppCategory
import com.example.recentdock.data.RecentApp
import com.example.recentdock.data.RecentAppsCache
import com.example.recentdock.ui.theme.RecentDockTheme
import com.example.recentdock.widget.RecentAppsWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.math.roundToInt

class AnalyticsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RecentDockTheme { AnalyticsScreen() } }
    }
}

@Composable
private fun AnalyticsScreen() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val prefs = remember { AnalyticsPreferences(appContext) }
    val repository = remember { AnalyticsRepository(appContext) }
    val iconCache = remember(appContext) { AppIconCache(appContext) }
    var enabled by remember { mutableStateOf(prefs.enabled) }
    var period by remember { mutableStateOf(AnalyticsPeriod.DAY) }
    var snapshot by remember { mutableStateOf<AnalyticsSnapshot?>(null) }
    var timeline by remember { mutableStateOf<List<AnalyticsTimelinePoint>>(emptyList()) }
    var selectedTimelineIndex by remember { mutableIntStateOf(-1) }
    var committedTimelineIndex by remember { mutableIntStateOf(-1) }
    var reloadTick by remember { mutableIntStateOf(0) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun setEnabled(value: Boolean) {
        enabled = value
        prefs.enabled = value
        // Never backfill the period while analytics was disabled.
        if (value) prefs.lastUsageIngestAt = System.currentTimeMillis()
        AnalyticsScheduler.applyEnabledState(appContext)
        if (value) {
            scope.launch(Dispatchers.IO) {
                runCatching { UsageEventsIngestor(appContext).ingestNow() }
                withContext(Dispatchers.Main) { reloadTick++ }
            }
        } else {
            snapshot = null
            timeline = emptyList()
            selectedTimelineIndex = -1
            committedTimelineIndex = -1
        }
        scope.launch { runCatching { RecentAppsWidget().updateAll(appContext) } }
    }

    fun selectPeriod(newPeriod: AnalyticsPeriod) {
        if (period == newPeriod) return
        period = newPeriod
        snapshot = null
        timeline = emptyList()
        selectedTimelineIndex = -1
        committedTimelineIndex = -1
    }

    LaunchedEffect(enabled, period, reloadTick) {
        if (!enabled) return@LaunchedEffect
        val points = withContext(Dispatchers.IO) { repository.timeline(period) }
        timeline = points
        selectedTimelineIndex = points.lastIndex
        committedTimelineIndex = points.lastIndex
    }

    // Dragging only updates the chart preview. Database work starts after the gesture commits.
    LaunchedEffect(enabled, period, committedTimelineIndex, timeline, reloadTick) {
        val point = timeline.getOrNull(committedTimelineIndex)
        snapshot = if (enabled && point != null) {
            withContext(Dispatchers.IO) { repository.snapshot(point.range) }
        } else if (!enabled) {
            null
        } else {
            snapshot
        }
    }

    Box(
        Modifier.fillMaxSize().background(Color(0xFF10161F)),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.analytics_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFE4DED3),
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2430)),
                        shape = RoundedCornerShape(22.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.analytics_enable), color = Color(0xFFE4DED3))
                                Text(
                                    stringResource(R.string.analytics_enable_description),
                                    color = Color(0xFF8E98A6),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Switch(checked = enabled, onCheckedChange = ::setEnabled)
                        }
                    }
                    Button(onClick = { showClearConfirmation = true }) {
                        Text(stringResource(R.string.analytics_clear_data))
                    }
                    if (enabled) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PeriodButton(period == AnalyticsPeriod.DAY, R.string.analytics_day) {
                                selectPeriod(AnalyticsPeriod.DAY)
                            }
                            PeriodButton(period == AnalyticsPeriod.WEEK, R.string.analytics_week) {
                                selectPeriod(AnalyticsPeriod.WEEK)
                            }
                            PeriodButton(period == AnalyticsPeriod.MONTH, R.string.analytics_month) {
                                selectPeriod(AnalyticsPeriod.MONTH)
                            }
                        }

                        if (timeline.isNotEmpty()) {
                            TimelineCard(
                                period = period,
                                points = timeline,
                                selectedIndex = selectedTimelineIndex.coerceIn(0, timeline.lastIndex),
                                onPreview = { selectedTimelineIndex = it },
                                onCommit = {
                                    selectedTimelineIndex = it
                                    committedTimelineIndex = it
                                },
                            )
                        }

                        snapshot?.let { data ->
                            val started = if (data.startedAt > 0L) {
                                DateFormat.getMediumDateFormat(context).format(Date(data.startedAt))
                            } else {
                                "—"
                            }
                            Text(stringResource(R.string.analytics_since, started), color = Color(0xFF8E98A6))
                            StatsCard(
                                stringResource(R.string.analytics_installed_count),
                                data.installedInPeriod.toString(),
                            )
                            StatsCard(
                                stringResource(R.string.analytics_total_apps),
                                data.totalLaunchableApps.toString(),
                            )
                        }
                    }
                }
            }

            if (enabled) {
                snapshot?.let { data ->
                    val appsByPackage = RecentAppsCache(appContext)
                        .get(AppCategory.RECENTLY_INSTALLED)
                        .associateBy { it.packageName }
                    item { SectionHeader(stringResource(R.string.analytics_usage_ranking)) }
                    if (data.usageRanking.isEmpty()) item { EmptyAnalyticsRow() }
                    items(data.usageRanking.take(20), key = { "usage:${it.packageName}" }) {
                        AnalyticsRankingRow(it, appsByPackage[it.packageName], iconCache)
                    }
                    item { SectionHeader(stringResource(R.string.analytics_recentdock_ranking)) }
                    if (data.dockRanking.isEmpty()) item { EmptyAnalyticsRow() }
                    items(data.dockRanking.take(20), key = { "dock:${it.packageName}" }) {
                        AnalyticsRankingRow(it, appsByPackage[it.packageName], iconCache)
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text(stringResource(R.string.analytics_clear_confirm_title)) },
            text = { Text(stringResource(R.string.analytics_clear_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirmation = false
                    scope.launch {
                        withContext(Dispatchers.IO) { repository.clearAnalyticsData() }
                        reloadTick++
                    }
                }) { Text(stringResource(R.string.analytics_clear_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun TimelineCard(
    period: AnalyticsPeriod,
    points: List<AnalyticsTimelinePoint>,
    selectedIndex: Int,
    onPreview: (Int) -> Unit,
    onCommit: (Int) -> Unit,
) {
    val context = LocalContext.current
    val selected = points.getOrNull(selectedIndex) ?: return
    var dragPreviewIndex by remember(points) { mutableIntStateOf(selectedIndex) }
    val primaryColor = Color(0xFFD8C2A2)
    val secondaryColor = Color(0xFF8EA8C8)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171E28)),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.analytics_timeline),
                    color = Color(0xFFE4DED3),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    formatRange(context, selected.range, period),
                    color = Color(0xFFB7C0CC),
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Text(
                stringResource(R.string.analytics_timeline_hint),
                color = Color(0xFF8E98A6),
                style = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem(primaryColor, stringResource(R.string.analytics_app_opens))
                LegendItem(secondaryColor, stringResource(R.string.analytics_recentdock_opens))
            }

            Text(
                stringResource(
                    R.string.analytics_timeline_summary,
                    selected.usageCount,
                    selected.dockCount,
                ),
                color = Color(0xFFE4DED3),
                style = MaterialTheme.typography.bodyMedium,
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(156.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF121923))
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val index = timelineIndexForX(offset.x, size.width.toFloat(), points.size)
                            dragPreviewIndex = index
                            onPreview(index)
                            onCommit(index)
                        }
                    }
                    .pointerInput(points) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val index = timelineIndexForX(offset.x, size.width.toFloat(), points.size)
                                dragPreviewIndex = index
                                onPreview(index)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val index = timelineIndexForX(
                                    change.position.x,
                                    size.width.toFloat(),
                                    points.size,
                                )
                                if (index != dragPreviewIndex) {
                                    dragPreviewIndex = index
                                    onPreview(index)
                                }
                            },
                            onDragEnd = { onCommit(dragPreviewIndex) },
                            onDragCancel = { onCommit(dragPreviewIndex) },
                        )
                    },
            ) {
                val horizontalPadding = 14.dp.toPx()
                val verticalPadding = 14.dp.toPx()
                val plotWidth = (size.width - horizontalPadding * 2f).coerceAtLeast(1f)
                val plotHeight = (size.height - verticalPadding * 2f).coerceAtLeast(1f)
                val maxValue = points.maxOfOrNull { maxOf(it.usageCount, it.dockCount) }
                    ?.coerceAtLeast(1) ?: 1

                fun xFor(index: Int): Float = if (points.size <= 1) {
                    horizontalPadding + plotWidth / 2f
                } else {
                    horizontalPadding + (index.toFloat() / (points.lastIndex).toFloat()) * plotWidth
                }

                fun yFor(value: Int): Float =
                    verticalPadding + plotHeight - (value.toFloat() / maxValue.toFloat()) * plotHeight

                repeat(4) { gridIndex ->
                    val y = verticalPadding + (gridIndex.toFloat() / 3f) * plotHeight
                    drawLine(
                        color = Color.White.copy(alpha = 0.07f),
                        start = Offset(horizontalPadding, y),
                        end = Offset(horizontalPadding + plotWidth, y),
                        strokeWidth = 1.dp.toPx(),
                    )
                }

                fun drawSeries(color: Color, value: (AnalyticsTimelinePoint) -> Int) {
                    if (points.isEmpty()) return
                    val path = Path()
                    points.forEachIndexed { index, point ->
                        val x = xFor(index)
                        val y = yFor(value(point))
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }

                drawSeries(primaryColor) { it.usageCount }
                drawSeries(secondaryColor) { it.dockCount }

                val selectedX = xFor(selectedIndex)
                drawLine(
                    color = Color.White.copy(alpha = 0.30f),
                    start = Offset(selectedX, verticalPadding),
                    end = Offset(selectedX, verticalPadding + plotHeight),
                    strokeWidth = 1.dp.toPx(),
                )
                drawCircle(primaryColor, 4.dp.toPx(), Offset(selectedX, yFor(selected.usageCount)))
                drawCircle(secondaryColor, 4.dp.toPx(), Offset(selectedX, yFor(selected.dockCount)))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    formatRangeStart(context, points.first().range),
                    color = Color(0xFF778290),
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    formatRangeStart(context, points.last().range),
                    color = Color(0xFF778290),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(Modifier.size(8.dp)) {
            drawCircle(color = color, radius = size.minDimension / 2f)
        }
        Text(text, color = Color(0xFF9EA8B5), style = MaterialTheme.typography.labelSmall)
    }
}

private fun timelineIndexForX(x: Float, width: Float, count: Int): Int {
    if (count <= 1 || width <= 0f) return 0
    return ((x.coerceIn(0f, width) / width) * (count - 1)).roundToInt().coerceIn(0, count - 1)
}

private fun formatRange(context: Context, range: TimeRange, period: AnalyticsPeriod): String {
    val format = DateFormat.getMediumDateFormat(context)
    val start = format.format(Date(range.startInclusive))
    if (period == AnalyticsPeriod.DAY) return start
    val end = format.format(Date((range.endExclusive - 1L).coerceAtLeast(range.startInclusive)))
    return "$start – $end"
}

private fun formatRangeStart(context: Context, range: TimeRange): String =
    DateFormat.getMediumDateFormat(context).format(Date(range.startInclusive))

@Composable
private fun PeriodButton(selected: Boolean, label: Int, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = !selected) { Text(stringResource(label)) }
}

@Composable
private fun StatsCard(label: String, value: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF171E28))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color(0xFF8E98A6))
            Text(value, color = Color(0xFFE4DED3))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        color = Color(0xFFE4DED3),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
    )
}

@Composable
private fun EmptyAnalyticsRow() {
    Text(
        stringResource(R.string.analytics_no_data),
        color = Color(0xFF8E98A6),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
}

@Composable
private fun AnalyticsRankingRow(row: AnalyticsRow, app: RecentApp?, iconCache: AppIconCache) {
    var icon by remember(app) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(app, iconCache) {
        icon = if (app == null) null else withContext(Dispatchers.IO) {
            runCatching { iconCache.load(app, 96) }.getOrNull()
        }
    }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF263242)),
            contentAlignment = Alignment.Center,
        ) {
            icon?.let { Image(it.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize()) }
        }
        Column(Modifier.weight(1f)) {
            Text(row.label, color = Color(0xFFE4DED3))
            if (row.label != row.packageName) {
                Text(row.packageName, color = Color(0xFF8E98A6), style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(row.count.toString(), color = Color(0xFF8E98A6))
    }
}
