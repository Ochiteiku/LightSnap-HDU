package com.electroboys.lightsnap.domain.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.entity.Message

class ChatAdapter(private var messages: List<Message>, private var contactAvatarRes: Int) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == 0) {
            R.layout.msg_chat_received  // 接收的消息布局
        } else {
            R.layout.msg_chat_sent     // 发送的消息布局
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)

    }



    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.text
        holder.messageTime.text = message.time

        // 设置头像
        val avatarRes = if (message.isMe) {
            R.drawable.ic_avatar1 // 当前用户的头像
        } else {
            contactAvatarRes // 使用联系人的头像
        }
        holder.messageAvatar.setImageResource(avatarRes)
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isMe) 1 else 0  // 0=接收，1=发送
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val messageTime: TextView = itemView.findViewById(R.id.messageTime)
        val messageAvatar: ImageView = itemView.findViewById(R.id.messageAvatar) // 新增
    }
}
