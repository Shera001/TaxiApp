package taxiapp.feature.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import taxiapp.core.model.Tariff

class TariffAdapter(
    private val onTariffClickedListener: OnTariffClickedListener
) : ListAdapter<Tariff, TariffViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TariffViewHolder {
        return TariffViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TariffViewHolder, position: Int) {
        holder.bind(getItem(position), onTariffClickedListener)
    }

    companion object DiffCallback : ItemCallback<Tariff>() {
        override fun areItemsTheSame(oldItem: Tariff, newItem: Tariff): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tariff, newItem: Tariff): Boolean {
            return (oldItem.tariffName == newItem.tariffName) &&
                    (oldItem.price == newItem.price) &&
                    (oldItem.backgroundColorId == newItem.backgroundColorId)
        }
    }
}