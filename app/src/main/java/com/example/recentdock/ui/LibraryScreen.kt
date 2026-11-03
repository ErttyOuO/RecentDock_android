package com.example.recentdock.ui

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.text.format.DateFormat
import androidx.compose.foundation.Image
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.recentdock.R
import com.example.recentdock.analytics.AnalyticsActivity
import com.example.recentdock.analytics.DockLaunchRecorder
import com.example.recentdock.data.AppCategory
import com.example.recentdock.data.AppIconCache
import com.example.recentdock.data.AppSortMode
import com.example.recentdock.data.CustomAppOrderStore
import com.example.recentdock.data.ManualAppOrderDraft
import com.example.recentdock.data.HiddenAppsStore
import com.example.recentdock.data.PinnedAppsStore
import com.example.recentdock.data.RecentApp
import com.example.recentdock.data.RecentAppsCache
import com.example.recentdock.data.RecentAppsRepository
import com.example.recentdock.data.StarredAppsStore
import com.example.recentdock.data.UsageAccess
import com.example.recentdock.data.sortedFor
import com.example.recentdock.data.sortedForLibrary
import com.example.recentdock.data.supportsHiddenApps
import com.example.recentdock.widget.RecentAppsWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date

private val ScreenBackground = Color(0xFF10161F)
private val ScreenBackgroundTop = Color(0xFF161E29)
private val PanelColor = Color(0xFF171E28)
private val PanelElevatedColor = Color(0xFF1C2430)
private val AccentColor = Color(0xFFE4DED3)
private val SubtleText = Color(0xFF8E98A6)
private val StarColor = Color(0xFFF5D06F)
private const val FOREGROUND_REFRESH_MAX_AGE_MS = 30_000L
private const val LIBRARY_CONTENT_MAX_WIDTH_DP = 720

