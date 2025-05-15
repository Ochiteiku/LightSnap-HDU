package com.electroboys.lightsnap.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

object UriUtil {

    fun getPathFromUri(context: Context, uri: Uri): String? {
        return when (uri.scheme) {
            "content" -> {
                // SAF 或媒体库中的 uri
                try {
                    val docFile = DocumentFile.fromTreeUri(context, uri)
                    docFile?.name
                } catch (e: Exception) {
                    null
                }
            }
            "file" -> uri.path
            else -> uri.toString()
        }
    }
}