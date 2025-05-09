package com.electroboys.lightsnap.data.entity

import android.os.Parcel
import android.os.Parcelable


data class Document(
    val title: String,
    val content: String,
    val time: String,
    val id: String
) : Parcelable {
    // 实现 describeContents（通常返回 0）
    override fun describeContents(): Int = 0

    // 序列化字段到 Parcel
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeString(content)
        dest.writeString(time)
    }

    // 反序列化 Creator
    companion object CREATOR : Parcelable.Creator<Document> {
        override fun createFromParcel(source: Parcel): Document =
            Document(source.readString()!!, source.readString()!!, source.readString()!!, "doc1")

        override fun newArray(size: Int): Array<Document?> = arrayOfNulls(size)
    }
}