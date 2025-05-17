package com.electroboys.lightsnap.domain.screenshot.scrollshot

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.graphics.createBitmap

object ScrollShotHelper {
//    private const val MAX_BITMAP_HEIGHT_RATIO = 2f

    fun captureAllScrollables(activity: Activity): Bitmap? {
        val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val scrollableViews = findAllScrollableViews(rootView)

        val bitmaps = scrollableViews.mapNotNull { view ->
            when (view) {
                is ScrollView -> captureScrollView(view)
                is RecyclerView -> captureRecyclerView(view)
                is WebView -> captureWebView(view)
                else -> null
            }
        }
        if (bitmaps.isEmpty()) return null

        return mergeBitmapsVertically(bitmaps)
    }

    fun findAllScrollableViews(view: View): List<View> {
        val result = mutableListOf<View>()
        if (view.canScrollVertically(1) || view.canScrollVertically(-1)) {
            result.add(view)
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                result += findAllScrollableViews(view.getChildAt(i))
            }
        }
        return result
    }

    fun captureScrollView(scrollView: ScrollView): Bitmap? {
        val child = scrollView.getChildAt(0) ?: return null
        val width = child.width
        val height = child.height
        if (width == 0 || height == 0) return null

        // 获取屏幕尺寸并计算最大允许高度
//        val displayMetrics = scrollView.context.resources.displayMetrics
//        val maxHeight = (displayMetrics.heightPixels * MAX_BITMAP_HEIGHT_RATIO).toInt()
//
//        // 缩放比例
//        val scale = if (height > maxHeight) maxHeight.toFloat() / height else 1f
//        val scaledWidth = (width * scale).toInt()
//        val scaledHeight = (height * scale).toInt()

        val bitmap = createBitmap(width, height)
//        val bitmap = createBitmap(scaledWidth, scaledHeight)
        val canvas = Canvas(bitmap)
        child.draw(canvas)
        return bitmap
    }

    fun captureRecyclerView(recyclerView: RecyclerView): Bitmap? {
        val adapter = recyclerView.adapter ?: return null
        val paint = Paint()
        val bitmaps = mutableListOf<Bitmap>()
        var totalHeight = 0
        val context = recyclerView.context
        val displayMetrics = context.resources.displayMetrics
        val maxHeightPerItem = (displayMetrics.heightPixels * 0.5f).toInt()

        for (i in 0 until adapter.itemCount) {
            val holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i))
            adapter.onBindViewHolder(holder, i)

            val itemView = holder.itemView
            itemView.measure(
                View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            itemView.layout(0, 0, itemView.measuredWidth, itemView.measuredHeight)

            val bmpWidth = itemView.width
            val bmpHeight = itemView.height
            if (bmpWidth == 0 || bmpHeight == 0) continue

            // 缩放比例
            val scale = if (bmpHeight > maxHeightPerItem) maxHeightPerItem.toFloat() / bmpHeight else 1f
            val scaledWidth = (bmpWidth * scale).toInt()
            val scaledHeight = (bmpHeight * scale).toInt()

//            val bmp = createBitmap(itemView.width, itemView.height)
            val bmp = createBitmap(scaledWidth, scaledHeight)
            val canvas = Canvas(bmp)
            itemView.draw(canvas)
            bitmaps.add(bmp)
            totalHeight += itemView.height
        }

        val finalBitmap = createBitmap(recyclerView.width, totalHeight)
        val canvas = Canvas(finalBitmap)
        var top = 0
        bitmaps.forEach { bmp ->
            canvas.drawBitmap(bmp, 0f, top.toFloat(), paint)
            top += bmp.height
        }

        return finalBitmap
    }

    fun captureWebView(webView: WebView): Bitmap? {
        val width = webView.width
        val height = (webView.contentHeight * webView.scale).toInt()
        if (width == 0 || height == 0) return null

//        val context = webView.context
//        val displayMetrics = context.resources.displayMetrics
//        val maxHeight = (displayMetrics.heightPixels * MAX_BITMAP_HEIGHT_RATIO).toInt()
//
//        val scale = if (height > maxHeight) maxHeight.toFloat() / height else 1f
//        val scaledWidth = (width * scale).toInt()
//        val scaledHeight = (height * scale).toInt()

        val bitmap = createBitmap(width, height)
//        val bitmap = createBitmap(scaledWidth, scaledHeight)
        val canvas = Canvas(bitmap)
        webView.draw(canvas)
        return bitmap
    }

    private fun mergeBitmapsVertically(bitmaps: List<Bitmap>): Bitmap {
        val width = bitmaps.maxOfOrNull { it.width } ?: 1
        val height = bitmaps.sumOf { it.height }

        val result = createBitmap(width, height)
        val canvas = Canvas(result)
        var top = 0
        bitmaps.forEach {
            canvas.drawBitmap(it, 0f, top.toFloat(), null)
            top += it.height
        }
        return result
    }
}
