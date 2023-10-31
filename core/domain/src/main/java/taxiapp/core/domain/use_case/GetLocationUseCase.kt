package taxiapp.core.domain.use_case

import kotlinx.coroutines.flow.Flow
import taxiapp.core.domain.repository.HomeRepository
import taxiapp.core.model.Location
import javax.inject.Inject

class GetLocationUseCase @Inject constructor(
    private val repository: HomeRepository
) {

    operator fun invoke(): Flow<Location> = repository.getLocation()
}