@Composable
fun RecentDockLibraryScreen(
    initialCategory: AppCategory,
    resumeTick: Int,
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val cache = remember { RecentAppsCache(appContext) }
    val starredStore = remember { StarredAppsStore(appContext) }
    val pinnedStore = remember { PinnedAppsStore(appContext) }
    val hiddenStore = remember { HiddenAppsStore(appContext) }
    val customOrderStore = remember { CustomAppOrderStore(appContext) }
    val iconCache = remember { AppIconCache(appContext) }

    var starredPackages by remember { mutableStateOf(starredStore.getStarredPackages()) }
    var pinnedPackages by remember { mutableStateOf(pinnedStore.getPinnedPackages()) }
    var hiddenByCategory by remember { mutableStateOf(hiddenStore.getAll()) }
    var customOrders by remember { mutableStateOf(customOrderStore.getAll()) }
    var snapshots by remember { mutableStateOf(cache.getAll()) }
    var category by remember { mutableStateOf(initialCategory) }
    var sortMode by remember { mutableStateOf(AppSortMode.CATEGORY_DEFAULT) }
    var refreshing by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }
    var showCredits by remember { mutableStateOf(false) }
    var showHiddenManager by remember { mutableStateOf(false) }
    var uninstallRefreshPending by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var draggedPackage by remember { mutableStateOf<String?>(null) }
    var draggedOffsetY by remember { mutableStateOf(0f) }
    var dragOrderDraft by remember { mutableStateOf<ManualAppOrderDraft?>(null) }
    var dragCategory by remember { mutableStateOf<AppCategory?>(null) }
    var categoryTransitioning by remember { mutableStateOf(false) }
    var swipeDragOffsetX by remember { mutableFloatStateOf(0f) }
    val pageOffsetX = remember { Animatable(0f) }
    val pageAlpha = remember { Animatable(1f) }
    val snackbarHostState = remember { SnackbarHostState() }

    val density = LocalDensity.current
    val pageSlideDistancePx = with(density) { 52.dp.toPx() }
    val swipeThresholdPx = with(density) { 64.dp.toPx() }
    val swipeMaxPx = with(density) { 112.dp.toPx() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val usageGranted = remember(resumeTick) { UsageAccess.hasPermission(context) }
    val cacheRevision by cache.updates.collectAsState()

    // Package callbacks / workers can refresh the shared cache while this screen is already open.
    LaunchedEffect(cacheRevision) {
        val cached = cache.getAll()
        if (cached != snapshots) snapshots = cached
        val currentHidden = hiddenStore.getAll()
        if (currentHidden != hiddenByCategory) hiddenByCategory = currentHidden
    }

    // Stale-while-revalidate: cached lists are already on screen. A scan happens in the
    // background only when stale, and state changes only if the fresh snapshot is different.
    LaunchedEffect(resumeTick, usageGranted) {
        val permissionChanged =
            cache.isInitialized() && usageGranted != cache.usageAccessGrantedAtLastRefresh()

        if (cache.isInitialized() &&
            !permissionChanged &&
            !uninstallRefreshPending &&
            !cache.isStale(FOREGROUND_REFRESH_MAX_AGE_MS)
        ) {
            return@LaunchedEffect
        }

        refreshing = true
        val fresh = withContext(Dispatchers.IO) {
            runCatching { RecentAppsRepository(appContext).refreshCache(cache) }.getOrNull()
        }
        if (fresh != null) {
            if (fresh != snapshots) snapshots = fresh
            uninstallRefreshPending = false
            starredPackages = starredStore.getStarredPackages()
            pinnedPackages = pinnedStore.getPinnedPackages()
        }
        hiddenByCategory = hiddenStore.getAll()
        refreshing = false
    }

    val sourceApps = snapshots[category].orEmpty()
    val hiddenPackages = hiddenByCategory[category].orEmpty()

    // Keep saved manual order healthy after package changes, but never persist reconciliation
    // while a drag draft is active; the gesture commits only from onDragEnd.
    LaunchedEffect(sourceApps, category, dragCategory) {
        if (dragCategory == category) return@LaunchedEffect
        if (customOrderStore.hasCustomOrder(category)) {
            val categoryDefault = sourceApps
                .sortedFor(category, AppSortMode.CATEGORY_DEFAULT)
                .map { it.packageName }
            val reconciled = customOrderStore.reconcile(category, categoryDefault)
            if (reconciled != customOrders[category].orEmpty()) {
                customOrders = customOrders.toMutableMap().apply { put(category, reconciled) }
            }
        }
    }

    val customOrder = customOrders[category].orEmpty()
    val previewOrder = dragOrderDraft
        ?.takeIf { dragCategory == category }
        ?.previewOrder
    val activeCustomOrder = previewOrder ?: customOrder
    val orderedApps = remember(sourceApps, category, sortMode, pinnedPackages, activeCustomOrder) {
        sourceApps.sortedForLibrary(category, sortMode, pinnedPackages, activeCustomOrder)
    }
    val unhiddenApps = remember(orderedApps, hiddenPackages) {
        orderedApps.filterNot { it.packageName in hiddenPackages }
    }
    val normalizedQuery = searchQuery.trim()
    val visibleApps = remember(unhiddenApps, normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            unhiddenApps
        } else {
            unhiddenApps.filter { app ->
                app.label.contains(normalizedQuery, ignoreCase = true) ||
                    app.packageName.contains(normalizedQuery, ignoreCase = true)
            }
        }
    }
    val canReorder =
        sortMode == AppSortMode.CATEGORY_DEFAULT && normalizedQuery.isEmpty() && unhiddenApps.size > 1
    val visiblePackageSet = remember(unhiddenApps) {
        unhiddenApps.mapTo(mutableSetOf()) { it.packageName }
    }
    val allKnownApps = remember(snapshots) {
        snapshots.values.flatten().associateBy { it.packageName }
    }
    val hiddenAppsForManager = remember(hiddenPackages, allKnownApps) {
        hiddenPackages.mapNotNull { allKnownApps[it] }.sortedBy { it.label.lowercase() }
    }

    val categoryTitle = stringResource(
        when (category) {
            AppCategory.RECENTLY_INSTALLED -> R.string.category_recently_installed
            AppCategory.RECENTLY_OPENED -> R.string.category_recently_opened
            AppCategory.STARRED -> R.string.category_starred
        }
    )
    val categoryDescription = stringResource(
        when (category) {
            AppCategory.RECENTLY_INSTALLED -> R.string.category_description_installed
            AppCategory.RECENTLY_OPENED -> R.string.category_description_opened
            AppCategory.STARRED -> R.string.category_description_starred
        }
    )

    fun resetDragState() {
        draggedPackage = null
        draggedOffsetY = 0f
        dragOrderDraft = null
        dragCategory = null
    }

    fun refreshWidget() {
        scope.launch { runCatching { RecentAppsWidget().updateAll(appContext) } }
    }

    fun finishDrag(commit: Boolean) {
        val targetCategory = dragCategory
        val orderToCommit = if (commit) dragOrderDraft?.orderToCommit() else null
        if (targetCategory != null && orderToCommit != null) {
            customOrderStore.setOrder(targetCategory, orderToCommit)
            customOrders = customOrders.toMutableMap().apply {
                put(targetCategory, orderToCommit)
            }
            refreshWidget()
        }
        resetDragState()
    }

    fun refreshHiddenStateAndWidget() {
        hiddenByCategory = hiddenStore.getAll()
        refreshWidget()
    }

    // Pre-warm a small set of icons so category changes do not start with a burst of disk decodes.
    LaunchedEffect(snapshots) {
        withContext(Dispatchers.IO) {
            snapshots.values
                .asSequence()
                .flatten()
                .distinctBy { it.packageName }
                .take(24)
                .forEach { app -> runCatching { iconCache.load(app, 144) } }
        }
    }

    suspend fun switchCategorySmoothly(selected: AppCategory, initialDragOffset: Float = 0f) {
        if (selected == category || categoryTransitioning) return
        categoryTransitioning = true
        val direction = if (selected.ordinal > category.ordinal) 1 else -1
        try {
            pageOffsetX.stop()
            pageAlpha.stop()
            pageOffsetX.snapTo(initialDragOffset)
            pageAlpha.snapTo(1f)
            val exitTarget = if (direction > 0) {
                minOf(-pageSlideDistancePx, initialDragOffset)
            } else {
                maxOf(pageSlideDistancePx, initialDragOffset)
            }

            coroutineScope {
                launch {
                    pageOffsetX.animateTo(
                        targetValue = exitTarget,
                        animationSpec = tween(95, easing = FastOutLinearInEasing),
                    )
                }
                launch {
                    pageAlpha.animateTo(
                        targetValue = 0.80f,
                        animationSpec = tween(90),
                    )
                }
            }

            category = selected
            searchQuery = ""
            showHiddenManager = false
            resetDragState()
            listState.scrollToItem(0)

            pageOffsetX.snapTo(direction * pageSlideDistancePx)
            pageAlpha.snapTo(0.80f)
            coroutineScope {
                launch {
                    pageOffsetX.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(155, easing = FastOutSlowInEasing),
                    )
                }
                launch {
                    pageAlpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(125),
                    )
                }
            }
        } finally {
            swipeDragOffsetX = 0f
            categoryTransitioning = false
        }
    }

    fun requestCategory(selected: AppCategory, initialDragOffset: Float = 0f) {
        if (selected == category || categoryTransitioning) return
        scope.launch { switchCategorySmoothly(selected, initialDragOffset) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ScreenBackgroundTop, ScreenBackground)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = AccentColor,
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CategoryBottomBar(
                        modifier = Modifier.fillMaxWidth().widthIn(max = LIBRARY_CONTENT_MAX_WIDTH_DP.dp),
                        category = category,
                        onSelect = { selected -> requestCategory(selected) },
                    )
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.TopCenter,
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .widthIn(max = LIBRARY_CONTENT_MAX_WIDTH_DP.dp)
                        .pointerInput(category, categoryTransitioning) {
                            if (categoryTransitioning) return@pointerInput
                            detectHorizontalDragGestures(
                                onDragStart = { swipeDragOffsetX = 0f },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    swipeDragOffsetX = (swipeDragOffsetX + dragAmount)
                                        .coerceIn(-swipeMaxPx, swipeMaxPx)
                                },
                                onDragCancel = {
                                    val current = swipeDragOffsetX
                                    swipeDragOffsetX = 0f
                                    scope.launch {
                                        pageOffsetX.snapTo(current)
                                        pageOffsetX.animateTo(
                                            0f,
                                            animationSpec = tween(120, easing = FastOutSlowInEasing),
                                        )
                                    }
                                },
                                onDragEnd = {
                                    val drag = swipeDragOffsetX
                                    val target = when {
                                        drag <= -swipeThresholdPx -> AppCategory.entries.getOrNull(category.ordinal + 1)
                                        drag >= swipeThresholdPx -> AppCategory.entries.getOrNull(category.ordinal - 1)
                                        else -> null
                                    }
                                    swipeDragOffsetX = 0f
                                    if (target != null) {
                                        requestCategory(target, initialDragOffset = drag)
                                    } else {
                                        scope.launch {
                                            pageOffsetX.snapTo(drag)
                                            pageOffsetX.animateTo(
                                                0f,
                                                animationSpec = tween(120, easing = FastOutSlowInEasing),
                                            )
                                        }
                                    }
                                },
                            )
                        }
                        .graphicsLayer {
                            translationX = pageOffsetX.value + swipeDragOffsetX
                            alpha = pageAlpha.value
                        },
                    contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item(key = "library_header") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = categoryTitle,
                                        color = AccentColor,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 34.sp,
                                        lineHeight = 40.sp,
                                    )
                                    Spacer(Modifier.height(5.dp))
                                    Text(
                                        text = categoryDescription,
                                        color = SubtleText,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }

                                Box {
                                    IconButton(onClick = { sortExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                                            contentDescription = stringResource(R.string.sort_title),
                                            tint = AccentColor,
                                        )
                                    }
                                    SortMenu(
                                        expanded = sortExpanded,
                                        selected = sortMode,
                                        category = category,
                                        customOrderActive = customOrderStore.hasCustomOrder(category),
                                        onDismiss = { sortExpanded = false },
                                        onSelect = {
                                            sortMode = it
                                            resetDragState()
                                            sortExpanded = false
                                        },
                                        onResetCustomOrder = {
                                            customOrderStore.clear(category)
                                            customOrders = customOrders.toMutableMap().apply {
                                                put(category, emptyList())
                                            }
                                            resetDragState()
                                            refreshWidget()
                                            sortExpanded = false
                                        },
                                    )
                                }

                                Box {
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Menu,
                                            contentDescription = stringResource(R.string.about_and_credits),
                                            tint = AccentColor,
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                        containerColor = PanelElevatedColor,
                                    ) {
                                        if (category.supportsHiddenApps()) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = stringResource(
                                                            R.string.manage_hidden_apps_with_count,
                                                            hiddenPackages.size,
                                                        ),
                                                        color = AccentColor,
                                                    )
                                                },
                                                onClick = {
                                                    menuExpanded = false
                                                    showHiddenManager = true
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Visibility,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(20.dp),
                                                        tint = SubtleText,
                                                    )
                                                },
                                            )
                                            HorizontalDivider()
                                        }
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.about_and_credits), color = AccentColor) },
                                            onClick = {
                                                menuExpanded = false
                                                showCredits = true
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(20.dp), tint = SubtleText)
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.creator_github), color = AccentColor) },
                                            onClick = {
                                                menuExpanded = false
                                                runCatching {
                                                    context.startActivity(
                                                        Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse("https://github.com/ErttyOuO"),
                                                        )
                                                    )
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.AutoMirrored.Outlined.OpenInNew,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = SubtleText,
                                                )
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.analytics_title), color = AccentColor) },
                                            onClick = {
                                                menuExpanded = false
                                                context.startActivity(
                                                    Intent(context, AnalyticsActivity::class.java)
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Rounded.BarChart, contentDescription = null, modifier = Modifier.size(20.dp), tint = SubtleText)
                                            },
                                        )
                                        if (!usageGranted) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        stringResource(R.string.usage_access_button),
                                                        color = AccentColor,
                                                    )
                                                },
                                                onClick = {
                                                    menuExpanded = false
                                                    context.startActivity(
                                                        Intent(context, UsageAccessActivity::class.java)
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(Icons.Outlined.Settings, contentDescription = null, modifier = Modifier.size(20.dp), tint = SubtleText)
                                                },
                                            )
                                        }
                                    }
                                }
                            }

                            LibrarySearchField(
                                query = searchQuery,
                                onQueryChange = {
                                    searchQuery = it
                                    resetDragState()
                                },
                                onClear = {
                                    searchQuery = ""
                                    resetDragState()
                                },
                            )

                            if (sortMode == AppSortMode.CATEGORY_DEFAULT) {
                                Text(
                                    text = if (normalizedQuery.isEmpty()) {
                                        stringResource(R.string.manual_order_hint)
                                    } else {
                                        stringResource(R.string.manual_order_search_disabled)
                                    },
                                    color = SubtleText,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }

                    if (category == AppCategory.RECENTLY_OPENED && !usageGranted) {
                        item {
                            UsagePermissionCard {
                                context.startActivity(Intent(context, UsageAccessActivity::class.java))
                            }
                        }
                    } else if (visibleApps.isEmpty() && refreshing) {
                        item { StatusPanel(stringResource(R.string.loading_apps)) }
                    } else if (visibleApps.isEmpty()) {
                        item {
                            StatusPanel(
                                when {
                                    normalizedQuery.isNotEmpty() ->
                                        stringResource(R.string.search_no_results, normalizedQuery)
                                    sourceApps.isNotEmpty() && unhiddenApps.isEmpty() &&
                                        category.supportsHiddenApps() ->
                                        stringResource(R.string.all_apps_hidden)
                                    else -> when (category) {
                                        AppCategory.STARRED -> stringResource(R.string.empty_starred)
                                        AppCategory.RECENTLY_OPENED ->
                                            stringResource(R.string.empty_recently_opened)
                                        AppCategory.RECENTLY_INSTALLED ->
                                            stringResource(R.string.empty_apps)
                                    }
                                }
                            )
                        }
                    } else {
                        items(
                            items = visibleApps,
                            key = { it.packageName },
                            contentType = { "app" },
                        ) { app ->
                            RecentAppCard(
                                app = app,
                                category = category,
                                starred = app.packageName in starredPackages,
                                pinned = app.packageName in pinnedPackages,
                                iconCache = iconCache,
                                hideEnabled = category.supportsHiddenApps(),
                                dragEnabled = canReorder,
                                dragging = draggedPackage == app.packageName,
                                dragOffsetY = if (draggedPackage == app.packageName) {
                                    draggedOffsetY
                                } else {
                                    0f
                                },
                                onLaunch = {
                                    DockLaunchRecorder.recordAsync(
                                        appContext,
                                        app.packageName,
                                        "app",
                                    )
                                    runCatching { context.startActivity(app.launchIntent()) }
                                },
                                onStar = {
                                    val nowStarred = app.packageName !in starredPackages
                                    val updatedPackages = starredPackages.toMutableSet().apply {
                                        if (nowStarred) add(app.packageName) else remove(app.packageName)
                                    }.toSet()

                                    // Optimistic UI first: the star state and Starred category update in this frame.
                                    starredPackages = updatedPackages
                                    val starredSnapshot = snapshots[AppCategory.RECENTLY_INSTALLED]
                                        .orEmpty()
                                        .filter { it.packageName in updatedPackages }
                                        .sortedWith(
                                            compareByDescending<RecentApp> { it.firstInstallTime }
                                                .thenBy { it.label.lowercase() }
                                        )
                                    snapshots = snapshots.toMutableMap().apply {
                                        put(AppCategory.STARRED, starredSnapshot)
                                    }

                                    starredStore.setStarred(app.packageName, nowStarred)
                                    scope.launch(Dispatchers.IO) {
                                        runCatching { cache.rebuildStarred(updatedPackages) }
                                        runCatching { RecentAppsWidget().updateAll(appContext) }
                                    }
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar(
                                            context.getString(
                                                if (nowStarred) R.string.star_added_snackbar
                                                else R.string.star_removed_snackbar,
                                                app.label,
                                            )
                                        )
                                    }
                                },
                                onPin = {
                                    pinnedStore.toggle(app.packageName)
                                    pinnedPackages = pinnedStore.getPinnedPackages()
                                    resetDragState()
                                    refreshWidget()
                                },
                                onHide = {
                                    if (category.supportsHiddenApps()) {
                                        hiddenStore.hide(category, app.packageName)
                                        refreshHiddenStateAndWidget()
                                        resetDragState()
                                    }
                                },
                                onDetails = {
                                    context.startActivity(
                                        Intent(context, AppDetailActivity::class.java)
                                            .putExtra(AppDetailActivity.EXTRA_PACKAGE_NAME, app.packageName)
                                    )
                                },
                                onUninstall = {
                                    uninstallRefreshPending = true
                                    val uninstallIntent = Intent(
                                        Intent.ACTION_DELETE,
                                        Uri.parse("package:${app.packageName}"),
                                    )
                                    runCatching { context.startActivity(uninstallIntent) }
                                        .onFailure {
                                            uninstallRefreshPending = false
                                            scope.launch {
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                                snackbarHostState.showSnackbar(
                                                    context.getString(
                                                        R.string.uninstall_not_available,
                                                        app.label,
                                                    )
                                                )
                                            }
                                        }
                                },
                                onDragStart = {
                                    val displayedPackages = sourceApps
                                        .sortedForLibrary(
                                            category,
                                            AppSortMode.CATEGORY_DEFAULT,
                                            pinnedPackages,
                                            customOrders[category].orEmpty(),
                                        )
                                        .map { it.packageName }
                                    dragCategory = category
                                    dragOrderDraft = ManualAppOrderDraft(displayedPackages)
                                    draggedPackage = app.packageName
                                    draggedOffsetY = 0f
                                },
                                onDrag = { deltaY ->
                                    val draft = dragOrderDraft
                                    if (draggedPackage == app.packageName &&
                                        dragCategory == category &&
                                        canReorder &&
                                        draft != null
                                    ) {
                                        draggedOffsetY += deltaY
                                        val layoutInfo = listState.layoutInfo
                                        val currentInfo = layoutInfo.visibleItemsInfo
                                            .firstOrNull { it.key == app.packageName }
                                        if (currentInfo != null) {
                                            val draggedCenter =
                                                currentInfo.offset + draggedOffsetY +
                                                    currentInfo.size / 2f
                                            val targetInfo =
                                                layoutInfo.visibleItemsInfo.firstOrNull { info ->
                                                    val targetPackage = info.key as? String
                                                    targetPackage != null &&
                                                        targetPackage in visiblePackageSet &&
                                                        targetPackage != app.packageName &&
                                                        (targetPackage in pinnedPackages) ==
                                                        (app.packageName in pinnedPackages) &&
                                                        draggedCenter >= info.offset &&
                                                        draggedCenter <= info.offset + info.size
                                                }

                                            val targetPackage = targetInfo?.key as? String
                                            if (targetInfo != null && targetPackage != null) {
                                                val movedDraft = draft.move(app.packageName, targetPackage)
                                                if (movedDraft != draft) {
                                                    dragOrderDraft = movedDraft
                                                    draggedOffsetY +=
                                                        (currentInfo.offset - targetInfo.offset).toFloat()
                                                }
                                            }

                                            val draggedTop = currentInfo.offset + draggedOffsetY
                                            val draggedBottom = draggedTop + currentInfo.size
                                            val scrollDelta = when {
                                                draggedBottom > layoutInfo.viewportEndOffset ->
                                                    (draggedBottom - layoutInfo.viewportEndOffset)
                                                        .coerceAtMost(36f)
                                                draggedTop < layoutInfo.viewportStartOffset ->
                                                    (draggedTop - layoutInfo.viewportStartOffset)
                                                        .coerceAtLeast(-36f)
                                                else -> 0f
                                            }
                                            if (scrollDelta != 0f) {
                                                scope.launch { listState.scrollBy(scrollDelta) }
                                            }
                                        }
                                    }
                                },
                                onDragCancel = { finishDrag(commit = false) },
                                onDragEnd = { finishDrag(commit = true) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCredits) {
        CreditsDialog { showCredits = false }
    }

    if (showHiddenManager && category.supportsHiddenApps()) {
        HiddenAppsDialog(
            categoryTitle = categoryTitle,
            hiddenApps = hiddenAppsForManager,
            hiddenCount = hiddenPackages.size,
            onRestore = { app ->
                hiddenStore.unhide(category, app.packageName)
                refreshHiddenStateAndWidget()
            },
            onRestoreAll = {
                hiddenStore.clear(category)
                refreshHiddenStateAndWidget()
            },
            onDismiss = { showHiddenManager = false },
        )
    }
}

@Composable
private fun SortMenu(
    expanded: Boolean,
    selected: AppSortMode,
    category: AppCategory,
    customOrderActive: Boolean,
    onDismiss: () -> Unit,
    onSelect: (AppSortMode) -> Unit,
    onResetCustomOrder: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = PanelElevatedColor,
    ) {
        val options = buildList {
            add(AppSortMode.CATEGORY_DEFAULT to R.string.sort_category_default)
            add(AppSortMode.NAME_ASC to R.string.sort_name_asc)
            add(AppSortMode.NAME_DESC to R.string.sort_name_desc)
            add(AppSortMode.NEWEST_INSTALL to R.string.sort_newest_install)
            add(AppSortMode.OLDEST_INSTALL to R.string.sort_oldest_install)
            add(AppSortMode.LAST_USED to R.string.sort_last_used)
        }
        options.forEach { (mode, label) ->
            DropdownMenuItem(
                text = { Text(stringResource(label), color = AccentColor) },
                onClick = { onSelect(mode) },
                leadingIcon = if (mode == selected) {
                    { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp), tint = AccentColor) }
                } else {
                    null
                },
            )
        }
        if (customOrderActive) {
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.reset_manual_order), color = AccentColor) },
                onClick = onResetCustomOrder,
                leadingIcon = { Icon(Icons.Rounded.DragHandle, contentDescription = null, modifier = Modifier.size(20.dp), tint = SubtleText) },
            )
        }
    }
}

