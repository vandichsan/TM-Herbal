package com.akacaraka.herbal.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.akacaraka.herbal.R
import com.akacaraka.herbal.data.HerbalCategory
import com.akacaraka.herbal.data.HerbalItem
import com.akacaraka.herbal.data.Visitor
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportPrinter {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val FOOTER_Y = 820f

    fun printHerbalReport(context: Context, items: List<HerbalItem>, category: HerbalCategory) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Laporan_${category.label}_${System.currentTimeMillis()}"

        val itemsPerPageFirst = 15
        val itemsPerPageOther = 20
        val signatureSpaceRows = 6
        
        val totalItemsWithSpace = items.size + signatureSpaceRows
        val pageCount = if (totalItemsWithSpace <= itemsPerPageFirst) 1 
                        else 1 + Math.ceil((totalItemsWithSpace - itemsPerPageFirst).toDouble() / itemsPerPageOther).toInt()

        printManager.print(jobName, object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(pageCount)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                val pdfDocument = PdfDocument()
                val logo = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
                val scaledLogo = logo?.let { Bitmap.createScaledBitmap(it, 60, 60, true) }
                val paint = Paint().apply {
                    typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                    textSize = 14f
                }
                val signatureDate = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("in", "ID")).format(Date())

                var itemIndex = 0
                for (p in 0 until pageCount) {
                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, p + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas

                    var y = 60f

                    if (p == 0) {
                        scaledLogo?.let { canvas.drawBitmap(it, MARGIN, 40f, paint) }
                        paint.textSize = 20f
                        paint.isFakeBoldText = true
                        canvas.drawText("TM-HERBAL", MARGIN + 70f, 65f, paint)
                        paint.textSize = 10f
                        paint.isFakeBoldText = false
                        canvas.drawText("Aplikasi Katalog Herbal Akacaraka", MARGIN + 70f, 85f, paint)
                        paint.strokeWidth = 2f
                        canvas.drawLine(MARGIN, 110f, PAGE_WIDTH - MARGIN, 110f, paint)
                        
                        y = 140f
                        paint.textSize = 18f
                        paint.isFakeBoldText = true
                        canvas.drawText("LAPORAN ${category.label.uppercase()}", MARGIN, y, paint)
                        y += 25f
                        paint.textSize = 11f
                        paint.isFakeBoldText = false
                        canvas.drawText("Total data dalam laporan ini: ${items.size} item", MARGIN, y, paint)
                        y += 30f
                    }

                    paint.textSize = 14f
                    paint.isFakeBoldText = true
                    canvas.drawText("No", MARGIN + 5f, y, paint)
                    canvas.drawText("Nama Lokal", MARGIN + 45f, y, paint)
                    canvas.drawText("Nama Ilmiah", MARGIN + 245f, y, paint)
                    
                    val headerY = y - 20f
                    y += 10f
                    canvas.drawLine(MARGIN, headerY, PAGE_WIDTH - MARGIN, headerY, paint)
                    canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)

                    canvas.drawLine(MARGIN, headerY, MARGIN, y, paint)
                    canvas.drawLine(MARGIN + 40f, headerY, MARGIN + 40f, y, paint)
                    canvas.drawLine(MARGIN + 240f, headerY, MARGIN + 240f, y, paint)
                    canvas.drawLine(PAGE_WIDTH - MARGIN, headerY, PAGE_WIDTH - MARGIN, y, paint)

                    paint.isFakeBoldText = false
                    val limit = if (p == 0) itemsPerPageFirst else itemsPerPageOther
                    var count = 0
                    while (count < limit && itemIndex < items.size) {
                        val item = items[itemIndex]
                        y += 25f
                        canvas.drawText((itemIndex + 1).toString(), MARGIN + 5f, y, paint)
                        canvas.drawText(item.localName, MARGIN + 45f, y, paint)
                        canvas.drawText(item.scientificName, MARGIN + 245f, y, paint)
                        
                        val rowBottomY = y + 5f
                        canvas.drawLine(MARGIN, rowBottomY, PAGE_WIDTH - MARGIN, rowBottomY, paint)
                        canvas.drawLine(MARGIN, rowBottomY - 30f, MARGIN, rowBottomY, paint)
                        canvas.drawLine(MARGIN + 40f, rowBottomY - 30f, MARGIN + 40f, rowBottomY, paint)
                        canvas.drawLine(MARGIN + 240f, rowBottomY - 30f, MARGIN + 240f, rowBottomY, paint)
                        canvas.drawLine(PAGE_WIDTH - MARGIN, rowBottomY - 30f, PAGE_WIDTH - MARGIN, rowBottomY, paint)
                        
                        y = rowBottomY
                        itemIndex++
                        count++
                    }

                    paint.textSize = 10f
                    canvas.drawText("Halaman ${p + 1} dari $pageCount", MARGIN, FOOTER_Y, paint)

                    if (itemIndex >= items.size) {
                        y += 50f
                        if (y < 400f && p > 0) y = 400f
                        paint.textSize = 14f
                        paint.isFakeBoldText = false
                        canvas.drawText("Depok, $signatureDate", PAGE_WIDTH - MARGIN - 180f, y, paint)
                        y += 25f
                        canvas.drawText("Mengetahui,", PAGE_WIDTH - MARGIN - 180f, y, paint)
                        y += 70f
                        paint.isFakeBoldText = true
                        canvas.drawText("Yuyun", PAGE_WIDTH - MARGIN - 180f, y, paint)
                    }

                    pdfDocument.finishPage(page)
                }

                try {
                    pdfDocument.writeTo(FileOutputStream(destination.fileDescriptor))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.toString())
                    return
                } finally {
                    pdfDocument.close()
                }
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }
        }, null)
    }

    fun printVisitorReport(context: Context, visitors: List<Visitor>) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Laporan_Pengunjung_${System.currentTimeMillis()}"
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val sortedVisitors = visitors.sortedByDescending { it.timestamp }
        
        val itemsPerPageFirst = 15
        val itemsPerPageOther = 20
        val signatureSpaceRows = 6
        
        val totalItemsWithSpace = sortedVisitors.size + signatureSpaceRows
        val pageCount = if (totalItemsWithSpace <= itemsPerPageFirst) 1 
                        else 1 + Math.ceil((totalItemsWithSpace - itemsPerPageFirst).toDouble() / itemsPerPageOther).toInt()

        printManager.print(jobName, object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(pageCount)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                val pdfDocument = PdfDocument()
                val logo = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
                val scaledLogo = logo?.let { Bitmap.createScaledBitmap(it, 60, 60, true) }
                val paint = Paint().apply {
                    typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                    textSize = 14f
                }
                val signatureDate = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("in", "ID")).format(Date())

                var itemIndex = 0
                for (p in 0 until pageCount) {
                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, p + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas

                    var y = 60f

                    if (p == 0) {
                        scaledLogo?.let { canvas.drawBitmap(it, MARGIN, 40f, paint) }
                        paint.textSize = 20f
                        paint.isFakeBoldText = true
                        canvas.drawText("TM-HERBAL", MARGIN + 70f, 65f, paint)
                        paint.textSize = 10f
                        paint.isFakeBoldText = false
                        canvas.drawText("Aplikasi Katalog Herbal Akacaraka", MARGIN + 70f, 85f, paint)
                        paint.strokeWidth = 2f
                        canvas.drawLine(MARGIN, 110f, PAGE_WIDTH - MARGIN, 110f, paint)
                        
                        y = 140f
                        paint.textSize = 18f
                        paint.isFakeBoldText = true
                        canvas.drawText("LAPORAN PENGUNJUNG TAMU", MARGIN, y, paint)
                        y += 25f
                        paint.textSize = 11f
                        paint.isFakeBoldText = false
                        canvas.drawText("Total kunjungan tamu terdeteksi: ${sortedVisitors.size} sesi", MARGIN, y, paint)
                        y += 30f
                    }

                    paint.textSize = 14f
                    paint.isFakeBoldText = true
                    canvas.drawText("No", MARGIN + 5f, y, paint)
                    canvas.drawText("ID Pengunjung", MARGIN + 45f, y, paint)
                    canvas.drawText("Waktu Kunjungan", MARGIN + 245f, y, paint)
                    
                    val headerY = y - 20f
                    y += 10f
                    canvas.drawLine(MARGIN, headerY, PAGE_WIDTH - MARGIN, headerY, paint)
                    canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
                    
                    canvas.drawLine(MARGIN, headerY, MARGIN, y, paint)
                    canvas.drawLine(MARGIN + 40f, headerY, MARGIN + 40f, y, paint)
                    canvas.drawLine(MARGIN + 240f, headerY, MARGIN + 240f, y, paint)
                    canvas.drawLine(PAGE_WIDTH - MARGIN, headerY, PAGE_WIDTH - MARGIN, y, paint)

                    paint.isFakeBoldText = false
                    val limit = if (p == 0) itemsPerPageFirst else itemsPerPageOther
                    var count = 0
                    while (count < limit && itemIndex < sortedVisitors.size) {
                        val visitor = sortedVisitors[itemIndex]
                        y += 25f
                        canvas.drawText((itemIndex + 1).toString(), MARGIN + 5f, y, paint)
                        canvas.drawText(visitor.id, MARGIN + 45f, y, paint)
                        canvas.drawText(sdf.format(Date(visitor.timestamp)), MARGIN + 245f, y, paint)
                        
                        val rowBottomY = y + 5f
                        canvas.drawLine(MARGIN, rowBottomY, PAGE_WIDTH - MARGIN, rowBottomY, paint)
                        canvas.drawLine(MARGIN, rowBottomY - 30f, MARGIN, rowBottomY, paint)
                        canvas.drawLine(MARGIN + 40f, rowBottomY - 30f, MARGIN + 40f, rowBottomY, paint)
                        canvas.drawLine(MARGIN + 240f, rowBottomY - 30f, MARGIN + 240f, rowBottomY, paint)
                        canvas.drawLine(PAGE_WIDTH - MARGIN, rowBottomY - 30f, PAGE_WIDTH - MARGIN, rowBottomY, paint)

                        y = rowBottomY
                        itemIndex++
                        count++
                    }

                    canvas.drawText("Halaman ${p + 1} dari $pageCount", MARGIN, FOOTER_Y, paint)

                    if (itemIndex >= sortedVisitors.size) {
                        y += 50f
                        if (y < 400f && p > 0) y = 400f
                        paint.textSize = 14f
                        paint.isFakeBoldText = false
                        canvas.drawText("Depok, $signatureDate", PAGE_WIDTH - MARGIN - 180f, y, paint)
                        y += 25f
                        canvas.drawText("Mengetahui,", PAGE_WIDTH - MARGIN - 180f, y, paint)
                        y += 70f
                        paint.isFakeBoldText = true
                        canvas.drawText("Yuyun", PAGE_WIDTH - MARGIN - 180f, y, paint)
                    }

                    pdfDocument.finishPage(page)
                }

                try {
                    pdfDocument.writeTo(FileOutputStream(destination.fileDescriptor))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.toString())
                    return
                } finally {
                    pdfDocument.close()
                }
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }
        }, null)
    }

    fun printStatReport(context: Context, items: List<HerbalItem>) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Laporan_Statistik_${System.currentTimeMillis()}"
        
        printManager.print(jobName, object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                val pdfDocument = PdfDocument()
                val logo = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
                val scaledLogo = logo?.let { Bitmap.createScaledBitmap(it, 60, 60, true) }
                val paint = Paint().apply {
                    typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                    textSize = 14f
                }
                val signatureDate = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("in", "ID")).format(Date())

                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                scaledLogo?.let { canvas.drawBitmap(it, MARGIN, 40f, paint) }
                paint.textSize = 20f
                paint.isFakeBoldText = true
                canvas.drawText("TM-HERBAL", MARGIN + 70f, 65f, paint)
                paint.textSize = 10f
                paint.isFakeBoldText = false
                canvas.drawText("Aplikasi Katalog Herbal Akacaraka", MARGIN + 70f, 85f, paint)
                paint.strokeWidth = 2f
                canvas.drawLine(MARGIN, 110f, PAGE_WIDTH - MARGIN, 110f, paint)

                var y = 140f
                paint.textSize = 18f
                paint.isFakeBoldText = true
                canvas.drawText("LAPORAN STATISTIK & AUDIT DATA", MARGIN, y, paint)
                y += 40f

                paint.textSize = 14f
                paint.isFakeBoldText = true
                canvas.drawText("I. DISTRIBUSI KATEGORI", MARGIN, y, paint)
                y += 25f
                
                val total = items.size.coerceAtLeast(1)
                val tanaman = items.count { it.category == HerbalCategory.TANAMAN }
                val minuman = items.count { it.category == HerbalCategory.MINUMAN }
                
                drawSimpleTable(canvas, paint, MARGIN, y, listOf("Kategori", "Jumlah", "Persentase"), listOf(
                    listOf("Tanaman Herbal", tanaman.toString(), "${tanaman * 100 / total}%"),
                    listOf("Minuman Herbal", minuman.toString(), "${minuman * 100 / total}%")
                ))
                
                y += 100f

                paint.isFakeBoldText = true
                canvas.drawText("II. KELENGKAPAN DATA (AUDIT)", MARGIN, y, paint)
                y += 25f
                
                val missingPhoto = items.count { it.imageUrl.isBlank() }
                val missingScientific = items.count { it.scientificName.isBlank() }
                val missingDesc = items.count { it.description.isBlank() }
                val missingSymptom = items.count { it.symptoms.isEmpty() }
                
                drawSimpleTable(canvas, paint, MARGIN, y, listOf("Komponen Data", "Terisi", "Kosong"), listOf(
                    listOf("Foto Utama", (items.size - missingPhoto).toString(), missingPhoto.toString()),
                    listOf("Nama Ilmiah", (items.size - missingScientific).toString(), missingScientific.toString()),
                    listOf("Deskripsi", (items.size - missingDesc).toString(), missingDesc.toString()),
                    listOf("Gejala/Symptom", (items.size - missingSymptom).toString(), missingSymptom.toString())
                ))

                y = PAGE_HEIGHT - 180f
                paint.isFakeBoldText = false
                paint.textSize = 14f
                canvas.drawText("Depok, $signatureDate", PAGE_WIDTH - MARGIN - 180f, y, paint)
                y += 25f
                canvas.drawText("Mengetahui,", PAGE_WIDTH - MARGIN - 180f, y, paint)
                y += 70f
                paint.isFakeBoldText = true
                canvas.drawText("Yuyun", PAGE_WIDTH - MARGIN - 180f, y, paint)

                pdfDocument.finishPage(page)

                try {
                    pdfDocument.writeTo(FileOutputStream(destination.fileDescriptor))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.toString())
                    return
                } finally {
                    pdfDocument.close()
                }
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }
            
            private fun drawSimpleTable(canvas: Canvas, paint: Paint, xPos: Float, yStart: Float, headers: List<String>, rows: List<List<String>>) {
                var y = yStart
                paint.textSize = 13f
                paint.isFakeBoldText = true

                canvas.drawText(headers[0], xPos + 5f, y, paint)
                canvas.drawText(headers[1], xPos + 165f, y, paint)
                canvas.drawText(headers[2], xPos + 305f, y, paint)
                
                y += 5f
                canvas.drawLine(xPos, yStart - 20f, PAGE_WIDTH - MARGIN, yStart - 20f, paint)
                canvas.drawLine(xPos, y, PAGE_WIDTH - MARGIN, y, paint)
                
                paint.isFakeBoldText = false
                paint.textSize = 13f
                rows.forEach { row ->
                    y += 25f
                    canvas.drawText(row[0], xPos + 5f, y, paint)
                    canvas.drawText(row[1], xPos + 165f, y, paint)
                    canvas.drawText(row[2], xPos + 305f, y, paint)
                    canvas.drawLine(xPos, y + 5f, PAGE_WIDTH - MARGIN, y + 5f, paint)
                }

                val bottomY = y + 5f
                canvas.drawLine(xPos, yStart - 20f, xPos, bottomY, paint)
                canvas.drawLine(xPos + 160f, yStart - 20f, xPos + 160f, bottomY, paint)
                canvas.drawLine(xPos + 300f, yStart - 20f, xPos + 300f, bottomY, paint)
                canvas.drawLine(PAGE_WIDTH - MARGIN, yStart - 20f, PAGE_WIDTH - MARGIN, bottomY, paint)
            }
        }, null)
    }
}
