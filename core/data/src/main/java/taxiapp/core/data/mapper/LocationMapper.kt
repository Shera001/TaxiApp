package taxiapp.core.data.mapper

import taxiapp.core.database.entity.LocationEntity
import taxiapp.core.domain.mapper.EntityMapper
import taxiapp.core.model.Location
import javax.inject.Inject

class LocationMapper @Inject constructor() : EntityMapper<LocationEntity?, Location?> {

    override fun mapFromEntity(entity: LocationEntity?): Location {
        return Location(entity?.latitude ?: 0.0, entity?.longitude ?: 0.0)
    }

    override fun mapToEntity(model: Location?): LocationEntity {
        return LocationEntity(
            latitude = model?.latitude,
            longitude = model?.longitude
        )
    }
}