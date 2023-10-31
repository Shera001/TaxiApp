package taxiapp.core.common.util

import android.content.res.Resources
import javax.inject.Inject

class AndroidResourceProvider @Inject constructor(
    private val resources: Resources
) : ResourceProvider {

    override fun getString(stringRes: Int): String {
        return resources.getString(stringRes)
    }
}