package com.eisen.trackernow.domain.repository

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.eisen.trackernow.MainActivity
import com.eisen.trackernow.R
import com.eisen.trackernow.data.PushUpdate
import com.eisen.trackernow.data.UpdateChanges
import com.eisen.trackernow.data.remote.PushUpdateRepository
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.eisen.trackernow.domain.model.Status
import com.google.firebase.database.IgnoreExtraProperties
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.jvm.java
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.eisen.trackernow.presentation.util.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


// Data classes with no-argument constructors for Firebase
@IgnoreExtraProperties
data class UpdateData(
    var type: String = "",
    var shipmentId: String = "",
    var updatedAt: String = "",
    var timestamp: Long = 0,
    var changes: UpdateChanges = UpdateChanges()
) {
    // No-argument constructor required for Firebase
    constructor() : this("", "", "", 0, UpdateChanges())
}



// Fixed Status class with no-argument constructor
@IgnoreExtraProperties
data class Status(
    var code: String = "",
    var label: String = ""
) {
    // No-argument constructor required for Firebase
    constructor() : this("", "")
}

// Data class for update notifications
/*data class UpdateData(
    val type: String = "",
    val shipmentId: String = "",
    val updatedAt: String = "",
    val timestamp: Long = 0,
    val changes: UpdateChanges = UpdateChanges()
)*/

/*data class UpdateChanges(
    val newStatus: Status = Status("", ""),
    val location: String = "",
    val notes: String = ""
)*/
@AndroidEntryPoint
class UpdateListenerService : Service() {
    @Inject
    lateinit var pushUpdateRepository: PushUpdateRepository
    @Inject
    lateinit var dataStoreManager: DataStoreManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var updateJob: Job? = null

    companion object {
        private const val TAG = "UpdateListenerService"
        private const val CHANNEL_ID = "shipment_updates"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        //startForeground(NOTIFICATION_ID, createForegroundNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = getUserId()
        startListening(userId)
        return START_STICKY
    }

    private fun getUserId(): String {
        val userId = runBlocking {
            dataStoreManager.observeUserId().first()
        }
        registerUserInFirebase(userId)
        return userId
    }



    private fun registerUserInFirebase(userId: String) {
        val database =  FirebaseDatabase.getInstance()
        database.getReference("user_updates/$userId")
            .child("pushedUpdates")
            .setValue(true)
            .addOnSuccessListener {
                Log.d(TAG, "User registered in Firebase")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to register user: ${e.message}")
            }
    }

    private fun startListening(userId: String) {
        updateJob = serviceScope.launch {
            pushUpdateRepository.listenForUpdatesRealTime(userId)
                .collect { pushUpdate ->
                    Log.d(TAG, "Received push update: ${pushUpdate.shipmentId}")
                    showNotification(pushUpdate)
                }
        }
    }

    private fun showNotification(pushUpdate: PushUpdate) {
        // Create a unique notification ID based on shipment ID
        val notificationId = pushUpdate.shipmentId.hashCode()

        // Create deep link intent
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "tracknow://shipment/${pushUpdate.shipmentId}".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("shipment_id", pushUpdate.shipmentId)
            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationText = buildString {
            append("${pushUpdate.changes.newStatus.label}: ${pushUpdate.shipmentId.takeLast(8)}\n")
            append("Location: ${pushUpdate.changes.location}")
            if (pushUpdate.changes.notes.isNotEmpty()) {
                append("\nNotes: ${pushUpdate.changes.notes}")
            }
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Shipment Update")
            .setContentText(pushUpdate.changes.newStatus.label)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(
                NotificationCompat.Action.Builder(
                    0,
                    "View Details",
                    pendingIntent
                ).build()
            )
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Shipment Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time shipment status updates"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }



    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        serviceScope.cancel()
    }
}