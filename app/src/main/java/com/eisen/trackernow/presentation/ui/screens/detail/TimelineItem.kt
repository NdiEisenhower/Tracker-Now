package com.eisen.trackernow.presentation.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eisen.trackernow.domain.model.StatusTimeline
import com.eisen.trackernow.presentation.util.Helper
import com.eisen.trackernow.presentation.util.Helper.ModernColors
import com.eisen.trackernow.presentation.util.Helper.getStatusColor

@Composable
fun TimelineItem(
    status: StatusTimeline,
    isLast: Boolean,
    modernColors: ModernColors
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 24.dp else 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(if (isTablet) 40.dp else 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (isTablet) 14.dp else 12.dp)
                    .clip(CircleShape)
                    .background(color = getStatusColor(status.code))
                    .shadow(2.dp, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(if (isTablet) 100.dp else 80.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    getStatusColor(status.code),
                                    modernColors.onSurface.copy(alpha = 0.1f)
                                )
                            )
                        )
                )
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = if (isTablet) 600.dp else androidx.compose.ui.unit.Dp.Unspecified),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = modernColors.surface.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        status.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = modernColors.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )

                    when (status.code) {
                        "DELIVERED" -> Icon(
                            Icons.Default.CheckCircle,
                            tint = modernColors.success,
                            modifier = Modifier.size(20.dp),
                            contentDescription = "Delivered"
                        )
                        "IN_TRANSIT" -> Icon(
                            Icons.Default.LocalShipping,
                            tint = modernColors.info,
                            modifier = Modifier.size(20.dp),
                            contentDescription = "In Transit"
                        )
                        "OUT_FOR_DELIVERY" -> Icon(
                            Icons.Default.DirectionsCar,
                            tint = modernColors.warning,
                            modifier = Modifier.size(20.dp),
                            contentDescription = "Out for Delivery"
                        )
                        "EXCEPTION" -> Icon(
                            Icons.Default.Warning,
                            tint = modernColors.error,
                            modifier = Modifier.size(20.dp),
                            contentDescription = "Exception"
                        )
                        else -> Icon(
                            Icons.Default.Schedule,
                            tint = modernColors.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp),
                            contentDescription = "Status"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                status.location?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp),
                            tint = modernColors.onSurface.copy(alpha = 0.6f),
                            contentDescription = "Location"
                        )
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = modernColors.onSurface.copy(alpha = 0.8f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        modifier = Modifier.size(16.dp),
                        tint = modernColors.onSurface.copy(alpha = 0.6f),
                        contentDescription = "Time"
                    )
                    Text(
                        Helper.formatDateTime(status.time),
                        style = MaterialTheme.typography.bodySmall,
                        color = modernColors.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}