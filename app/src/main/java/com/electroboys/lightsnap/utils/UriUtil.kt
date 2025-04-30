package com.electroboys.lightsnap.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

object UriUtil {

    fun getPathFromUri(context: Context, uri: Uri): String? {
        return DocumentFile.fromTreeUri(context, uri)?.name
    }
}