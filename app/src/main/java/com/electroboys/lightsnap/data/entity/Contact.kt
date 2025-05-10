package com.electroboys.lightsnap.data.entity

import android.os.Parcel
import android.os.Parcelable

class Contact(
    val name: String,
    val avatarRes: Int,
    val lastMessage: String,
    val unread: Boolean, // 是否未读
    val marked: Boolean, // 是否标记
    val messages: List<Message> // 对话内容
) : Parcelable {

    // 实现 describeContents
    override fun describeContents(): Int = 0

    // 序列化字段到 Parcel
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeInt(avatarRes)
        dest.writeString(lastMessage)
        dest.writeByte(if (unread) 1 else 0)
        dest.writeByte(if (marked) 1 else 0)
        dest.writeTypedList(messages)
    }

    // 反序列化 Creator
    companion object CREATOR : Parcelable.Creator<Contact> {
        override fun createFromParcel(source: Parcel): Contact {
            return Contact(
                source.readString()!!,
                source.readInt(),
                source.readString()!!,
                source.readByte() != 0.toByte(),
                source.readByte() != 0.toByte(),
                source.createTypedArrayList(Message.CREATOR)!!
            )
        }

        override fun newArray(size: Int): Array<Contact?> = arrayOfNulls(size)
    }
}