@Composable
private fun RecentAppCard(
    app: RecentApp,
    category: AppCategory,
    starred: Boolean,
    pinned: Boolean,
    iconCache: AppIconCache,
    hideEnabled: Boolean,
    dragEnabled: Boolean,
    dragging: Boolean,
    dragOffsetY: Float,
    onLaunch: () -> Unit,
    onStar: () -> Unit,
    onPin: () -> Unit,
    onHide: () -> Unit,
    onDetails: () -> Unit,
    onUninstall: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragCancel: () -> Unit,
    onDragEnd: () -> Unit,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val feedbackScope = rememberCoroutineScope()
    var actionMenuExpanded by remember(app.packageName) { mutableStateOf(false) }
    var pressed by remember(app.packageName) { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = tween(if (pressed) 75 else 125, easing = FastOutSlowInEasing),
        label = "appPressScale",
    )
    val starPulse = remember(app.packageName) { Animatable(0f) }
    val timestamp = if (category == AppCategory.RECENTLY_OPENED) {
        app.lastUsedTime
    } else {
        app.firstInstallTime
    }
    val date = if (timestamp > 0L) {
        DateFormat.getMediumDateFormat(context).format(Date(timestamp))
    } else {
        ""
    }
    val secondary = when (category) {
        AppCategory.RECENTLY_OPENED -> stringResource(R.string.opened_on, date)
        AppCategory.RECENTLY_INSTALLED -> if (app.lastUsedTime > 0L) {
            stringResource(
                R.string.installed_and_last_used,
                date,
                DateFormat.getMediumDateFormat(context).format(Date(app.lastUsedTime)),
            )
        } else stringResource(R.string.installed_on, date)
        AppCategory.STARRED -> if (app.lastUsedTime > 0L) {
            stringResource(
                R.string.last_used_on,
                DateFormat.getMediumDateFormat(context).format(Date(app.lastUsedTime)),
            )
        } else stringResource(R.string.starred_item)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(if (dragging) 1f else 0f)
                .graphicsLayer {
                    translationY = if (dragging) dragOffsetY else 0f
                    scaleX = pressScale
                    scaleY = pressScale
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (pressed) PanelElevatedColor else PanelColor,
            ),
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .preciseOneSecondPress(
                        onClick = onLaunch,
                        onPressedChange = { pressed = it },
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStar()
                            feedbackScope.launch {
                                starPulse.snapTo(0f)
                                starPulse.animateTo(
                                    1f,
                                    animationSpec = tween(120, easing = FastOutSlowInEasing),
                                )
                                starPulse.animateTo(
                                    0f,
                                    animationSpec = tween(420, easing = FastOutSlowInEasing),
                                )
                            }
                        },
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LazyCachedAppIcon(app = app, iconCache = iconCache)
                Spacer(Modifier.size(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.label,
                        color = AccentColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = secondary,
                        color = SubtleText,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (dragEnabled) {
                    AppDragHandle(
                        onDragStart = onDragStart,
                        onDrag = onDrag,
                        onDragCancel = onDragCancel,
                        onDragEnd = onDragEnd,
                    )
                }

                Box {
                    Surface(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable { actionMenuExpanded = true },
                        shape = CircleShape,
                        color = PanelElevatedColor,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.MoreHoriz,
                                contentDescription = stringResource(R.string.about_and_credits),
                                modifier = Modifier.size(18.dp),
                                tint = SubtleText,
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = actionMenuExpanded,
                        onDismissRequest = { actionMenuExpanded = false },
                        containerColor = PanelElevatedColor,
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (pinned) {
                                        stringResource(R.string.unpin_app)
                                    } else {
                                        stringResource(R.string.pin_app)
                                    },
                                    color = AccentColor,
                                )
                            },
                            onClick = {
                                actionMenuExpanded = false
                                onPin()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.PushPin,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (pinned) AccentColor else SubtleText,
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (starred) {
                                        stringResource(R.string.remove_star)
                                    } else {
                                        stringResource(R.string.add_star)
                                    },
                                    color = AccentColor,
                                )
                            },
                            onClick = {
                                actionMenuExpanded = false
                                onStar()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (starred) Icons.Rounded.Star else Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (starred) StarColor else SubtleText,
                                )
                            },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.app_details), color = AccentColor) },
                            onClick = {
                                actionMenuExpanded = false
                                onDetails()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = SubtleText,
                                )
                            },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.uninstall_app),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                actionMenuExpanded = false
                                onUninstall()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                        )
                        if (hideEnabled) {
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.hide_app),
                                        color = AccentColor,
                                    )
                                },
                                onClick = {
                                    actionMenuExpanded = false
                                    onHide()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.VisibilityOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = SubtleText,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
        }

        if (starPulse.value > 0f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        val pulse = starPulse.value
                        alpha = pulse
                        scaleX = 0.78f + pulse * 0.34f
                        scaleY = 0.78f + pulse * 0.34f
                    },
                shape = CircleShape,
                color = StarColor.copy(alpha = 0.18f),
            ) {
                Box(
                    modifier = Modifier.padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (starred) Icons.Rounded.Star else Icons.Outlined.StarOutline,
                        contentDescription = null,
                        tint = StarColor,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LibrarySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = SubtleText,
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.search_clear),
                        tint = SubtleText,
                    )
                }
            }
        } else {
            null
        },
        placeholder = {
            Text(
                text = stringResource(R.string.search_apps_hint),
                color = SubtleText,
            )
        },
    )
}

