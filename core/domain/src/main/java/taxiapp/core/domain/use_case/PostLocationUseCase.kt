package taxiapp.core.domain.use_case

import taxiapp.core.domain.repository.HomeRepository
import taxiapp.core.model.Location
import javax.inject.Inject

class PostLocationUseCase @Inject constructor(
    private val repository: HomeRepository
) {

    suspend operator fun invoke(location: Location) = repository.postLocation(location)
}