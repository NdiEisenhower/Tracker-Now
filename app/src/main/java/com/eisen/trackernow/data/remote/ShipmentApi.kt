package com.eisen.trackernow.data.remote

import com.eisen.trackernow.data.remote.dto.AddressDto
import com.eisen.trackernow.data.remote.dto.CarrierDto
import com.eisen.trackernow.data.remote.dto.ShipmentDetailResponse
import com.eisen.trackernow.data.remote.dto.ShipmentDto
import com.eisen.trackernow.data.remote.dto.ShipmentsResponse
import com.eisen.trackernow.data.remote.dto.StatusDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface ShipmentApi {
    @GET("shipments.json")
    suspend fun getShipments(): Map<String, ShipmentDto>

    @GET("shipment_details/{id}.json")
    suspend fun getShipmentDetail(@Path("id") id: String): ShipmentDetailResponse
}