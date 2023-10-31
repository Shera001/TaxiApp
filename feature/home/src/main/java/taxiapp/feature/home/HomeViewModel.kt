package taxiapp.feature.home

import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import taxiapp.core.common.network_status.ConnectivityObserver
import taxiapp.core.common.util.ResourceProvider
import taxiapp.core.domain.use_case.GetLocationUseCase
import taxiapp.core.model.Location
import taxiapp.core.model.Tariff
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getLocation: GetLocationUseCase,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    val locations: SharedFlow<Location> = flow {
        emitAll(getLocation())
    }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    val networkStatus: StateFlow<Boolean> = flow {
        emitAll(connectivityObserver.observe())
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _tempTariffs = MutableSharedFlow<List<Tariff>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val tempTariffs: SharedFlow<List<Tariff>> = _tempTariffs.asSharedFlow()

    init {
        viewModelScope.launch {
            _tempTariffs.emit(getTempTariffs())
        }
    }

    fun changeTempTariffs(id: Int) {
        viewModelScope.launch {
            val tempTariffList = mutableListOf<Tariff>()
            getTempTariffs().forEach { tariff ->
                if (tariff.id == id) {
                    tempTariffList.add(
                        tariff.copy(
                            backgroundColorId = taxiapp.core.ui.R.color.selected_tariff_background
                        )
                    )
                } else {
                    tempTariffList.add(
                        tariff.copy(
                            backgroundColorId = taxiapp.core.ui.R.color.unselected_tariff_background
                        )
                    )
                }
            }
            _tempTariffs.emit(tempTariffList)
        }
    }


    companion object {
        fun getTempTariffs(): List<Tariff> {
            val list = mutableListOf<Tariff>()

            list.add(
                Tariff(
                    id = 0,
                    tariffName = "Стандарт",
                    price = "4 000 сум",
                    icon = R.drawable.car2,
                    isThunder = true,
                    backgroundColorId = taxiapp.core.ui.R.color.selected_tariff_background
                )
            )

            list.add(
                Tariff(
                    id = 1,
                    tariffName = "Комфорт",
                    price = "6 000 сум",
                    icon = R.drawable.car1,
                    backgroundColorId = taxiapp.core.ui.R.color.unselected_tariff_background
                )
            )

            list.add(
                Tariff(
                    id = 2,
                    tariffName = "Доставка",
                    price = "7 000 сум",
                    icon = R.drawable.car3,
                    backgroundColorId = taxiapp.core.ui.R.color.unselected_tariff_background
                )
            )

            list.add(
                Tariff(
                    id = 3,
                    tariffName = "Техпомощъ",
                    price = "10 000 сум",
                    icon = R.drawable.car1,
                    backgroundColorId = taxiapp.core.ui.R.color.unselected_tariff_background
                )
            )

            return list
        }
    }
}