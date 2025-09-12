package com.lhht.aiassistant.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lhht.aiassistant.R
import com.lhht.aiassistant.databinding.ItemConversationBinding
import com.lhht.aiassistant.model.Conversation
import com.lhht.aiassistant.model.ConversationType
import java.text.SimpleDateFormat
import java.util.*

/**
 * 对话适配器
 */
class ConversationAdapter(
    private val onConversationClick: (Conversation) -> Unit,
    private val onConversationDelete: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder>(ConversationDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConversationViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ConversationViewHolder(
        private val binding: ItemConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
        
        fun bind(conversation: Conversation) {
            binding.apply {
                titleText.text = conversation.title
                lastMessageText.text = conversation.lastMessage
                timeText.text = dateFormat.format(conversation.lastMessageTime)
                
                // 设置对话类型图标
                when (conversation.type) {
                    ConversationType.DIFY -> {
                        typeIcon.setImageResource(R.drawable.ic_chat_bubble_outline)
                        typeIcon.setColorFilter(ContextCompat.getColor(typeIcon.context, R.color.blue))
                    }
                    ConversationType.XIAOZHI -> {
                        typeIcon.setImageResource(R.drawable.ic_mic)
                        typeIcon.setColorFilter(ContextCompat.getColor(typeIcon.context, R.color.gray_700))
                    }
                }
                
                // 设置置顶状态
                pinIcon.visibility = if (conversation.isPinned) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
                
                // 设置未读消息数量
                if (conversation.unreadCount > 0) {
                    unreadBadge.visibility = android.view.View.VISIBLE
                    unreadBadge.text = conversation.unreadCount.toString()
                } else {
                    unreadBadge.visibility = android.view.View.GONE
                }
                
                // 设置点击事件
                root.setOnClickListener {
                    onConversationClick(conversation)
                }
                
                // 设置长按删除事件
                root.setOnLongClickListener {
                    showDeleteDialog(conversation)
                    true
                }
            }
        }
        
        private fun showDeleteDialog(conversation: Conversation) {
            val context = binding.root.context
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("删除对话")
                .setMessage("确定要删除对话 \"${conversation.title}\" 吗？此操作不可撤销。")
                .setPositiveButton("删除") { _, _ ->
                    onConversationDelete(conversation)
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }
    
    class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem == newItem
        }
    }
}
