package taxiapp.core.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import taxiapp.core.common.network_status.ConnectivityObserver
import taxiapp.core.common.network_status.NetworkConnectionObserver

@Module
@InstallIn(SingletonComponent::class)
interface ConnectivityObserverModule {

    @Binds
    fun binConnectivityObserver(connectionObserver: NetworkConnectionObserver): ConnectivityObserver
}