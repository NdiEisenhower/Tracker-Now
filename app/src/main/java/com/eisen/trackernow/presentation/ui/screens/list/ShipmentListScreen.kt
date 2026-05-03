package com.eisen.trackernow.presentation.ui.screens.list

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eisen.trackernow.domain.model.Shipment
import com.eisen.trackernow.presentation.ui.ThemeManager
import com.eisen.trackernow.presentation.ui.ThemeMode
import com.eisen.trackernow.presentation.ui.components.OfflineIndicator
import com.eisen.trackernow.presentation.util.Helper
import com.eisen.trackernow.presentation.util.Helper.formatDate
import com.eisen.trackernow.presentation.util.Helper.getCarrierColor
import com.eisen.trackernow.presentation.util.Resource
import com.eisen.trackernow.presentation.viewmodel.ShipmentListViewModel
import com.eisen.trackernow.presentation.viewmodel.ShipmentListViewModel.StatusFilter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import com.eisen.trackernow.presentation.ui.LocalThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentListScreen(
    viewModel: ShipmentListViewModel = hiltViewModel(),
    onShipmentClick: (String) -> Unit
) {
    val shipmentsState by viewModel.shipments.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val hasNewUpdates by viewModel.hasNewUpdates.collectAsState()
    val lastUpdateMessage by viewModel.lastUpdateMessage.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val themeManager = LocalThemeManager.current
    val themeMode by themeManager.getThemeMode().collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
    val userId by viewModel.userId.collectAsStateWithLifecycle()
    val isDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val modernColors = Helper.getModernColors(isDarkTheme)
    val filteredShipments = remember(searchQuery, selectedFilter, showOnlyFavorites, shipmentsState) {
        viewModel.getFilteredShipments()
    }

    var searchActive by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showRecentSearches by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val showScrollTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    val filterCount = listOfNotNull(
        if (searchQuery.isNotEmpty()) "search" else null,
        selectedFilter?.let { "filter" },
        if (showOnlyFavorites) "fav" else null  // This is already there
    ).size

    // Pull to Refresh State
    var localIsRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    // Handle refresh
    LaunchedEffect(localIsRefreshing) {
        if (localIsRefreshing) {
            if (hasNewUpdates) {
                viewModel.refreshWithUpdates()
            } else {
                viewModel.refresh()
            }
            delay(1000)
            localIsRefreshing = false
        }
    }

    Scaffold(
        containerColor = modernColors.background,
        topBar = {
            ModernTopBar(
                searchActive = searchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = { query ->
                    viewModel.updateSearchQuery(query)
                },
                onSearchSubmit = { query ->
                    // Call this when search is submitted (e.g., on keyboard done action)
                    if (query.isNotEmpty()) {
                        viewModel.addRecentSearchIfHasResults(query)
                    }
                },
                onSearchActiveChange = { active ->
                    searchActive = active
                    if (!active) {
                        showRecentSearches = false
                    } else {
                        showRecentSearches = true
                    }
                },
                onFilterClick = { showFilterSheet = true },
                onThemeClick = { showThemeDialog = true },
                onRefresh = {
                    coroutineScope.launch {
                        if (hasNewUpdates) {
                            viewModel.refreshWithUpdates()
                        } else {
                            viewModel.refresh()
                        }
                    }
                },
                isRefreshing = isRefreshing,
                hasNewUpdates = hasNewUpdates,
                hasActiveFilters = filterCount > 0,
                onClearFilters = { viewModel.clearFilters() },
                showOnlyFavorites = showOnlyFavorites,
                onToggleFavorites = { viewModel.toggleShowOnlyFavorites() },
                modernColors = modernColors,
                recentSearches = recentSearches,
                userId = userId,
                onRecentSearchClick = { query ->
                    viewModel.updateSearchQuery(query)
                    searchActive = false
                    showRecentSearches = false
                },
                onClearRecentSearches = { viewModel.clearRecentSearches() }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showScrollTop,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    containerColor = modernColors.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Scroll to top",
                        tint = modernColors.surface
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Update notification banner
            AnimatedVisibility(
                visible = lastUpdateMessage != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .zIndex(1f),
                    colors = CardDefaults.cardColors(containerColor = modernColors.success)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lastUpdateMessage ?: "",
                            color = modernColors.surface,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.dismissUpdateMessage() }) {
                            Text("Dismiss", color = modernColors.surface, fontSize = 12.sp)
                        }
                    }
                }
            }

            when (val state = shipmentsState) {
                is Resource.Loading -> {
                    ModernShimmerLoadingState(modernColors = modernColors)
                }

                is Resource.Success -> {
                    if (filteredShipments.isEmpty()) {
                        ModernEmptyState(
                            hasFilters = filterCount > 0,
                            modernColors = modernColors,
                            onClearFilters = { viewModel.clearFilters() }
                        )
                    } else {
                        PullToRefreshBox(
                            isRefreshing = localIsRefreshing,
                            onRefresh = {
                                localIsRefreshing = true
                            },
                            modifier = Modifier.fillMaxSize(),
                            state = pullToRefreshState,
                            indicator = {
                                PullToRefreshDefaults.Indicator(
                                    modifier = Modifier.align(Alignment.TopCenter),
                                    isRefreshing = localIsRefreshing,
                                    containerColor = modernColors.surface,
                                    color = modernColors.primary,
                                    state = pullToRefreshState
                                )
                            }
                        ) {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 8.dp,
                                    bottom = 80.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Stats header
                                item {
                                    StatsHeader(
                                        totalCount = filteredShipments.size,
                                        favoriteCount = filteredShipments.count { it.isFavorite },
                                        showOnlyFavorites = showOnlyFavorites,
                                        onToggleFavorites = { viewModel.toggleShowOnlyFavorites() },
                                        modernColors = modernColors
                                    )
                                }

                                items(
                                    items = filteredShipments,
                                    key = { it.id }
                                ) { shipment ->
                                    AnimatedShipmentCard(
                                        shipment = shipment,
                                        onClick = { onShipmentClick(shipment.id) },
                                        onFavoriteClick = { viewModel.toggleFavorite(shipment.id) },
                                        modernColors = modernColors
                                    )
                                }
                            }
                        }
                    }

                    if (state.isOffline) {
                        OfflineIndicator(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            modernColors = modernColors
                        )
                    }
                }

                is Resource.Error -> {
                    ModernErrorState(
                        message = state.message ?: "An error occurred",
                        modernColors = modernColors,
                        onRetry = { viewModel.refresh() }
                    )
                }
            }
        }

        // Filter Bottom Sheet
        if (showFilterSheet) {
            ModernFilterBottomSheet(
                onDismiss = { showFilterSheet = false },
                currentFilter = selectedFilter,
                onFilterSelected = { filter ->
                    viewModel.updateFilter(filter)
                    showFilterSheet = false
                },
                searchQuery = searchQuery,
                modernColors = modernColors,
                onSearchClear = { viewModel.updateSearchQuery("") }
            )
        }

        // Theme Dialog
        if (showThemeDialog) {
            ThemeSelectorDialog(
                onDismiss = { showThemeDialog = false },
                currentTheme = themeMode,
                onThemeSelected = { themeManager.setThemeModeSync(it) },
                modernColors = modernColors
            )
        }
    }
}

