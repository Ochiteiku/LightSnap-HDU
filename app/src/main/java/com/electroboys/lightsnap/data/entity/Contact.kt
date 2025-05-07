package com.electroboys.lightsnap.data.entity

class Contact(
    val name: String,
    val avatarRes: Int,
    val lastMessage: String,
    val unread: Boolean, // 是否未读
    val marked: Boolean, // 是否标记
    val messages: List<Message> // 对话内容
)