package com.eisen.trackernow.presentation.ui.screens.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eisen.trackernow.domain.model.StatusTimeline
import com.eisen.trackernow.presentation.util.Helper
import com.eisen.trackernow.presentation.util.Helper.ModernColors
import com.eisen.trackernow.presentation.util.Helper.getStatusColor

@Composable
fun AnimatedTimelineItem(
    status: StatusTimeline,
    isLast: Boolean,
    index: Int,
    modernColors: ModernColors
) {
    val delay = index * 100
    val transition = remember { MutableTransitionState(false) }
    transition.targetState = true

    AnimatedVisibility(
        visible = transition.currentState,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = delay
            )
        ) + slideInHorizontally(
            initialOffsetX = { it / 2 },
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = delay
            )
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Timeline indicator column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(24.dp)
            ) {
                // Animated status dot
                val scale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(color = getStatusColor(status.code))
                        .shadow(4.dp, CircleShape)
                )

                // Connecting line (except for last item)
                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .height(80.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        getStatusColor(status.code),
                                        modernColors.onSurface.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = modernColors.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = status.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = modernColors.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            // Status icon based on code
                            when (status.code) {
                                "DELIVERED" -> Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Delivered",
                                    tint = getStatusColor(status.code),
                                    modifier = Modifier.size(20.dp)
                                )
                                "IN_TRANSIT" -> Icon(
                                    Icons.Default.LocalShipping,
                                    contentDescription = "In Transit",
                                    tint = getStatusColor(status.code),
                                    modifier = Modifier.size(20.dp)
                                )
                                "EXCEPTION" -> Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Exception",
                                    tint = getStatusColor(status.code),
                                    modifier = Modifier.size(20.dp)
                                )
                                else -> Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = "Status",
                                    tint = getStatusColor(status.code),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        status.location?.let {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    modifier = Modifier.size(14.dp),
                                    tint = modernColors.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = modernColors.onSurface.copy(alpha = 0.8f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Time",
                                modifier = Modifier.size(14.dp),
                                tint = modernColors.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = Helper.formatDateTime(status.time),
                                style = MaterialTheme.typography.bodySmall,
                                color = modernColors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}