@Composable
private fun AppDragHandle(
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragCancel: () -> Unit,
    onDragEnd: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    Box(
        modifier = Modifier
            .size(34.dp)
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentOnDragStart()
                    },
                    onDragCancel = currentOnDragCancel,
                    onDragEnd = currentOnDragEnd,
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentOnDrag(dragAmount.y)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.DragHandle,
            contentDescription = stringResource(R.string.drag_to_reorder),
            modifier = Modifier.size(18.dp),
            tint = SubtleText,
        )
    }
}

@Composable
private fun HiddenAppsDialog(
    categoryTitle: String,
    hiddenApps: List<RecentApp>,
    hiddenCount: Int,
    onRestore: (RecentApp) -> Unit,
    onRestoreAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
        dismissButton = if (hiddenCount > 0) {
            {
                TextButton(onClick = onRestoreAll) {
                    Text(stringResource(R.string.restore_all_hidden_apps))
                }
            }
        } else {
            null
        },
        title = {
            Text(stringResource(R.string.manage_hidden_apps_title, categoryTitle))
        },
        text = {
            if (hiddenApps.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_hidden_apps),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    hiddenApps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(PanelColor)
                                .padding(start = 12.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = app.label,
                                modifier = Modifier.weight(1f),
                                color = AccentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            IconButton(onClick = { onRestore(app) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Visibility,
                                    contentDescription = stringResource(R.string.restore_hidden_app),
                                    tint = AccentColor,
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun LazyCachedAppIcon(
    app: RecentApp,
    iconCache: AppIconCache,
) {
    var bitmap by remember(app.packageName, app.lastUpdateTime) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(app.packageName, app.lastUpdateTime) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching { iconCache.load(app, 144) }.getOrNull()
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = app.label,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)),
        )
    } else {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PanelElevatedColor),
        )
    }
}

