package com.eisen.trackernow.presentation.ui.screens.detail

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eisen.trackernow.domain.model.ShipmentDetail
import com.eisen.trackernow.presentation.ui.LocalThemeManager
import com.eisen.trackernow.presentation.ui.ThemeMode
import com.eisen.trackernow.presentation.util.Helper
import com.eisen.trackernow.presentation.util.Helper.ModernColors
import com.eisen.trackernow.presentation.util.Resource
import com.eisen.trackernow.presentation.viewmodel.ShipmentDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ShipmentDetailViewModel = hiltViewModel()
) {
    val detailState by viewModel.shipmentDetail.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val themeManager = LocalThemeManager.current
    val themeMode by themeManager.getThemeMode().collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

    val isDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val modernColors = Helper.getModernColors(isDarkTheme)

    Scaffold(
        containerColor = modernColors.background,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    when (val state = detailState) {
                        is Resource.Success -> {
                            Text(
                                text = state.data?.shipment?.carrier?.name ?: "Shipment Details",
                                fontWeight = FontWeight.Bold,
                                color = modernColors.onSurface
                            )
                        }
                        else -> Text("Shipment Details", color = modernColors.onSurface)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = modernColors.onSurface
                        )
                    }
                },
                actions = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = modernColors.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = detailState) {
                is Resource.Loading -> {
                    LoadingState(modernColors = modernColors)
                }

                is Resource.Success -> {
                    state.data?.let { data ->
                        ShipmentDetailContent(
                            detail = data,
                            modernColors = modernColors,
                            lazyListState = lazyListState
                        )
                    }
                }

                is Resource.Error -> {
                    ErrorState(
                        message = state.message ?: "Failed to load shipment details",
                        onRetry = { viewModel.retry() },
                        modernColors = modernColors
                    )
                }
            }
        }
    }
}

@Composable
fun ShipmentDetailContent(
    detail: ShipmentDetail,
    modernColors: ModernColors,
    lazyListState: androidx.compose.foundation.lazy.LazyListState
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val uniqueTimeline = remember(detail.timeline) {
        detail.timeline
            .distinctBy { "${it.time}_${it.location}_${it.label}" }
            .sortedByDescending { it.time }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = if (isTablet) 32.dp else 16.dp,
            vertical = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(if (isTablet) 24.dp else 16.dp)
    ) {
        item(key = "info_card") {
            ShipmentInfoCard(detail = detail, modernColors = modernColors)
        }

        item(key = "timeline_header") {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tracking Timeline",
                    style = if (isTablet)
                        MaterialTheme.typography.headlineMedium
                    else
                        MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = modernColors.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${uniqueTimeline.size} status updates",
                    style = if (isTablet)
                        MaterialTheme.typography.bodyMedium
                    else
                        MaterialTheme.typography.bodySmall,
                    color = modernColors.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        itemsIndexed(
            items = uniqueTimeline,
            key = { index, item -> "${item.time}_${item.location}_${item.label}_$index" }
        ) { index, status ->
            TimelineItem(
                status = status,
                isLast = index == uniqueTimeline.size - 1,
                modernColors = modernColors
            )
        }
    }
}

@Composable
fun LoadingState(modernColors: ModernColors) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = modernColors.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Loading shipment details...",
                color = modernColors.onSurface.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modernColors: ModernColors
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
                        Icons.AutoMirrored.Filled.ArrowBack,
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            androidx.compose.material3.Button(
                onClick = onRetry,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = modernColors.primary
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = modernColors.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again", color = modernColors.onSurface)
            }
        }
    }
}