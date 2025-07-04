package com.electroboys.lightsnap.ui.main.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import coil.load
import com.electroboys.lightsnap.R

class LibraryPictureAdapter(private var images: MutableList<Uri>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object {
        var isScreenshotMode = false
        const val TYPE_ITEM = 1 // 正常图片展示布局
    }
    // 为item定义回调接口
    var onImageViewLongClickListener: ((position: Int) -> Unit)? = null
    var onImageViewClickListener: ((position: Int) -> Unit)? = null

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view){
        // 在此类中绑定数据，设置监听事件
        val imageView : ImageView = view.findViewById(R.id.imageView)

        fun bind(uri: Uri){
            if (isScreenshotMode) {
                imageView.load(uri) {
                    crossfade(false)
                    allowHardware(false)
                    placeholder(R.drawable.ic_avatar1)
                }
            } else {
                imageView.load(uri) {
                    crossfade(true)
                    allowHardware(true)
                    placeholder(R.drawable.ic_avatar1)
                }
            }
            imageView.setOnLongClickListener{
                onImageViewLongClickListener?.invoke(adapterPosition)
                true
            }
            imageView.setOnClickListener {
                onImageViewClickListener?.invoke(adapterPosition)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.library_recyclerview_component, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImageViewHolder) {
            holder.bind(images[position])
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    fun deleteData(){
        images.clear()
        notifyDataSetChanged()
    }

    fun updateData(newData: List<Uri>) {
        images.clear()
        images.addAll(newData)
        notifyDataSetChanged() // 通知 RecyclerView 数据已变更
    }
}