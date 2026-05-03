package com.eisen.trackernow.presentation.ui.screens.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eisen.trackernow.domain.model.ShipmentDetail
import com.eisen.trackernow.presentation.util.Helper
import com.eisen.trackernow.presentation.util.Helper.ModernColors
import com.eisen.trackernow.presentation.util.Helper.formatDate
import com.eisen.trackernow.presentation.util.Helper.getCarrierColor
import com.eisen.trackernow.presentation.util.Helper.getStatusColor

@Composable
fun ShipmentInfoCard(detail: ShipmentDetail, modernColors: ModernColors) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = modernColors.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Section with Carrier and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Carrier Logo
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = getCarrierColor(detail.shipment.carrier.code).copy(alpha = 0.15f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = detail.shipment.carrier.name.take(2).uppercase(),
                                color = getCarrierColor(detail.shipment.carrier.code),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = detail.shipment.carrier.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = modernColors.onSurface
                        )
                        Text(
                            text = detail.shipment.trackingNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = modernColors.onSurface.copy(alpha = 0.6f),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }

                // Status Badge
                StatusBadge(
                    statusCode = detail.shipment.lastStatus.code,
                    statusLabel = detail.shipment.lastStatus.label,
                    modernColors = modernColors
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider
            Divider(
                color = modernColors.onSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Route Information
            Text(
                text = "Route Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = modernColors.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Origin and Destination
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Origin
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = "Origin",
                            modifier = Modifier.size(14.dp),
                            tint = modernColors.primary
                        )
                        Text(
                            text = "FROM",
                            style = MaterialTheme.typography.labelSmall,
                            color = modernColors.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = detail.shipment.origin.city,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = modernColors.onSurface
                    )
                    Text(
                        text = detail.shipment.origin.country,
                        style = MaterialTheme.typography.bodySmall,
                        color = modernColors.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Arrow
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "to",
                    tint = modernColors.primary,
                    modifier = Modifier.size(24.dp)
                )

                // Destination
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "TO",
                            style = MaterialTheme.typography.labelSmall,
                            color = modernColors.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Destination",
                            modifier = Modifier.size(14.dp),
                            tint = modernColors.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = detail.shipment.destination.city,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = modernColors.onSurface,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = detail.shipment.destination.country,
                        style = MaterialTheme.typography.bodySmall,
                        color = modernColors.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.End
                    )
                }
            }

            // Expandable Details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = modernColors.onSurface.copy(alpha = 0.1f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Estimated Delivery - Improved styling
                    detail.shipment.estimatedDeliveryAt?.let { deliveryDate ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = modernColors.success.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(0.4f)
                                ) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = "Estimated Delivery",
                                        tint = modernColors.success,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Est. Delivery",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = modernColors.onSurface.copy(alpha = 0.8f)
                                    )
                                }

                                Text(
                                    text = Helper.formatDateTime(deliveryDate),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = modernColors.success,
                                    modifier = Modifier.weight(0.6f),
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Last Updated
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(0.35f)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Last Updated",
                                tint = modernColors.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Last Updated",
                                style = MaterialTheme.typography.bodyMedium,
                                color = modernColors.onSurface.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            text = Helper.formatDateTime(detail.shipment.lastUpdatedAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = modernColors.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.weight(0.65f),
                            textAlign = TextAlign.End,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tracking ID
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(0.35f)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Tracking ID",
                                tint = modernColors.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Tracking ID",
                                style = MaterialTheme.typography.bodyMedium,
                                color = modernColors.onSurface.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            text = detail.shipment.id.takeLast(8),
                            style = MaterialTheme.typography.bodySmall,
                            color = modernColors.primary,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.weight(0.65f),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Expand/Collapse Button
            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = modernColors.primary
                )
            ) {
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = modernColors.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isExpanded) "Show less details" else "Show more details",
                    fontSize = 12.sp,
                    color = modernColors.primary
                )
            }
        }
    }
}

@Composable
fun StatusBadge(statusCode: String, statusLabel: String, modernColors: ModernColors) {
    val color = getStatusColor(statusCode)
    val icon = when (statusCode) {
        "DELIVERED" -> Icons.Default.CheckCircle
        "IN_TRANSIT" -> Icons.Default.LocalShipping
        "OUT_FOR_DELIVERY" -> Icons.Default.DirectionsCar
        "EXCEPTION" -> Icons.Default.Warning
        "LABEL_CREATED" -> Icons.Default.Label
        else -> Icons.Default.Help
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                text = statusLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = color,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}