package io.github.jezreal.smartburglaralarm.ui.sensorstream


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.jezreal.smartburglaralarm.databinding.SensorDataCardBinding
import io.github.jezreal.smartburglaralarm.domain.SensorData

class SensorDataAdapter :
    ListAdapter<SensorData, SensorDataAdapter.ViewHolder>(SensorDataDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder private constructor(
        private val binding: SensorDataCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SensorData) {
            binding.sensorValue.text = item.sensorData
            binding.timestampText.text = item.timeStamp
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SensorDataCardBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class SensorDataDiffCallback : DiffUtil.ItemCallback<SensorData>() {
    override fun areItemsTheSame(oldItem: SensorData, newItem: SensorData): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SensorData, newItem: SensorData): Boolean {
        return oldItem == newItem
    }

}