package taxiapp.core.domain.repository

import kotlinx.coroutines.flow.Flow
import taxiapp.core.model.Location

interface HomeRepository {

    fun getLocation(): Flow<Location>

    suspend fun postLocation(location: Location)
}