package com.example.androiddatingapp.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID

object MediaUpload {

    fun createVideoPart(context: Context, uri: Uri): MultipartBody.Part {
        val file = copyUriToCache(context, uri, "upload_video", defaultExtension = ".mp4")
        val mimeType = context.contentResolver.getType(uri) ?: "video/mp4"
        val body = file.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", file.name, body)
    }

    fun createImagePart(context: Context, uri: Uri): MultipartBody.Part {
        val file = copyUriToCache(context, uri, "upload_avatar", defaultExtension = ".jpg")
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val body = file.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", file.name, body)
    }

    private fun copyUriToCache(
        context: Context,
        uri: Uri,
        prefix: String,
        defaultExtension: String,
    ): File {
        val extension = resolveExtension(context, uri, defaultExtension)
        val fileName = resolveDisplayName(context, uri) ?: "${UUID.randomUUID()}$extension"
        val safeName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val target = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}_$safeName")
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IllegalArgumentException("Не удалось прочитать файл")
        return target
    }

    private fun resolveExtension(context: Context, uri: Uri, defaultExtension: String): String {
        val mime = context.contentResolver.getType(uri)
        return when {
            mime?.contains("png", ignoreCase = true) == true -> ".png"
            mime?.contains("webp", ignoreCase = true) == true -> ".webp"
            mime?.contains("quicktime", ignoreCase = true) == true -> ".mov"
            mime?.contains("mp4", ignoreCase = true) == true -> ".mp4"
            else -> defaultExtension
        }
    }

    private fun resolveDisplayName(context: Context, uri: Uri): String? {
        if (uri.scheme != "content") return null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return null
    }
}
