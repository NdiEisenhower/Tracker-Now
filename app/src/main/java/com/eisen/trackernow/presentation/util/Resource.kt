package com.eisen.trackernow.presentation.util

sealed class Resource<out T>(
    val data: T? = null,
    val message: String? = null,
    val isOffline: Boolean = false
) {
    object Loading : Resource<Nothing>()

    data class Success<T>(
        val value: T,
        val offline: Boolean = false
    ) : Resource<T>(data = value, isOffline = offline)

    data class Error(
        val errorMessage: String,
        val throwable: Throwable? = null,
        val offline: Boolean = false
    ) : Resource<Nothing>(message = errorMessage)
}