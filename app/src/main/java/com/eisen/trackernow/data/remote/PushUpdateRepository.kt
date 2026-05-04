package com.eisen.trackernow.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.eisen.trackernow.data.PushUpdate
import com.eisen.trackernow.data.UpdateChanges
import com.eisen.trackernow.data.remote.dto.UpdateDataDto
import com.eisen.trackernow.presentation.util.DataStoreManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import kotlin.getValue
import kotlin.jvm.java
import androidx.core.content.edit

@Singleton
class PushUpdateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: FirebaseDatabase,
    private val dataStoreManager: DataStoreManager,
    private val moshi: Moshi
) {

    companion object {
        private const val TAG = "PushUpdateRepository"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences("tracker_now", Context.MODE_PRIVATE)
    private val updateDataAdapter: JsonAdapter<UpdateDataDto> by lazy {
        moshi.adapter(UpdateDataDto::class.java)
    }

    fun observeUserId(): Flow<String> {
        return dataStoreManager.observeUserId()
    }
    suspend fun getUserId(): String {
        return dataStoreManager.getUserId()
    }

    fun listenForUpdates(userId: String): Flow<PushUpdate> = callbackFlow {
        val updatesRef = database.getReference("user_updates/$userId/pushedUpdates")

        Log.d(TAG, "Listening for updates at: user_updates/$userId/pushedUpdates")

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    for (childSnapshot in snapshot.children) {
                        // Get the raw value and convert to JSON string
                        val rawValue = childSnapshot.value
                        val jsonString = when (rawValue) {
                            is Map<*, *> -> {
                                JSONObject(rawValue).toString()
                            }
                            is String -> rawValue
                            else -> rawValue.toString()
                        }

                        Log.d(TAG, "Raw JSON: $jsonString")

                        val updateData = updateDataAdapter.fromJson(jsonString)

                        if (updateData != null && updateData.shipmentId.isNotEmpty()) {
                            Log.d(TAG, "Successfully parsed update for: ${updateData.shipmentId}")

                            val pushUpdate = PushUpdate(
                                type = updateData.type,
                                shipmentId = updateData.shipmentId,
                                updatedAt = updateData.updatedAt,
                                timestamp = updateData.timestamp,
                                changes = UpdateChanges(
                                    newStatus = updateData.changes.newStatus,
                                    location = updateData.changes.location,
                                    notes = updateData.changes.notes
                                )
                            )

                            trySend(pushUpdate)

                            childSnapshot.ref.removeValue()
                                .addOnSuccessListener {
                                    Log.d(TAG, "Update removed successfully")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Failed to remove update: ${e.message}")
                                }
                        } else {
                            Log.w(TAG, "Failed to parse update or empty shipment ID")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing updates: ${e.message}", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
                close(error.toException())
            }
        }

        updatesRef.addValueEventListener(valueEventListener)

        awaitClose {
            updatesRef.removeEventListener(valueEventListener)
        }
    }


    fun listenForUpdatesRealTime(userId: String): Flow<PushUpdate> = callbackFlow {
        val updatesRef = database.getReference("user_updates/$userId/pushedUpdates")

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val rawValue = snapshot.value
                    val jsonString = when (rawValue) {
                        is Map<*, *> -> JSONObject(rawValue).toString()
                        else -> rawValue.toString()
                    }

                    Log.d(TAG, "Child added JSON: $jsonString")

                    val updateData = updateDataAdapter.fromJson(jsonString)

                    if (updateData != null && updateData.shipmentId.isNotEmpty()) {
                        val pushUpdate = PushUpdate(
                            type = updateData.type,
                            shipmentId = updateData.shipmentId,
                            updatedAt = updateData.updatedAt,
                            timestamp = updateData.timestamp,
                            changes = UpdateChanges(
                                newStatus = updateData.changes.newStatus,
                                location = updateData.changes.location,
                                notes = updateData.changes.notes
                            )
                        )

                        trySend(pushUpdate)

                        snapshot.ref.removeValue()
                            .addOnSuccessListener {
                                Log.d(TAG, "Update removed successfully")
                            }
                    } else {
                        Log.w(TAG, "Failed to parse child update")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing child update: ${e.message}", e)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
                close(error.toException())
            }
        }

        updatesRef.addChildEventListener(childEventListener)

        awaitClose {
            updatesRef.removeEventListener(childEventListener)
        }
    }
    fun saveRefreshTime() {
        prefs.edit { putLong("last_refresh_time", System.currentTimeMillis()) }
    }

    fun getLastRefreshTime(): Long = prefs.getLong("last_refresh_time", 0)

    fun getLastUpdateTimestamp(): Long = prefs.getLong("last_update_timestamp", 0)

    fun saveLastUpdateTimestamp(timestamp: Long) {
        prefs.edit { putLong("last_update_timestamp", timestamp) }
    }

}