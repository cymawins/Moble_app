package com.kiuda.app.presentation.growth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kiuda.app.databinding.ItemGrowthRecordBinding
import com.kiuda.app.domain.model.GrowthRecordItem

class GrowthRecordAdapter(
    private val onItemClick: ((GrowthRecordItem) -> Unit)? = null
) : ListAdapter<GrowthRecordItem, GrowthRecordAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGrowthRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemGrowthRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(getItem(position))
                }
            }
        }

        fun bind(item: GrowthRecordItem) {
            binding.tvRecordIcon.text = item.recordType.icon
            binding.tvRecordCategory.text = item.recordType.label
            binding.tvPlantNameTag.text = buildString {
                append(item.plantName)
                if (!item.plantType.isNullOrBlank()) {
                    append(" · ")
                    append(item.plantType)
                }
            }
            binding.tvDisplayDate.text = item.displayDate
            binding.tvRecordTitle.text = item.title
            binding.tvRecordContent.text = item.content ?: ""
            binding.tvStatusBadge.text = if (item.isCompleted) "✓ 관리 완료" else "진행 중"
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<GrowthRecordItem>() {
            override fun areItemsTheSame(oldItem: GrowthRecordItem, newItem: GrowthRecordItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: GrowthRecordItem, newItem: GrowthRecordItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
