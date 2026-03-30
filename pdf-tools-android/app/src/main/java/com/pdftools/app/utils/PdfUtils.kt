package com.pdftools.app.utils

import android.content.Context
import android.net.Uri
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfEncryption
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.kernel.utils.PdfMerger
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Utility class providing core PDF manipulation operations using iText7.
 */
object PdfUtils {

    // -------------------------------------------------------------------------
    // Merge
    // -------------------------------------------------------------------------

    /**
     * Merges multiple PDF files (provided as [Uri] list) into [outputFile].
     */
    fun mergePdfs(context: Context, uris: List<Uri>, outputFile: File) {
        PdfWriter(FileOutputStream(outputFile)).use { writer ->
            PdfDocument(writer).use { mergedDoc ->
                val merger = PdfMerger(mergedDoc)
                for (uri in uris) {
                    openInputStream(context, uri)?.use { inputStream ->
                        PdfReader(inputStream).use { reader ->
                            PdfDocument(reader).use { srcDoc ->
                                merger.merge(srcDoc, 1, srcDoc.numberOfPages)
                            }
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Split
    // -------------------------------------------------------------------------

    /**
     * Splits [sourceUri] into individual single-page PDFs placed in [outputDir].
     * Returns the list of generated files.
     */
    fun splitPdf(context: Context, sourceUri: Uri, outputDir: File): List<File> {
        val results = mutableListOf<File>()
        openInputStream(context, sourceUri)?.use { inputStream ->
            PdfReader(inputStream).use { reader ->
                PdfDocument(reader).use { srcDoc ->
                    for (pageNum in 1..srcDoc.numberOfPages) {
                        val outFile = File(outputDir, "page_$pageNum.pdf")
                        PdfWriter(FileOutputStream(outFile)).use { writer ->
                            PdfDocument(writer).use { destDoc ->
                                val merger = PdfMerger(destDoc)
                                merger.merge(srcDoc, pageNum, pageNum)
                            }
                        }
                        results.add(outFile)
                    }
                }
            }
        }
        return results
    }

    // -------------------------------------------------------------------------
    // Split by page ranges
    // -------------------------------------------------------------------------

    /**
     * Splits a PDF into chunks defined by [ranges] (each range is a Pair of
     * 1-based start/end page numbers). Returns generated files.
     */
    fun splitPdfByRanges(
        context: Context,
        sourceUri: Uri,
        ranges: List<Pair<Int, Int>>,
        outputDir: File
    ): List<File> {
        val results = mutableListOf<File>()
        openInputStream(context, sourceUri)?.use { inputStream ->
            PdfReader(inputStream).use { reader ->
                PdfDocument(reader).use { srcDoc ->
                    ranges.forEachIndexed { index, (from, to) ->
                        val outFile = File(outputDir, "split_${index + 1}.pdf")
                        PdfWriter(FileOutputStream(outFile)).use { writer ->
                            PdfDocument(writer).use { destDoc ->
                                val merger = PdfMerger(destDoc)
                                merger.merge(srcDoc, from, to)
                            }
                        }
                        results.add(outFile)
                    }
                }
            }
        }
        return results
    }

    // -------------------------------------------------------------------------
    // Compress
    // -------------------------------------------------------------------------

    /**
     * Produces a compressed copy of [sourceUri] at [outputFile] by re-writing
     * with optimized writer settings.
     */
    fun compressPdf(context: Context, sourceUri: Uri, outputFile: File) {
        openInputStream(context, sourceUri)?.use { inputStream ->
            PdfReader(inputStream).use { reader ->
                val writerProps = WriterProperties().useSmartMode()
                PdfWriter(FileOutputStream(outputFile), writerProps).use { writer ->
                    PdfDocument(reader, writer).use { /* triggers copy+optimize */ }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Rotate
    // -------------------------------------------------------------------------

    /**
     * Rotates every page in [sourceUri] by [degrees] (must be 90, 180 or 270)
     * and writes the result to [outputFile].
     */
    fun rotatePdf(context: Context, sourceUri: Uri, outputFile: File, degrees: Int) {
        require(degrees in listOf(90, 180, 270)) { "degrees must be 90, 180 or 270" }
        openInputStream(context, sourceUri)?.use { inputStream ->
            PdfReader(inputStream).use { reader ->
                PdfWriter(FileOutputStream(outputFile)).use { writer ->
                    PdfDocument(reader, writer).use { pdfDoc ->
                        for (i in 1..pdfDoc.numberOfPages) {
                            val page = pdfDoc.getPage(i)
                            val current = page.pageRotation
                            page.setRotation((current + degrees) % 360)
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Watermark (text)
    // -------------------------------------------------------------------------

    /**
     * Stamps [watermarkText] diagonally across every page of [sourceUri] and
     * writes the result to [outputFile].
     */
    fun addTextWatermark(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        watermarkText: String,
        opacity: Float = 0.3f,
        fontSize: Float = 52f
    ) {
        openInputStream(context, sourceUri)?.use { inputStream ->
            PdfReader(inputStream).use { reader ->
                PdfWriter(FileOutputStream(outputFile)).use { writer ->
                    PdfDocument(reader, writer).use { pdfDoc ->
                        val font = PdfFontFactory.createFont()
                        val doc = Document(pdfDoc)
                        for (i in 1..pdfDoc.numberOfPages) {
                            val page = pdfDoc.getPage(i)
                            val ps = page.pageSize
                            val xCenter = ps.left + (ps.width - fontSize * watermarkText.length * 0.5f) / 2
                            val yCenter = ps.bottom + (ps.height - fontSize) / 2
                            val para = Paragraph(watermarkText)
                                .setFont(font)
                                .setFontSize(fontSize)
                                .setFontColor(ColorConstants.LIGHT_GRAY, opacity)
                                .setRotationAngle(Math.toRadians(45.0))
                                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFixedPosition(i, xCenter, yCenter, ps.width)
                            doc.add(para)
                        }
                    }
                }
            }
        }
    }

    /**
     * Stamps an image watermark on every page of [sourceUri].
     */
    fun addImageWatermark(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        imageUri: Uri,
        opacity: Float = 0.3f
    ) {
        openInputStream(context, sourceUri)?.use { inputStream ->
            openInputStream(context, imageUri)?.use { imgStream ->
                val imgBytes = imgStream.readBytes()
                PdfReader(inputStream).use { reader ->
                    PdfWriter(FileOutputStream(outputFile)).use { writer ->
                        PdfDocument(reader, writer).use { pdfDoc ->
                            val imgData = ImageDataFactory.create(imgBytes)
                            for (i in 1..pdfDoc.numberOfPages) {
                                val page = pdfDoc.getPage(i)
                                val ps = page.pageSize
                                val doc = Document(pdfDoc)
                                val image = Image(imgData)
                                    .setOpacity(opacity)
                                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                                    .setFixedPosition(
                                        i,
                                        (ps.width - image.imageWidth) / 2,
                                        (ps.height - image.imageHeight) / 2
                                    )
                                doc.add(image)
                            }
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Protect / Unlock
    // -------------------------------------------------------------------------

    /**
     * Password-protects [sourceUri] with [userPassword] and [ownerPassword].
     */
    fun protectPdf(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        userPassword: String,
        ownerPassword: String
    ) {
        openInputStream(context, sourceUri)?.use { inputStream ->
            PdfReader(inputStream).use { reader ->
                val writerProps = WriterProperties()
                    .setStandardEncryption(
                        userPassword.toByteArray(),
                        ownerPassword.toByteArray(),
                        PdfEncryption.ALLOW_PRINTING,
                        WriterProperties.ENCRYPTION_AES_256
                    )
                PdfWriter(FileOutputStream(outputFile), writerProps).use { writer ->
                    PdfDocument(reader, writer).use { }
                }
            }
        }
    }

    /**
     * Removes password protection from [sourceUri] (caller must supply the
     * [password] to open it) and writes an unprotected copy to [outputFile].
     */
    fun unlockPdf(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        password: String
    ) {
        openInputStream(context, sourceUri)?.use { inputStream ->
            val readerProps = com.itextpdf.kernel.pdf.ReaderProperties()
                .setPassword(password.toByteArray())
            PdfReader(inputStream, readerProps).use { reader ->
                PdfWriter(FileOutputStream(outputFile)).use { writer ->
                    PdfDocument(reader, writer).use { }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Page numbers
    // -------------------------------------------------------------------------

    /**
     * Adds page numbers to every page of [sourceUri] at [position] and writes
     * the result to [outputFile].
     * [position] values: "bottom-center", "bottom-right", "bottom-left",
     *                     "top-center", "top-right", "top-left"
     */
    fun addPageNumbers(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        position: String = "bottom-center",
        startNumber: Int = 1,
        fontSize: Float = 12f
    ) {
        openInputStream(context, sourceUri)?.use { inputStream ->
            PdfReader(inputStream).use { reader ->
                PdfWriter(FileOutputStream(outputFile)).use { writer ->
                    PdfDocument(reader, writer).use { pdfDoc ->
                        val doc = Document(pdfDoc)
                        val font = PdfFontFactory.createFont()
                        val totalPages = pdfDoc.numberOfPages
                        for (i in 1..totalPages) {
                            val page = pdfDoc.getPage(i)
                            val ps = page.pageSize
                            val number = i + startNumber - 1
                            val text = "$number / $totalPages"
                            val para = Paragraph(text).setFont(font).setFontSize(fontSize)
                            val (x, y, alignment) = when (position) {
                                "bottom-right" -> Triple(ps.right - 72f, ps.bottom + 18f, TextAlignment.RIGHT)
                                "bottom-left"  -> Triple(ps.left + 18f,  ps.bottom + 18f, TextAlignment.LEFT)
                                "top-center"   -> Triple(ps.left,         ps.top - 28f,    TextAlignment.CENTER)
                                "top-right"    -> Triple(ps.right - 72f,  ps.top - 28f,    TextAlignment.RIGHT)
                                "top-left"     -> Triple(ps.left + 18f,   ps.top - 28f,    TextAlignment.LEFT)
                                else           -> Triple(ps.left,         ps.bottom + 18f, TextAlignment.CENTER)
                            }
                            para.setTextAlignment(alignment)
                                .setFixedPosition(i, x, y, ps.width - 36f)
                            doc.add(para)
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Organize (reorder / delete pages)
    // -------------------------------------------------------------------------

    /**
     * Reorders (and optionally removes) pages from [sourceUri] according to
     * [pageOrder] (1-based). Write result to [outputFile].
     */
    fun organizePdf(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        pageOrder: List<Int>
    ) {
        openInputStream(context, sourceUri)?.use { inputStream ->
            PdfReader(inputStream).use { reader ->
                PdfWriter(FileOutputStream(outputFile)).use { writer ->
                    PdfDocument(reader).use { srcDoc ->
                        PdfDocument(writer).use { destDoc ->
                            val merger = PdfMerger(destDoc)
                            for (pageNum in pageOrder) {
                                merger.merge(srcDoc, pageNum, pageNum)
                            }
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // JPG → PDF
    // -------------------------------------------------------------------------

    /**
     * Converts a list of image URIs to a single PDF at [outputFile].
     */
    fun imagesToPdf(context: Context, imageUris: List<Uri>, outputFile: File) {
        PdfWriter(FileOutputStream(outputFile)).use { writer ->
            PdfDocument(writer).use { pdfDoc ->
                val doc = Document(pdfDoc)
                for (uri in imageUris) {
                    openInputStream(context, uri)?.use { inputStream ->
                        val bytes = inputStream.readBytes()
                        val imgData = ImageDataFactory.create(bytes)
                        val image = Image(imgData)
                        // Fit to page
                        val pageSize = PageSize(image.imageWidth, image.imageHeight)
                        pdfDoc.addNewPage(pageSize)
                        image.setFixedPosition(pdfDoc.numberOfPages, 0f, 0f)
                            .setWidth(pageSize.width)
                            .setHeight(pageSize.height)
                        doc.add(image)
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Crop
    // -------------------------------------------------------------------------

    /**
     * Crops every page of [sourceUri] to the given [cropBox] rectangle
     * (in default user-space units) and writes result to [outputFile].
     */
    fun cropPdf(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        cropBox: Rectangle
    ) {
        openInputStream(context, sourceUri)?.use { inputStream ->
            PdfReader(inputStream).use { reader ->
                PdfWriter(FileOutputStream(outputFile)).use { writer ->
                    PdfDocument(reader, writer).use { pdfDoc ->
                        for (i in 1..pdfDoc.numberOfPages) {
                            pdfDoc.getPage(i).setCropBox(cropBox)
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // PDF to PDF/A
    // -------------------------------------------------------------------------

    /**
     * Converts [sourceUri] to a PDF/A-1b compliant document at [outputFile].
     * Note: A full PDF/A conversion also requires embedding all fonts and providing
     * an ICC colour profile. This implementation performs a best-effort conversion
     * using iText7's PdfADocument; supply an sRGB ICC profile as `srgb.icc` in
     * the app assets folder for complete compliance.
     */
    fun convertToPdfA(context: Context, sourceUri: Uri, outputFile: File) {
        openInputStream(context, sourceUri)?.use { inputStream ->
            PdfReader(inputStream).use { reader ->
                // Try to open the ICC profile from assets; fall back to a basic copy if absent
                val iccStream = try {
                    context.assets.open("srgb.icc")
                } catch (e: Exception) {
                    null
                }

                if (iccStream != null) {
                    val outputIntent = com.itextpdf.kernel.pdf.PdfOutputIntent(
                        "Custom", "",
                        "http://www.color.org",
                        "sRGB IEC61966-2.1",
                        iccStream
                    )
                    val writerProps = WriterProperties().addXmpMetadata()
                    PdfWriter(FileOutputStream(outputFile), writerProps).use { writer ->
                        val pdfADoc = com.itextpdf.pdfa.PdfADocument(
                            writer,
                            com.itextpdf.kernel.pdf.PdfAConformanceLevel.PDF_A_1B,
                            outputIntent
                        )
                        PdfDocument(reader).use { srcDoc ->
                            PdfMerger(pdfADoc).merge(srcDoc, 1, srcDoc.numberOfPages)
                        }
                        pdfADoc.close()
                    }
                    iccStream.close()
                } else {
                    // Fallback: re-write with XMP metadata only
                    val writerProps = WriterProperties().addXmpMetadata()
                    PdfWriter(FileOutputStream(outputFile), writerProps).use { writer ->
                        PdfDocument(reader, writer).use { }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun openInputStream(context: Context, uri: Uri): InputStream? =
        context.contentResolver.openInputStream(uri)
}