@Composable
private fun StatusPanel(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PanelColor),
    ) {
        Text(text, color = SubtleText, modifier = Modifier.padding(18.dp))
    }
}

@Composable
private fun UsagePermissionCard(onGrant: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = PanelElevatedColor),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(R.string.usage_access_title),
                color = AccentColor,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(R.string.usage_access_body),
                color = SubtleText,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onGrant) { Text(stringResource(R.string.usage_access_button)) }
        }
    }
}

@Composable
private fun CategoryBottomBar(
    modifier: Modifier = Modifier,
    category: AppCategory,
    onSelect: (AppCategory) -> Unit,
) {
    Surface(
        color = Color.Transparent,
        modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = PanelElevatedColor),
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                BottomCategoryItem(
                    Modifier.weight(1f),
                    category == AppCategory.RECENTLY_INSTALLED,
                    stringResource(R.string.category_recently_installed_short),
                    { Icon(Icons.Rounded.DownloadForOffline, contentDescription = null) },
                ) { onSelect(AppCategory.RECENTLY_INSTALLED) }
                BottomCategoryItem(
                    Modifier.weight(1f),
                    category == AppCategory.RECENTLY_OPENED,
                    stringResource(R.string.category_recently_opened_short),
                    { Icon(Icons.Rounded.History, contentDescription = null) },
                ) { onSelect(AppCategory.RECENTLY_OPENED) }
                BottomCategoryItem(
                    Modifier.weight(1f),
                    category == AppCategory.STARRED,
                    stringResource(R.string.category_starred_short),
                    { Icon(Icons.Rounded.Star, contentDescription = null) },
                ) { onSelect(AppCategory.STARRED) }
            }
        }
    }
}

