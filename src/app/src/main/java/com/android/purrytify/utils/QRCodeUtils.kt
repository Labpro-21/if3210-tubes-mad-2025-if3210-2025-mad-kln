package com.android.purrytify.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.io.File


object QRCodeUtils {
    
    private fun generateQRCode(link: String, width: Int = 512, height: Int = 512): Bitmap {
        val bitMatrix: BitMatrix = try {
            MultiFormatWriter().encode(
                link,
                BarcodeFormat.QR_CODE,
                width,
                height,
                null
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Could not generate QR code", e)
        }
        
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        
        return bitmap
    }
    
    fun generateQRCodeWithInfo(
        link: String, 
        songTitle: String,
        songArtist: String,
        qrSize: Int = 512
    ): Bitmap {
        val qrBitmap = generateQRCode(link, qrSize, qrSize)
        
        val padding = 20
        val textHeight = 100
        val resultBitmap = Bitmap.createBitmap(
            qrSize + (padding * 2),
            qrSize + textHeight + (padding * 2),
            Bitmap.Config.ARGB_8888
        )
        
        val canvas = Canvas(resultBitmap)
        canvas.drawColor(Color.WHITE)
        
        canvas.drawBitmap(qrBitmap, padding.toFloat(), padding.toFloat(), null)
        
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val titleBounds = Rect()
        paint.getTextBounds(songTitle, 0, songTitle.length, titleBounds)

        canvas.drawText(
            songTitle,
            (resultBitmap.width - titleBounds.width()) / 2f,
            qrSize + padding + 35f,
            paint
        )

        paint.textSize = 24f
        paint.typeface = Typeface.DEFAULT

        val artistBounds = Rect()
        paint.getTextBounds(songArtist, 0, songArtist.length, artistBounds)

        canvas.drawText(
            songArtist,
            (resultBitmap.width - artistBounds.width()) / 2f,
            qrSize + padding + 70f,
            paint
        )

        return resultBitmap
    }
    

    fun saveQRCodeToStorage(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        return saveImageToMediaStore(context, bitmap, fileName)
    }
    
    private fun saveImageToMediaStore(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "Purrytify")
        }
        
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
            contentValues
        )
        
        return uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            it
        }
    }

    fun isValidPurrytifyQRCode(url: String): Boolean {
        return url.startsWith("https://purrytify-be.vercel.app/play?songId=") || 
               url.startsWith("purrytify://open?songId=")
    }
} 