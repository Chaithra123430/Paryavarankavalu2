package com.example.paryavaran_kavalu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    /**
     * Compresses image to be under 500KB as per project requirements.
     */
    suspend fun compressImage(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val compressedFile = File(context.cacheDir, "report_${System.currentTimeMillis()}.jpg")
            var quality = 90
            
            // Initial compression to check size
            var out = FileOutputStream(compressedFile)
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.close()

            // Iteratively reduce quality if still over 500KB
            while (compressedFile.length() > 500 * 1024 && quality > 10) {
                quality -= 10
                out = FileOutputStream(compressedFile)
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                out.close()
            }

            if (compressedFile.exists()) compressedFile else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
