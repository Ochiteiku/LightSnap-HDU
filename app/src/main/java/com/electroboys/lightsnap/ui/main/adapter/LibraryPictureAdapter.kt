package com.electroboys.lightsnap.ui.main.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import coil.load
import com.electroboys.lightsnap.R

class LibraryPictureAdapter(private val images: List<Uri>) : Adapter<LibraryPictureAdapter.ImageViewHolder>(){

    // 为item定义回调接口
    var onItemLongClickListener: ((position: Int) -> Unit)? = null

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view){
        // 在此类中绑定数据，设置监听事件
        val imageView : ImageView = view.findViewById(R.id.imageView)

        fun bind(uri: Uri){
            // 使用Coil加载图片
            imageView.load(uri){
                crossfade(true)
                placeholder(R.drawable.ic_avatar1)  // 占位图
            }
            imageView.setOnLongClickListener{
                onItemLongClickListener?.invoke(adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        // 组件布局
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_library_component, parent, false)
        return ImageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])

// 每次RecyclerView 滚动时会频繁调用该方法
// 不推荐在此处绑定数据以及设置监听器
//        val imageUri = images[position]
//
//        // 使用Coil加载图片
//        holder.imageView.load(imageUri){
//            crossfade(true)
//            placeholder(R.drawable.ic_avatar1)  // 占位图
//        }
    }
}