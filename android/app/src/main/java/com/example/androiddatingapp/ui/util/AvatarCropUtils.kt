package com.example.androiddatingapp.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

object AvatarCropUtils {

    fun loadOrientedBitmap(context: Context, uri: Uri, maxSize: Int = 2048): Bitmap? {
        val localFile = resolveLocalFile(context, uri) ?: return null
        val shouldDeleteTemp = uri.scheme != "file"
        return try {
            loadOrientedBitmapFromFile(localFile, maxSize)
        } finally {
            if (shouldDeleteTemp) {
                localFile.delete()
            }
        }
    }

    fun cropSquare(
        bitmap: Bitmap,
        containerWidth: Float,
        containerHeight: Float,
        cropSide: Float,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
    ): Bitmap {
        val imageLeft = containerWidth / 2f + offsetX - bitmap.width * scale / 2f
        val imageTop = containerHeight / 2f + offsetY - bitmap.height * scale / 2f
        val cropLeft = (containerWidth - cropSide) / 2f
        val cropTop = (containerHeight - cropSide) / 2f

        var srcLeft = ((cropLeft - imageLeft) / scale).toInt()
        var srcTop = ((cropTop - imageTop) / scale).toInt()
        var srcSize = (cropSide / scale).toInt().coerceAtLeast(1)

        srcLeft = srcLeft.coerceIn(0, (bitmap.width - 1).coerceAtLeast(0))
        srcTop = srcTop.coerceIn(0, (bitmap.height - 1).coerceAtLeast(0))
        srcSize = min(srcSize, min(bitmap.width - srcLeft, bitmap.height - srcTop)).coerceAtLeast(1)

        val cropped = Bitmap.createBitmap(bitmap, srcLeft, srcTop, srcSize, srcSize)
        return if (cropped.width == AVATAR_OUTPUT_SIZE) {
            cropped
        } else {
            Bitmap.createScaledBitmap(cropped, AVATAR_OUTPUT_SIZE, AVATAR_OUTPUT_SIZE, true).also {
                if (cropped != bitmap && cropped != it) cropped.recycle()
            }
        }
    }

    fun initialFitScale(bitmap: Bitmap, cropSide: Float): Float =
        min(cropSide / bitmap.width, cropSide / bitmap.height)

    fun clampPan(
        bitmap: Bitmap,
        scale: Float,
        cropSide: Float,
        containerWidth: Float,
        containerHeight: Float,
        offsetX: Float,
        offsetY: Float,
    ): Pair<Float, Float> {
        val imgW = bitmap.width * scale
        val imgH = bitmap.height * scale
        val cropLeft = (containerWidth - cropSide) / 2f
        val cropTop = (containerHeight - cropSide) / 2f
        val cropRight = cropLeft + cropSide
        val cropBottom = cropTop + cropSide

        var ox = offsetX
        var oy = offsetY
        val centerX = containerWidth / 2f + ox
        val centerY = containerHeight / 2f + oy
        val imgLeft = centerX - imgW / 2f
        val imgTop = centerY - imgH / 2f
        val imgRight = imgLeft + imgW
        val imgBottom = imgTop + imgH

        if (imgW > cropSide) {
            if (imgLeft > cropLeft) ox -= imgLeft - cropLeft
            if (imgRight < cropRight) ox += cropRight - imgRight
        } else {
            ox = 0f
        }
        if (imgH > cropSide) {
            if (imgTop > cropTop) oy -= imgTop - cropTop
            if (imgBottom < cropBottom) oy += cropBottom - imgBottom
        } else {
            oy = 0f
        }
        return ox to oy
    }

    fun saveToCache(context: Context, bitmap: Bitmap): Uri {
        val file = File(context.cacheDir, "avatar_crop_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        return Uri.fromFile(file)
    }

    private const val AVATAR_OUTPUT_SIZE = 512

    private fun resolveLocalFile(context: Context, uri: Uri): File? {
        if (uri.scheme == "file") {
            val path = uri.path ?: return null
            val file = File(path)
            return file.takeIf { it.exists() && it.canRead() }
        }
        return copyUriToCache(context, uri)
    }

    private fun copyUriToCache(context: Context, uri: Uri): File? = runCatching {
        val extension = when (context.contentResolver.getType(uri)) {
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/heic", "image/heif" -> ".heic"
            else -> ".jpg"
        }
        val target = File(context.cacheDir, "avatar_pick_${System.currentTimeMillis()}$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        target.takeIf { it.length() > 0 }
    }.getOrNull()

    private fun loadOrientedBitmapFromFile(file: File, maxSize: Int): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            runCatching {
                val source = ImageDecoder.createSource(file)
                return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            }.getOrNull()?.let { decoded ->
                return applyExifOrientation(file, decoded)
            }
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, maxSize)
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null
        return applyExifOrientation(file, decoded)
    }

    private fun calculateSampleSize(width: Int, height: Int, maxSize: Int): Int {
        var sample = 1
        while (width / sample > maxSize || height / sample > maxSize) {
            sample *= 2
        }
        return sample
    }

    private fun applyExifOrientation(file: File, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            ExifInterface(file.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }.getOrElse { ExifInterface.ORIENTATION_NORMAL }

        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }

        val matrix = Matrix().apply { postRotate(rotation) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
            if (it != bitmap) bitmap.recycle()
        }
    }
}
