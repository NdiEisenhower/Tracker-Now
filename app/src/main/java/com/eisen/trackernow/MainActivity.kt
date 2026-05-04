package com.eisen.trackernow

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eisen.trackernow.presentation.ui.LocalThemeManager
import com.eisen.trackernow.presentation.ui.ThemeManager
import com.eisen.trackernow.presentation.ui.ThemeMode
import com.eisen.trackernow.presentation.ui.navigation.NavGraph
import com.eisen.trackernow.ui.theme.TrackerNowTheme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager
    private var keepSplashScreen = false



    private fun getInitialShipmentId(): String? {
        return pendingShipmentId
    }

    companion object {
        private var pendingShipmentId: String? = null
        private const val EXTRA_SHIPMENT_ID = "shipment_id"
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val shipmentId = handleNotificationIntent(intent)
        shipmentId?.let {
            pendingShipmentId = it
        }

        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        val shipmentId = handleNotificationIntent(intent)
        shipmentId?.let {
            pendingShipmentId = it
        }
        enableEdgeToEdge()
        requestNotificationPermission()
        setContent {
            CompositionLocalProvider(
                LocalThemeManager provides themeManager
            ) {
                TrackNowApp(  initialShipmentId = getInitialShipmentId()){
                    keepSplashScreen = false
                }
            }
        }
    }

    private var notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")


        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }
    private fun handleNotificationIntent(intent: Intent): String? {
        return when {
            intent.action == Intent.ACTION_VIEW -> {
                intent.data?.lastPathSegment
            }
            intent.hasExtra(EXTRA_SHIPMENT_ID) -> {
                intent.getStringExtra(EXTRA_SHIPMENT_ID)
            }
            else -> null
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                     Log.d("MainActivity", "Notification permission already granted")

                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else { Log.d("MainActivity", "Notification permission not required for Android 12 and below")

        }
    }
}


@Composable
fun TrackNowApp(
    initialShipmentId: String? = null,
    onDataLoaded: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val themeManager = remember { application.themeManager }

    val themeMode by themeManager.getThemeMode().collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val window = (LocalActivity.current as Activity).window

    DisposableEffect(darkTheme) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = !darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme

        onDispose { }
    }

    TrackerNowTheme(
        darkTheme = darkTheme,
        content = {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent
            ) { innerPadding ->
                NavGraph(
                    modifier = Modifier.padding(innerPadding),
                    initialShipmentId = initialShipmentId,
                    onDataLoaded = onDataLoaded
                )
            }
        }
    )
}
