package com.electroboys.lightsnap.data.screenshot

import android.graphics.Bitmap
import java.util.UUID

object BitmapCache {
    private val cache = mutableMapOf<String, Bitmap>()

    //将Bitmap存入缓存并返回key
    fun cacheBitmap(bitmap: Bitmap): String {
        val key = UUID.randomUUID().toString()
        cache[key] = bitmap
        return key
    }

    //根据key获取Bitmap
    fun getBitmap(key: String): Bitmap? {
        return cache[key]
    }

    //清除指定key的Bitmap
    fun removeBitmap(key: String) {
        cache.remove(key)
    }

    //清空所有缓存
    fun clear() {
        cache.clear()
    }

    //清空所有缓存，除了指定的key
    fun clearExcept(keepKey: String?) {
        keepKey?.let { key ->
            val toKeep = cache[key]
            cache.clear()
            if (toKeep != null) {
                cache[key] = toKeep
            }
        }
    }
}