@Composable
private fun BottomCategoryItem(
    modifier: Modifier,
    selected: Boolean,
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(if (selected) PanelColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (selected) AccentColor.copy(alpha = 0.12f) else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (selected) AccentColor else SubtleText
            ) {
                icon()
            }
        }
        Spacer(Modifier.size(7.dp))
        Text(
            label,
            color = if (selected) AccentColor else SubtleText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

/**
 * Distinguishes a normal tap from an intentional one-second hold.
 *
 * Releasing before one second launches the app. Moving beyond touch slop cancels the action.
 * Completing the full second triggers star/unstar and release afterwards cannot also launch.
 */
private fun Modifier.preciseOneSecondPress(
    onClick: () -> Unit,
    onPressedChange: (Boolean) -> Unit = {},
    onLongPress: () -> Unit,
): Modifier = pointerInput(onClick, onPressedChange, onLongPress) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        onPressedChange(true)
        try {
            var moved = false
            var accumulated = Offset.Zero
            var releasedBeforeTimeout = false
            val completedBeforeTimeout = withTimeoutOrNull(1_000L) {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id }
                    if (change == null) {
                        releasedBeforeTimeout = true
                        return@withTimeoutOrNull true
                    }
                    accumulated += change.position - change.previousPosition
                    if (accumulated.getDistance() > viewConfiguration.touchSlop || change.isConsumed) {
                        moved = true
                        return@withTimeoutOrNull true
                    }
                    if (!change.pressed) {
                        releasedBeforeTimeout = true
                        return@withTimeoutOrNull true
                    }
                }
            }

            if (completedBeforeTimeout == null && !moved) {
                onLongPress()
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break
                }
            } else if (releasedBeforeTimeout && !moved) {
                onClick()
            }
        } finally {
            onPressedChange(false)
        }
    }
}

@Composable
private fun CreditsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
        title = {
            Text(
                stringResource(R.string.about_and_credits),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(stringResource(R.string.creator_line))
                TextButton(
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/ErttyOuO"),
                                )
                            )
                        }
                    },
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text("https://github.com/ErttyOuO")
                }
                HorizontalDivider()
                Text(
                    stringResource(R.string.opensource_used_title),
                    fontWeight = FontWeight.SemiBold,
                )
                Text(stringResource(R.string.opensource_used_body))
                Text(
                    stringResource(R.string.font_note),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
    )
}
