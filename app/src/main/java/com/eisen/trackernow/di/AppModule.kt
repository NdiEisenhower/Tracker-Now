package com.eisen.trackernow.di

import android.content.Context
import com.eisen.trackernow.data.local.ShipmentDatabase
import com.eisen.trackernow.data.local.dao.ShipmentDao
import com.eisen.trackernow.data.local.dao.StatusDao
import com.eisen.trackernow.data.remote.PushUpdateRepository
import com.eisen.trackernow.data.remote.ShipmentApi
import com.eisen.trackernow.data.repository.ShipmentRepositoryImpl
import com.eisen.trackernow.domain.repository.NetworkMonitor
import com.eisen.trackernow.domain.repository.NetworkMonitorImpl
import com.eisen.trackernow.domain.repository.ShipmentRepository
import com.eisen.trackernow.domain.usecase.FilterShipmentsUseCase
import com.eisen.trackernow.domain.usecase.GetShipmentDetailUseCase
import com.eisen.trackernow.domain.usecase.GetShipmentsUseCase
import com.eisen.trackernow.domain.usecase.RefreshShipmentsUseCase
import com.eisen.trackernow.domain.usecase.SearchShipmentsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideShipmentDatabase(@ApplicationContext context: Context): ShipmentDatabase {
        return ShipmentDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideShipmentDao(database: ShipmentDatabase): ShipmentDao {
        return database.shipmentDao()
    }

    @Provides
    @Singleton
    fun provideStatusDao(database: ShipmentDatabase): StatusDao {
        return database.statusDao()
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitorImpl(context)
    }

    @Provides
    @Singleton
    fun provideShipmentRepository(
        shipmentDao: ShipmentDao,
        statusDao: StatusDao,
        api: ShipmentApi,
        networkMonitor: NetworkMonitor,
        pushUpdateRepository: PushUpdateRepository,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): ShipmentRepository {
        return ShipmentRepositoryImpl(
            shipmentDao = shipmentDao,
            statusDao = statusDao,
            api = api,
            networkMonitor = networkMonitor,
            pushUpdateRepository = pushUpdateRepository,
            ioDispatcher = ioDispatcher,

        )
    }

    @Provides
    @Singleton
    fun provideGetShipmentsUseCase(repository: ShipmentRepository): GetShipmentsUseCase {
        return GetShipmentsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRefreshShipmentsUseCase(repository: ShipmentRepository): RefreshShipmentsUseCase {
        return RefreshShipmentsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSearchShipmentsUseCase(repository: ShipmentRepository): SearchShipmentsUseCase {
        return SearchShipmentsUseCase()
    }

    @Provides
    @Singleton
    fun provideFilterShipmentsUseCase(repository: ShipmentRepository): FilterShipmentsUseCase {
        return FilterShipmentsUseCase()
    }

    @Provides
    @Singleton
    fun provideGetShipmentDetailUseCase(repository: ShipmentRepository): GetShipmentDetailUseCase {
        return GetShipmentDetailUseCase(repository)
    }
}