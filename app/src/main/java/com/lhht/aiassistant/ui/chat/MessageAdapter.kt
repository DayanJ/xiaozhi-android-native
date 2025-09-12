package com.lhht.aiassistant.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lhht.aiassistant.databinding.ItemMessageBinding
import com.lhht.aiassistant.model.Message
import com.lhht.aiassistant.model.MessageRole
import java.text.SimpleDateFormat
import java.util.*

/**
 * 消息适配器
 */
class MessageAdapter : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        fun bind(message: Message) {
            binding.apply {
                when (message.role) {
                    MessageRole.USER -> {
                        userMessageText.text = message.content
                        userTimeText.text = timeFormat.format(message.timestamp)
                    }
                    MessageRole.ASSISTANT -> {
                        assistantMessageText.text = message.content
                        assistantTimeText.text = timeFormat.format(message.timestamp)
                    }
                    MessageRole.SYSTEM -> {
                        systemMessageText.text = message.content
                    }
                }
                
                when (message.role) {
                    MessageRole.USER -> {
                        // 用户消息 - 右对齐
                        userMessageLayout.visibility = android.view.View.VISIBLE
                        assistantMessageLayout.visibility = android.view.View.GONE
                        systemMessageLayout.visibility = android.view.View.GONE
                        
                        userMessageText.text = message.content
                        userTimeText.text = timeFormat.format(message.timestamp)
                    }
                    MessageRole.ASSISTANT -> {
                        // 助手消息 - 左对齐
                        userMessageLayout.visibility = android.view.View.GONE
                        assistantMessageLayout.visibility = android.view.View.VISIBLE
                        systemMessageLayout.visibility = android.view.View.GONE
                        
                        assistantMessageText.text = message.content
                        assistantTimeText.text = timeFormat.format(message.timestamp)
                    }
                    MessageRole.SYSTEM -> {
                        // 系统消息 - 居中
                        userMessageLayout.visibility = android.view.View.GONE
                        assistantMessageLayout.visibility = android.view.View.GONE
                        systemMessageLayout.visibility = android.view.View.VISIBLE
                        
                        systemMessageText.text = message.content
                    }
                }
            }
        }
    }
    
    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}
