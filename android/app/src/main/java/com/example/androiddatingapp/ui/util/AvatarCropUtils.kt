package com.example.androiddatingapp.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

object AvatarCropUtils {

    fun loadOrientedBitmap(context: Context, uri: Uri, maxSize: Int = 2048): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        } ?: return null

        val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, maxSize)
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        return applyExifOrientation(context, uri, decoded)
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

    fun initialCoverScale(bitmap: Bitmap, cropSide: Float): Float =
        max(cropSide / bitmap.width, cropSide / bitmap.height)

    fun saveToCache(context: Context, bitmap: Bitmap): Uri {
        val file = File(context.cacheDir, "avatar_crop_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        return Uri.fromFile(file)
    }

    private const val AVATAR_OUTPUT_SIZE = 512

    private fun calculateSampleSize(width: Int, height: Int, maxSize: Int): Int {
        var sample = 1
        while (width / sample > maxSize || height / sample > maxSize) {
            sample *= 2
        }
        return sample
    }

    private fun applyExifOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

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