@Composable
fun ModernShimmerLoadingState(modernColors: Helper.ModernColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(5) {
            ShimmerCardItem(modernColors = modernColors)
        }
    }
}

@Composable
fun ShimmerCardItem(modernColors: Helper.ModernColors) {
    val shimmerColors = listOf(
        modernColors.surface.copy(alpha = 0.6f),
        modernColors.surface.copy(alpha = 0.8f),
        modernColors.surface.copy(alpha = 0.6f)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = modernColors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Shimmer for carrier logo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmerBackground(shimmerColors)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Shimmer for carrier name
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerBackground(shimmerColors)
                )

                // Shimmer for tracking number
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerBackground(shimmerColors)
                )

                // Shimmer for status chip
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerBackground(shimmerColors)
                )
            }
        }
    }
}

@Composable
fun Modifier.shimmerBackground(shimmerColors: List<Color>): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    this.background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = Float.POSITIVE_INFINITY, y = 0f),
            tileMode = TileMode.Repeated
        )
    )
}

@Composable
fun ThemeSelectorDialog(
    onDismiss: () -> Unit,
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    modernColors: Helper.ModernColors
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = modernColors.surface,
        title = { Text("Select Theme", color = modernColors.primary) },
        text = {
            Column {
                listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK).forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (theme) {
                                ThemeMode.SYSTEM -> "System Default"
                                ThemeMode.LIGHT -> "Light Mode"
                                ThemeMode.DARK -> "Dark Mode"
                            },
                            color = modernColors.onSurface
                        )
                        if (currentTheme == theme) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = modernColors.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = modernColors.primary)
            }
        }
    )
}

