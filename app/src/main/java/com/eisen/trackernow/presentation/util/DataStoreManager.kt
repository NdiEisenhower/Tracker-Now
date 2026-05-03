package com.eisen.trackernow.presentation.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val RECENT_SEARCHES_KEY = stringPreferencesKey("recent_searches")
        val THEME_PREFERENCE_KEY = stringPreferencesKey("theme_preference")
        val LAST_REFRESH_KEY = stringPreferencesKey("last_refresh")
        val SHOW_ONLY_FAVORITES_KEY = stringPreferencesKey("show_only_favorites")
        val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    val dataStore: DataStore<Preferences> = context.dataStore


    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun getUserId(): String {
        val preferences = dataStore.data.first()
        var userId = preferences[USER_ID_KEY]
        if (userId.isNullOrEmpty()) {
            userId = generateUserId()
            saveUserId(userId)
        }
        return userId
    }

    private fun generateUserId(): String {
        return "android_${System.currentTimeMillis()}_${(Math.random() * 10000).toInt()}"
    }

    fun observeUserId(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[USER_ID_KEY] ?: generateAndSaveUserId()
        }
    }

    private suspend fun generateAndSaveUserId(): String {
        val userId = generateUserId()
        saveUserId(userId)
        return userId
    }
    suspend fun saveRecentSearches(searches: List<String>) {
        dataStore.edit { preferences ->
            val searchesString = searches.joinToString(",")
            preferences[RECENT_SEARCHES_KEY] = searchesString
        }
    }

    fun getRecentSearches(): Flow<List<String>> {
        return dataStore.data.map { preferences ->
            val searchesString = preferences[RECENT_SEARCHES_KEY] ?: ""
            if (searchesString.isNotEmpty()) {
                searchesString.split(",").filter { it.isNotEmpty() }
            } else {
                emptyList()
            }
        }
    }

    suspend fun clearRecentSearches() {
        dataStore.edit { preferences ->
            preferences.remove(RECENT_SEARCHES_KEY)
        }
    }

    suspend fun saveThemePreference(themeMode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_PREFERENCE_KEY] = themeMode
        }
    }

    fun getThemePreference(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[THEME_PREFERENCE_KEY] ?: "SYSTEM"
        }
    }

    suspend fun saveLastRefreshTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_REFRESH_KEY] = timestamp.toString()
        }
    }

    fun getLastRefreshTime(): Flow<Long> {
        return dataStore.data.map { preferences ->
            preferences[LAST_REFRESH_KEY]?.toLongOrNull() ?: 0L
        }
    }

    suspend fun saveShowOnlyFavorites(showOnlyFavorites: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_ONLY_FAVORITES_KEY] = showOnlyFavorites.toString()
        }
    }

    fun getShowOnlyFavorites(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[SHOW_ONLY_FAVORITES_KEY]?.toBoolean() ?: false
        }
    }
}