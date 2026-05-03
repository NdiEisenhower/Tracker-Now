package com.eisen.trackernow.presentation.util

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object Helper {

    @Composable
    fun getModernColors(isDarkTheme: Boolean): ModernColors {
        return if (isDarkTheme) {
            ModernColors(
                primary = Color(0xFFBB86FC),
                secondary = Color(0xFF03DAC6),
                tertiary = Color(0xFFCF6679),
                surface = Color(0xFF1E1E1E),
                background = Color(0xFF121212),
                error = Color(0xFFCF6679),
                warning = Color(0xFFFFB74D),
                success = Color(0xFF81C784),
                info = Color(0xFF64B5F6),
                onSurface = Color.White
            )
        } else {
            ModernColors(
                primary = Color(0xFF6200EE),
                secondary = Color(0xFF03DAC6),
                tertiary = Color(0xFFEF5350),
                surface = Color(0xFFFFFFFF),
                background = Color(0xFFF5F5F5),
                error = Color(0xFFB00020),
                warning = Color(0xFFFF9800),
                success = Color(0xFF4CAF50),
                info = Color(0xFF2196F3),
                onSurface = Color.Black
            )
        }
    }

    data class ModernColors(
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val surface: Color,
        val background: Color,
        val error: Color,
        val warning: Color,
        val success: Color,
        val info: Color,
        val onSurface: Color
    )



    @Composable
    fun getModernColors(): ModernColors {
        val isDarkTheme = MaterialTheme.colorScheme.background == Color(0xFF121212) ||
                MaterialTheme.colorScheme.background == Color(0xFF1E1E1E) ||
                !isSystemInDarkTheme()
        return getModernColors(isDarkTheme)
    }

    @Composable
    fun rememberModernColors(): ModernColors {
        return getModernColors()
    }


     fun formatDateTime(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(timestamp) ?: return timestamp

            val outputFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.US)
            outputFormat.format(date)
        } catch (e: Exception) {
            timestamp
        }
    }

     fun formatDate(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(timestamp) ?: return timestamp

            val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
            outputFormat.format(date)
        } catch (e: Exception) {
            timestamp
        }
    }

    @Composable
    fun SafeImage(@DrawableRes id: Int, contentDescription: String?) {
        val context = LocalContext.current
        val imageBitmap = remember(id) {
            // This works for LayerDrawable, BitmapDrawable, VectorDrawable, etc.
            ContextCompat.getDrawable(context, id)?.toBitmap()?.asImageBitmap()
        }

        if (imageBitmap != null) {
            Image(bitmap = imageBitmap, contentDescription = contentDescription,  modifier = Modifier.size(54.dp)  )
        } else {
            // Fallback: Try the standard method if the custom one fails
            // This keeps your app from crashing if the resource is a standard type
            Image(painterResource(id), contentDescription,  modifier = Modifier.size(54.dp) )
        }
    }

    // In your Helper.kt or a separate file
    @Composable
    fun getStatusColor(statusCode: String): Color {
        val modernColors = rememberModernColors() // Your theme-aware colors

        return when (statusCode) {
            "DELIVERED" -> modernColors.success
            "IN_TRANSIT" -> modernColors.info
            "OUT_FOR_DELIVERY" -> modernColors.warning
            "EXCEPTION" -> modernColors.error
            "LABEL_CREATED" -> modernColors.secondary
            else -> Color.White.copy(alpha = 0.5f)
        }
    }

    // Also make getCarrierColor theme-aware
    @Composable
    fun getCarrierColor(carrierCode: String): Color {
        val modernColors = rememberModernColors()

        return when (carrierCode.lowercase()) {
            "fedex" -> Color(0xFF4B0082) // Purple
            "ups" -> Color(0xFF1A237E) // Dark Blue
            "usps" -> Color(0xFF0D47A1) // Blue
            "dhl" -> Color(0xFFFFC107) // Yellow
            "amazon" -> Color(0xFFFF9800) // Orange
            else -> modernColors.primary
        }
    }
}