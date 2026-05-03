package com.eisen.trackernow.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class FavoritesManager @Inject constructor( @ApplicationContext private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds = _favoriteIds.asStateFlow()

    init {
        loadFavoritesFromPrefs()
    }

    private fun loadFavoritesFromPrefs() {
        val favoritesSet = prefs.getStringSet("favorite_ids", emptySet()) ?: emptySet()
        _favoriteIds.value = favoritesSet
    }

    suspend fun addFavorite(id: String) {
        val currentSet = _favoriteIds.value.toMutableSet()
        currentSet.add(id)
        saveFavorites(currentSet)
    }

    suspend fun removeFavorite(id: String) {
        val currentSet = _favoriteIds.value.toMutableSet()
        currentSet.remove(id)
        saveFavorites(currentSet)
    }

    private fun saveFavorites(set: Set<String>) {
        prefs.edit().putStringSet("favorite_ids", set).apply()
        _favoriteIds.value = set
    }

    suspend fun getFavoriteIds(): Flow<Set<String>> = favoriteIds
}