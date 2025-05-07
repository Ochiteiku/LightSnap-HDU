package com.electroboys.lightsnap.data.entity

data class Message(
    val text: String,
    val isMe: Boolean, // 标记是否是我发送的消息
    val time: String
)