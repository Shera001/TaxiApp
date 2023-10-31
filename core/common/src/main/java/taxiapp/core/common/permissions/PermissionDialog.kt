package taxiapp.core.common.permissions

import android.content.Context
import androidx.appcompat.app.AlertDialog

object PermissionDialog {

    fun showDialog(
        context: Context,
        permissionTextProvider: PermissionTextProvider,
        isPermanentlyDeclined: Boolean,
        onOkClick: () -> Unit,
        onGoToAppSettingsClick: () -> Unit
    ) {
        val buttonTitle = if (isPermanentlyDeclined) {
            "Предоставить разрешение"
        } else {
            "Ok"
        }
        val builder = AlertDialog.Builder(context)
            .setTitle("Требуется разрешение")
            .setMessage(permissionTextProvider.getDescription(isPermanentlyDeclined))
            .setCancelable(false)
        builder.setPositiveButton(
            buttonTitle
        ) { _, _ ->
            if (isPermanentlyDeclined) {
                onGoToAppSettingsClick()
            } else {
                onOkClick()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
}