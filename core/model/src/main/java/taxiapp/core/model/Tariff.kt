package taxiapp.core.model

data class Tariff(
    val id: Int,
    val tariffName: String,
    val price: String,
    val icon: Int,
    val isThunder: Boolean = false,
    val backgroundColorId: Int
)
