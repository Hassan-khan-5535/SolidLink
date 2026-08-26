package com.hyperdrop.app.di

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.hyperdrop.app.data.transport.nearby.NearbyTransportRepositoryImpl
import com.hyperdrop.app.domain.repository.NearbyTransportRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransportModule {

    @Binds
    @Singleton
    abstract fun bindNearbyTransportRepository(
        impl: NearbyTransportRepositoryImpl
    ): NearbyTransportRepository

    companion object {
        @Provides
        @Singleton
        fun provideConnectionsClient(@ApplicationContext context: Context): ConnectionsClient {
            return Nearby.getConnectionsClient(context)
        }
    }
}
