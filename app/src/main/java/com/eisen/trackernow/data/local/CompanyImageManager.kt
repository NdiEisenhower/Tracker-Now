package com.eisen.trackernow.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Singleton
class CompanyImageManager @Inject constructor(private val context: Context) {

    private val imageCacheDir = File(context.cacheDir, "carrier_images")

    init {
        if (!imageCacheDir.exists()) {
            imageCacheDir.mkdirs()
        }
    }

    suspend fun saveCarrierImage(carrierCode: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        val file = File(imageCacheDir, "${carrierCode}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    suspend fun getCarrierImage(carrierCode: String): Bitmap? = withContext(Dispatchers.IO) {
        val file = File(imageCacheDir, "${carrierCode}.png")
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    fun getCarrierImageFile(carrierCode: String): File? {
        val file = File(imageCacheDir, "${carrierCode}.png")
        return if (file.exists()) file else null
    }
}