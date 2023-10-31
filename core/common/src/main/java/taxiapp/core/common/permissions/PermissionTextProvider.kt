package taxiapp.core.common.permissions

interface PermissionTextProvider {

    fun getDescription(isPermanentlyDeclined: Boolean): String
}