package com.eisen.trackernow

import android.app.Application
import android.content.Intent
import com.eisen.trackernow.domain.repository.UpdateListenerService
import com.eisen.trackernow.presentation.ui.ThemeManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

@HiltAndroidApp
class Application : Application(){
    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        startNotificationService()
    }
    private fun startNotificationService() {
        val serviceIntent = Intent(this, UpdateListenerService::class.java)
        startService(serviceIntent)
    }
}
