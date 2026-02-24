package com.casha.app.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    /**
     * Compresses an image URI into a smaller JPEG File suitable for API upload.
     * Prevents giant multi-megabyte photo uploads by scaling down the resolution
     * and applying an 80% JPEG quality compression.
     */
    fun compressImage(context: Context, uri: Uri, fileName: String = "optimized_upload.jpg"): File? {
        return try {
            val contentResolver = context.contentResolver
            
            // 1. Decode bounds to determine required scaling
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, options) 
            }
            
            // Limit max dimension to ~1200px (sufficient for receipt OCR)
            val maxDimension = 1200
            options.inSampleSize = calculateInSampleSize(options, maxDimension, maxDimension)
            options.inJustDecodeBounds = false
            
            // 2. Decode actual bitmap with sample size applied
            val bitmap = contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null
            
            // 3. Compress to JPEG File
            val tempFile = File(context.cacheDir, "${System.currentTimeMillis()}_$fileName")
            FileOutputStream(tempFile).use { out ->
                // Compress with 80% quality
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            
            bitmap.recycle() // Free native memory
            
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
