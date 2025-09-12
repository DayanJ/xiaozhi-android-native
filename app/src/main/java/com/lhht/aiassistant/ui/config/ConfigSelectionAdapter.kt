package com.lhht.aiassistant.ui.config

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lhht.aiassistant.R
import com.lhht.aiassistant.databinding.ItemConfigSelectionBinding
import com.lhht.aiassistant.model.XiaozhiConfig

/**
 * 配置选择适配器
 */
class ConfigSelectionAdapter(
    private val onConfigSelect: (XiaozhiConfig) -> Unit
) : ListAdapter<XiaozhiConfig, ConfigSelectionAdapter.ConfigSelectionViewHolder>(ConfigSelectionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigSelectionViewHolder {
        val binding = ItemConfigSelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConfigSelectionViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ConfigSelectionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ConfigSelectionViewHolder(
        private val binding: ItemConfigSelectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(config: XiaozhiConfig) {
            binding.apply {
                titleText.text = config.name
                subtitleText.text = config.websocketUrl
                macAddressText.text = "MAC: ${config.macAddress}"
                typeIcon.setImageResource(R.drawable.ic_mic)
                typeIcon.setColorFilter(ContextCompat.getColor(typeIcon.context, R.color.gray_700))
            }
            
            binding.root.setOnClickListener {
                onConfigSelect(config)
            }
        }
    }
    
    class ConfigSelectionDiffCallback : DiffUtil.ItemCallback<XiaozhiConfig>() {
        override fun areItemsTheSame(oldItem: XiaozhiConfig, newItem: XiaozhiConfig): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: XiaozhiConfig, newItem: XiaozhiConfig): Boolean {
            return oldItem == newItem
        }
    }
}
