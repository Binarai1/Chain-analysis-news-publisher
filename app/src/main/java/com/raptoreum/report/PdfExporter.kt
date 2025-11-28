package com.raptoreum.report

import android.content.ContentValues
import android.content.Context
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

class PdfExporter {

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
        var y = 32f
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
}