@Composable
fun StatsHeader(
    totalCount: Int,
    favoriteCount: Int,
    showOnlyFavorites: Boolean,
    onToggleFavorites: () -> Unit,
    modernColors: Helper.ModernColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = modernColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatButton(
                icon = Icons.Default.Inbox,
                value = "$totalCount",
                label = "Total",
                color = modernColors.info,
                isSelected = !showOnlyFavorites,
                onClick = { if (showOnlyFavorites) onToggleFavorites() },
                modernColors = modernColors
            )

            HorizontalDivider(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp), thickness = DividerDefaults.Thickness, color = modernColors.onSurface.copy(alpha = 0.1f)
            )

            StatButton(
                icon = Icons.Default.Favorite,
                value = "$favoriteCount",
                label = "Favorites",
                color = modernColors.tertiary,
                isSelected = showOnlyFavorites,
                onClick = onToggleFavorites,
                modernColors = modernColors
            )
        }
    }
}

@Composable
fun StatButton(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modernColors: Helper.ModernColors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) color else modernColors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) color else modernColors.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = modernColors.onSurface.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar(
    searchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit = {},
    onSearchActiveChange: (Boolean) -> Unit,
    onFilterClick: () -> Unit,
    onThemeClick: () -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    hasNewUpdates: Boolean,
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    showOnlyFavorites: Boolean,
    onToggleFavorites: () -> Unit,
    modernColors: Helper.ModernColors,
    recentSearches: List<String>,
    userId: String,
    onRecentSearchClick: (String) -> Unit,
    onClearRecentSearches: () -> Unit
) {
    var overflowExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = modernColors.surface,
        shadowElevation = 4.dp
    ) {
        Column {
            TopAppBar(
                title = {
                    if (searchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Search by tracking # or carrier...",
                                    color = modernColors.onSurface.copy(alpha = 0.5f)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = modernColors.primary,
                                cursorColor = modernColors.primary,
                                focusedTextColor = modernColors.onSurface,
                                unfocusedTextColor = modernColors.onSurface
                            ),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                    ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onDone = {
                                    if (searchQuery.isNotEmpty()) {
                                        onSearchSubmit(searchQuery)
                                    }
                                }
                            )
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = modernColors.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Helper.SafeImage(id = com.eisen.trackernow.R.drawable.ic_logo, null)
                            }
                            Text(
                                "Tracker Now",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = modernColors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Visible
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (searchActive) {
                        IconButton(onClick = {
                            onSearchActiveChange(false)
                            onSearchQueryChange("")
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = modernColors.onSurface
                            )
                        }
                    }
                },
                actions = {
                    if (!searchActive) {
                        IconButton(onClick = { onSearchActiveChange(true) }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = modernColors.onSurface
                            )
                        }

                        IconButton(onClick = onToggleFavorites) {
                            Icon(
                                if (showOnlyFavorites) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Show Favorites",
                                tint = if (showOnlyFavorites) modernColors.tertiary else modernColors.onSurface
                            )
                        }

                        IconButton(onClick = { overflowExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = modernColors.onSurface
                            )
                        }
                    } else {
                        if (searchQuery.isNotEmpty()) {
                            TextButton(onClick = { onSearchQueryChange("") }) {
                                Text("Clear", color = modernColors.primary)
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = overflowExpanded,
                        onDismissRequest = { overflowExpanded = false },
                        containerColor = modernColors.surface
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Filter", color = modernColors.onSurface)
                                    if (hasActiveFilters) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Badge(
                                            containerColor = modernColors.primary,
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Text("!", fontSize = 10.sp, color = modernColors.onSurface)
                                        }
                                    }
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = if (hasActiveFilters) modernColors.primary else modernColors.onSurface
                                )
                            },
                            onClick = {
                                onFilterClick()
                                overflowExpanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Theme", color = modernColors.onSurface) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Brightness6,
                                    contentDescription = null,
                                    tint = modernColors.onSurface
                                )
                            },
                            onClick = {
                                onThemeClick()
                                overflowExpanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Refresh", color = modernColors.onSurface)
                                    if (hasNewUpdates) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(modernColors.error, shape = CircleShape)
                                        )
                                    }
                                }
                            },
                            leadingIcon = {
                                if (isRefreshing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = modernColors.primary
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = if (hasNewUpdates) modernColors.error else modernColors.onSurface
                                    )
                                }
                            },
                            onClick = {
                                onRefresh()
                                overflowExpanded = false
                            },
                            enabled = !isRefreshing
                        )

                        DropdownMenuItem(
                            text = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "User ID",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = modernColors.onSurface.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = userId,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = modernColors.primary,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "User ID",
                                    tint = modernColors.primary
                                )
                            },
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("User ID", userId)
                                clipboard.setPrimaryClip(clip)

                                // Show toast
                                Toast.makeText(context, "User ID copied to clipboard", Toast.LENGTH_SHORT).show()

                                overflowExpanded = false
                            },
                            enabled = true
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = modernColors.surface)
            )

            AnimatedVisibility(
                visible = searchActive && searchQuery.isEmpty() && recentSearches.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = modernColors.surface.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Searches",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = modernColors.primary
                            )
                            TextButton(onClick = onClearRecentSearches) {
                                Text("Clear All", color = modernColors.error, fontSize = 12.sp)
                            }
                        }

                        recentSearches.forEach { search ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRecentSearchClick(search) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        tint = modernColors.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = search,
                                        color = modernColors.onSurface,
                                        fontSize = 14.sp
                                    )
                                }
                                IconButton(
                                    onClick = onClearRecentSearches
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = modernColors.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            HorizontalDivider(
                                Modifier,
                                DividerDefaults.Thickness,
                                color = modernColors.onSurface.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }

            // Active filters chips
            AnimatedVisibility(
                visible = hasActiveFilters && !searchActive,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showOnlyFavorites) {
                        FilterChip(
                            onClick = onToggleFavorites,
                            label = { Text("Favorites Only", color = modernColors.tertiary) },
                            selected = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = modernColors.tertiary
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = modernColors.tertiary.copy(alpha = 0.2f),
                                selectedLabelColor = modernColors.tertiary
                            )
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        FilterChip(
                            onClick = { onSearchQueryChange("") },
                            label = { Text("Search: $searchQuery", color = modernColors.primary) },
                            selected = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = modernColors.primary
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = modernColors.primary.copy(alpha = 0.2f),
                                selectedLabelColor = modernColors.primary
                            )
                        )
                    }

                    if (hasActiveFilters) {
                        FilterChip(
                            onClick = onClearFilters,
                            label = { Text("Clear all", color = modernColors.onSurface) },
                            selected = false,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = modernColors.onSurface
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedShipmentCard(
    shipment: Shipment,
    modernColors: Helper.ModernColors,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val transition = updateTransition(targetState = isExpanded, label = "card")

    val cardElevation by transition.animateDp(label = "elevation") { expanded ->
        if (expanded) 8.dp else 2.dp
    }

    Card(
        onClick = { onClick() },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        colors = CardDefaults.cardColors(containerColor = modernColors.surface)
    ) {
        Column(
            modifier = Modifier.animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = getCarrierColor(shipment.carrier.code).copy(alpha = 0.15f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AnimatedContent(
                                targetState = shipment.isFavorite,
                                transitionSpec = {
                                    fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                                },
                                label = "favoriteAnimation"
                            ) { isFavorite ->
                                if (isFavorite) {
                                    Icon(
                                        Icons.Filled.Favorite,
                                        contentDescription = null,
                                        tint = modernColors.tertiary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                } else {
                                    Text(
                                        text = shipment.carrier.name.take(2).uppercase(),
                                        color = getCarrierColor(shipment.carrier.code),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = shipment.carrier.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = modernColors.onSurface
                            )

                            IconButton(
                                onClick = onFavoriteClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    if (shipment.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (shipment.isFavorite) modernColors.tertiary else modernColors.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = shipment.trackingNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = modernColors.onSurface.copy(alpha = 0.6f),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        ModernStatusChip(
                            status = shipment.lastStatus.label,
                            statusCode = shipment.lastStatus.code,
                            modernColors = modernColors
                        )

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                InfoRow(
                                    icon = Icons.Outlined.Update,
                                    text = "Updated: ${formatRelativeTime(shipment.lastUpdatedAt)}",
                                    color = modernColors.onSurface.copy(alpha = 0.7f)
                                )

                                shipment.estimatedDeliveryAt?.let {
                                    InfoRow(
                                        icon = Icons.Outlined.CalendarToday,
                                        text = "Est. delivery: ${formatDate(it)}",
                                        color = modernColors.success
                                    )
                                }

                                InfoRow(
                                    icon = Icons.Outlined.LocationOn,
                                    text = shipment.lastStatus.label,
                                    color = modernColors.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // Expand/Collapse indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Show less" else "Show more",
                    tint = modernColors.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ModernStatusChip(status: String, statusCode: String, modernColors: Helper.ModernColors) {
    val (color, icon) = when (statusCode) {
        "DELIVERED" -> modernColors.success to Icons.Default.CheckCircle
        "IN_TRANSIT" -> modernColors.info to Icons.Default.LocalShipping
        "OUT_FOR_DELIVERY" -> modernColors.warning to Icons.Default.DirectionsCar
        "EXCEPTION" -> modernColors.error to Icons.Default.Warning
        "LABEL_CREATED" -> modernColors.secondary to Icons.AutoMirrored.Filled.Label
        else -> modernColors.onSurface.copy(alpha = 0.5f) to Icons.AutoMirrored.Filled.Help
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernFilterBottomSheet(
    onDismiss: () -> Unit,
    currentFilter: StatusFilter?,
    onFilterSelected: (StatusFilter?) -> Unit,
    searchQuery: String,
    modernColors: Helper.ModernColors,
    onSearchClear: () -> Unit
) {
    val filterOptions = listOf(
        "All Orders" to StatusFilter.ALL,
        "In Transit" to StatusFilter.IN_TRANSIT,
        "Out for Delivery" to StatusFilter.OUT_FOR_DELIVERY,
        "Label Created" to StatusFilter.LABEL_CREATED,
        "Delivered" to StatusFilter.DELIVERED,
        "Exception" to StatusFilter.EXCEPTION
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = modernColors.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(modernColors.onSurface.copy(alpha = 0.3f), CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Filter Shipments",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = modernColors.onSurface
            )

            if (searchQuery.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = modernColors.primary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = modernColors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Searching: $searchQuery",
                                color = modernColors.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(onClick = onSearchClear) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = modernColors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = modernColors.onSurface.copy(alpha = 0.8f)
            )

            filterOptions.forEach { (label, filter) ->
                val isSelected = currentFilter == filter

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) modernColors.primary.copy(alpha = 0.15f)
                        else Color.Transparent
                    ),
                    onClick = { onFilterSelected(filter) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) modernColors.primary else modernColors.onSurface
                        )

                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = modernColors.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineIndicator(modifier: Modifier = Modifier, modernColors: Helper.ModernColors) {
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = modernColors.warning.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = modernColors.surface,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "You're offline. Showing cached data.",
                color = modernColors.surface,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ModernEmptyState(
    hasFilters: Boolean,
    modernColors: Helper.ModernColors,
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = modernColors.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Inbox,
                        contentDescription = null,
                        tint = modernColors.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Text(
                text = if (hasFilters) "No matches found" else "No shipments yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = modernColors.onSurface
            )

            Text(
                text = if (hasFilters) {
                    "Try adjusting your filters to see more results"
                } else {
                    "Your shipments will appear here once added"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = modernColors.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            if (hasFilters) {
                Button(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.buttonColors(containerColor = modernColors.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear all filters", color = modernColors.onSurface)
                }
            }
        }
    }
}

@Composable
fun ModernErrorState(
    message: String,
    modernColors: Helper.ModernColors,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = modernColors.error.copy(alpha = 0.1f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = modernColors.error,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = modernColors.onSurface
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = modernColors.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = modernColors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = modernColors.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again", color = modernColors.onSurface)
            }
        }
    }
}

private fun formatRelativeTime(timestamp: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        val date = format.parse(timestamp) ?: return "Unknown"

        val diff = System.currentTimeMillis() - date.time
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> SimpleDateFormat("MMM d", Locale.US).format(date)
        }
    } catch (e: Exception) {
        "Unknown"
    }
}