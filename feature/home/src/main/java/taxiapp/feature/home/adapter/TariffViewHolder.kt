package taxiapp.feature.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import taxiapp.core.model.Tariff
import taxiapp.feature.home.databinding.ItemTariffBinding

class TariffViewHolder(
    private val itemBinding: ItemTariffBinding
) : ViewHolder(itemBinding.root) {

    fun bind(item: Tariff, onTariffClickedListener: OnTariffClickedListener) {
        with(itemBinding) {
            tariffItemCard.setCardBackgroundColor(
                ContextCompat.getColor(root.context, item.backgroundColorId)
            )
            thunderIv.isVisible = item.isThunder
            tariffTv.text = item.tariffName
            priceTv.text = item.price
            carIv.setImageResource(item.icon)
            root.setOnClickListener {
                onTariffClickedListener.onClick(item.id)
            }
        }
    }

    companion object {
        fun create(viewGroup: ViewGroup): TariffViewHolder {
            return TariffViewHolder(
                ItemTariffBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
            )
        }
    }
}