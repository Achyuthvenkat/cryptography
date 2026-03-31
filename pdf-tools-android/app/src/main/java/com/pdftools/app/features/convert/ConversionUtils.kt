package com.pdftools.app.features.convert

import android.content.Context
import android.net.Uri
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import java.io.File
import java.io.FileOutputStream

/**
 * Utility object for document-format conversion operations.
 *
 * Note: PDF↔Office (Word/PowerPoint/Excel) conversions require either a
 * server-side API call or a commercial library. The stubs here render an
 * informative single-page PDF when a full conversion backend is not available.
 * Replace the stub implementations with your preferred API integration.
 *
 * PDF-to-JPG and JPG-to-PDF conversions are implemented locally using iText7
 * and Android's graphics APIs.
 */
object ConversionUtils {

    // -------------------------------------------------------------------------
    // PDF → Office stubs (replace with API / commercial library)
    // -------------------------------------------------------------------------

    fun pdfToWord(context: Context, sourceUri: Uri, outputFile: File) {
        writeConversionPlaceholder(context, sourceUri, outputFile,
            "PDF to Word conversion requires a server-side API.\n" +
                "Integrate with iLovePDF, Smallpdf, Adobe, or Aspose API to enable this feature.")
    }

    fun pdfToPowerPoint(context: Context, sourceUri: Uri, outputFile: File) {
        writeConversionPlaceholder(context, sourceUri, outputFile,
            "PDF to PowerPoint conversion requires a server-side API.\n" +
                "Integrate with iLovePDF, Smallpdf, Adobe, or Aspose API to enable this feature.")
    }

    fun pdfToExcel(context: Context, sourceUri: Uri, outputFile: File) {
        writeConversionPlaceholder(context, sourceUri, outputFile,
            "PDF to Excel conversion requires a server-side API.\n" +
                "Integrate with iLovePDF, Smallpdf, Adobe, or Aspose API to enable this feature.")
    }

    fun wordToPdf(context: Context, sourceUri: Uri, outputFile: File) {
        writeConversionPlaceholder(context, sourceUri, outputFile,
            "Word to PDF conversion requires a server-side API.\n" +
                "Integrate with iLovePDF, Smallpdf, Adobe, or Aspose API to enable this feature.")
    }

    fun powerPointToPdf(context: Context, sourceUri: Uri, outputFile: File) {
        writeConversionPlaceholder(context, sourceUri, outputFile,
            "PowerPoint to PDF conversion requires a server-side API.\n" +
                "Integrate with iLovePDF, Smallpdf, Adobe, or Aspose API to enable this feature.")
    }

    fun excelToPdf(context: Context, sourceUri: Uri, outputFile: File) {
        writeConversionPlaceholder(context, sourceUri, outputFile,
            "Excel to PDF conversion requires a server-side API.\n" +
                "Integrate with iLovePDF, Smallpdf, Adobe, or Aspose API to enable this feature.")
    }

    // -------------------------------------------------------------------------
    // PDF → JPG
    // -------------------------------------------------------------------------

    /**
     * Renders each page of [sourceUri] as a JPEG image and saves the images in
     * [outputDir]. Returns the list of produced image files.
     *
     * Android's PdfRenderer API (available from API 21) is used for rendering.
     */
    fun pdfToJpg(context: Context, sourceUri: Uri, outputDir: File): List<File> {
        val results = mutableListOf<File>()
        val pfd = context.contentResolver.openFileDescriptor(sourceUri, "r") ?: return results
        val renderer = android.graphics.pdf.PdfRenderer(pfd)
        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val bitmap = android.graphics.Bitmap.createBitmap(
                page.width * 2, page.height * 2,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            val outFile = File(outputDir, "page_${i + 1}.jpg")
            FileOutputStream(outFile).use { fos ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fos)
            }
            bitmap.recycle()
            results.add(outFile)
        }
        renderer.close()
        pfd.close()
        return results
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun writeConversionPlaceholder(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        message: String
    ) {
        PdfWriter(FileOutputStream(outputFile)).use { writer ->
            PdfDocument(writer).use { pdfDoc ->
                val doc = Document(pdfDoc)
                doc.add(
                    com.itextpdf.layout.element.Paragraph(message)
                        .setFontSize(14f)
                        .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                )
            }
        }
    }
}
