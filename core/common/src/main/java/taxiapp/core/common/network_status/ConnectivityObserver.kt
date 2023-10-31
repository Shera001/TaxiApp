package taxiapp.core.common.network_status

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {

    fun observe(): Flow<Boolean>
}