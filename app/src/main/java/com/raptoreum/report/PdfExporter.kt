package com.raptoreum.report

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.net.URL

class PdfExporter {

    private val logoUrls = listOf(
        "https://raptoreum.com/wp-content/uploads/2021/10/cropped-Raptoreum-Logo-2048x2048-1.png",
        "https://raw.githubusercontent.com/Raptor3um/raptoreum/master/doc/assets/raptoreum-logo.png",
        "https://cryptologos.cc/logos/raptoreum-rtm-logo.png"
    )

    fun export(context: Context, reportText: String): String {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            typeface = Typeface.MONOSPACE
        }

        val lines = reportText.split("\n")
        var x = 24f
        var y = drawBranding(canvas, paint)
        val lineSpacing = 16f

        lines.forEach { line ->
            if (y + lineSpacing > pageInfo.pageHeight) {
                // Prevent overflow; a second page can be added later if needed.
                return@forEach
            }
            canvas.drawText(line, x, y, paint)
            y += lineSpacing
        }
        pdfDocument.finishPage(page)

        val fileName = "Raptoreum_Report_${timestamp()}.pdf"
        val savedPath = saveDocument(context, pdfDocument, fileName)
        pdfDocument.close()
        return savedPath
    }

    private fun timestamp(): String =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))

    private fun saveDocument(context: Context, document: PdfDocument, fileName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + File.separator + "Raptoreum")
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                ?: error("Unable to create MediaStore entry")
            resolver.openOutputStream(uri)?.use { outputStream ->
                document.writeTo(outputStream)
            }
            uri.toString()
        } else {
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(directory, fileName)
            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }
            file.absolutePath
        }
    }

    private fun drawBranding(
        canvas: PdfDocument.Page,
        paint: Paint
    ): Float {
        val bitmaps = logoUrls.mapNotNull { fetchBitmap(it) }
        var yPosition = 32f
        if (bitmaps.isNotEmpty()) {
            var xPosition = 24f
            val maxHeight = bitmaps.maxOf { it.height.coerceAtMost(180) }
            bitmaps.forEach { bitmap ->
                val scaled = scaleBitmap(bitmap, maxHeight)
                canvas.canvas.drawBitmap(scaled, xPosition, yPosition, paint)
                xPosition += scaled.width + 12
            }
            yPosition += maxHeight + 18
        }
        return yPosition
    }

    private fun fetchBitmap(url: String): Bitmap? = runCatching {
        val connection = URL(url).openConnection()
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        connection.getInputStream().use { input ->
            BitmapFactory.decodeStream(input)
        }
    }.getOrNull()

    private fun scaleBitmap(bitmap: Bitmap, targetHeight: Int): Bitmap {
        val ratio = targetHeight.toFloat() / bitmap.height
        val targetWidth = (bitmap.width * ratio).toInt().coerceAtMost(400)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
