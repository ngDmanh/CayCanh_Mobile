package com.example.caycanh_mobile.util

import android.content.Context
import android.net.Uri
import java.io.File

object ImageFileUtil {
    /** Copy nội dung từ Uri (content://) ra file tạm trong cacheDir để upload */
    fun uriToTempFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val extension = when (context.contentResolver.getType(uri)) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val tempFile = File(
                context.cacheDir,
                "upload_${System.currentTimeMillis()}_${(0..9999).random()}.$extension"
            )
            inputStream.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}