package com.lhht.aiassistant.ui.config

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lhht.aiassistant.R
import com.lhht.aiassistant.databinding.ItemConfigBinding
import com.lhht.aiassistant.model.DifyConfig
import com.lhht.aiassistant.model.XiaozhiConfig

/**
 * 配置适配器
 */
class ConfigAdapter(
    private val onConfigClick: (Any) -> Unit,
    private val onConfigEdit: (Any) -> Unit,
    private val onConfigDelete: (Any) -> Unit
) : ListAdapter<Any, ConfigAdapter.ConfigViewHolder>(ConfigDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigViewHolder {
        val binding = ItemConfigBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConfigViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ConfigViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ConfigViewHolder(
        private val binding: ItemConfigBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(config: Any) {
            when (config) {
                is DifyConfig -> {
                    binding.apply {
                        titleText.text = config.name
                        subtitleText.text = config.apiUrl
                        typeIcon.setImageResource(R.drawable.ic_chat_bubble_outline)
                        typeIcon.setColorFilter(ContextCompat.getColor(typeIcon.context, R.color.blue))
                    }
                }
                is XiaozhiConfig -> {
                    binding.apply {
                        titleText.text = config.name
                        subtitleText.text = config.websocketUrl
                        typeIcon.setImageResource(R.drawable.ic_mic)
                        typeIcon.setColorFilter(ContextCompat.getColor(typeIcon.context, R.color.gray_700))
                    }
                }
            }
            
            binding.root.setOnClickListener {
                onConfigClick(config)
            }
            
            // 编辑按钮点击事件
            binding.editButton.setOnClickListener {
                onConfigEdit(config)
            }
            
            // 删除按钮点击事件
            binding.deleteButton.setOnClickListener {
                onConfigDelete(config)
            }
        }
    }
    
    class ConfigDiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is DifyConfig && newItem is DifyConfig -> oldItem.id == newItem.id
                oldItem is XiaozhiConfig && newItem is XiaozhiConfig -> oldItem.id == newItem.id
                else -> false
            }
        }
        
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }
    }
}
