package taxiapp.core.common.permissions

import taxiapp.core.common.R
import taxiapp.core.common.util.ResourceProvider
import javax.inject.Inject

class LocationTextProvider @Inject constructor(
    private val resourceProvider: ResourceProvider
) : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            resourceProvider.getString(R.string.location_permanently_declined)
        } else {
            resourceProvider.getString(R.string.location_declined)
        }
    }
}