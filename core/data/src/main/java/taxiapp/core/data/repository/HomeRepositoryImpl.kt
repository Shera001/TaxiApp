package taxiapp.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import taxiapp.core.common.di.IODispatcher
import taxiapp.core.data.mapper.LocationMapper
import taxiapp.core.database.dao.LocationDao
import taxiapp.core.domain.repository.HomeRepository
import taxiapp.core.model.Location
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val locationMapper: LocationMapper,
    private val dao: LocationDao,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : HomeRepository {

    override fun getLocation(): Flow<Location> = dao.getLocations()
        .map { value -> locationMapper.mapFromEntity(value) }

    override suspend fun postLocation(location: Location) = withContext(ioDispatcher) {
        dao.insert(locationMapper.mapToEntity(location))
    }
}