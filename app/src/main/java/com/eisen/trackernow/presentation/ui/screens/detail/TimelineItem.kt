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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color = getStatusColor(status.code))
                    .shadow(2.dp, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
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

        Spacer(modifier = Modifier.width(16.dp))

        Card(
            modifier = Modifier.weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = modernColors.surface.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        status.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = modernColors.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    when (status.code) {
                        "DELIVERED" -> Icon(
                            Icons.Default.CheckCircle,
                            tint = modernColors.success,
                            modifier = Modifier.size(18.dp),
                            contentDescription = ""
                        )
                        "IN_TRANSIT" -> Icon(
                            Icons.Default.LocalShipping,
                            tint = modernColors.info,
                            modifier = Modifier.size(18.dp),
                            contentDescription = ""
                        )
                        "OUT_FOR_DELIVERY" -> Icon(
                            Icons.Default.DirectionsCar,
                            tint = modernColors.warning,
                            modifier = Modifier.size(18.dp),
                            contentDescription = ""
                        )
                        "EXCEPTION" -> Icon(
                            Icons.Default.Warning,
                            tint = modernColors.error,
                            modifier = Modifier.size(18.dp),
                            contentDescription = ""
                        )
                        else -> Icon(
                            Icons.Default.Schedule,
                            tint = modernColors.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp),
                            contentDescription = ""
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                status.location?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            modifier = Modifier.size(14.dp),
                            tint = modernColors.onSurface.copy(alpha = 0.6f),
                            contentDescription = ""
                        )
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = modernColors.onSurface.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        modifier = Modifier.size(14.dp),
                        tint = modernColors.onSurface.copy(alpha = 0.6f),
                        contentDescription = ""
                    )
                    Text(
                        Helper.formatDateTime(status.time),
                        style = MaterialTheme.typography.bodySmall,
                        color = modernColors.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Remove the duplicate label display here
                // The label is already shown at the top of the card
            }
        }
    }
}