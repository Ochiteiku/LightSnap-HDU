package com.electroboys.lightsnap.ui.main.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.media3.common.text.TextAnnotation.Position
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import coil.load
import com.electroboys.lightsnap.R
import com.github.chrisbanes.photoview.PhotoView

class LibraryPictureAdapter(private val images: List<Uri>) : Adapter<LibraryPictureAdapter.ImageViewHolder>(){

    // 为item定义回调接口
    var onImageViewLongClickListener: ((position: Int) -> Unit)? = null
    var onImageViewClickListener: ((position: Int) -> Unit)? = null

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view){
        // 在此类中绑定数据，设置监听事件
        val imageView : ImageView = view.findViewById(R.id.imageView)

        fun bind(uri: Uri){

            // 使用Coil加载图片
            imageView.load(uri){
                crossfade(true)
                placeholder(R.drawable.ic_avatar1)  // 占位图

                // 强制使用软件层 Bitmap，避免硬件加速冲突
                allowHardware(false)
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
    }
}