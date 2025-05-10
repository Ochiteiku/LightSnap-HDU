package com.electroboys.lightsnap.data.entity

import android.os.Parcel
import android.os.Parcelable

data class Message(
    val text: String,
    val isMe: Boolean, // 标记是否是我发送的消息
    val time: String
) : Parcelable {

    // 实现 describeContents（通常返回 0）
    override fun describeContents(): Int = 0

    // 序列化字段到 Parcel
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(text)
        dest.writeByte(if (isMe) 1 else 0)
        dest.writeString(time)
    }

    // 反序列化 Creator
    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(
                parcel.readString()!!,
                parcel.readByte() != 0.toByte(),
                parcel.readString()!!
            )
        }

        override fun newArray(size: Int): Array<Message?> = arrayOfNulls(size)
    }
}