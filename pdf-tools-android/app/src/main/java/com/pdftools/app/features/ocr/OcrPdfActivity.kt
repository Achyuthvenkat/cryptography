package com.pdftools.app.features.ocr

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityOcrPdfBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.features.convert.ConversionUtils
import java.io.File
import java.io.FileOutputStream

/**
 * OCR PDF — converts a scanned (image-based) PDF into a searchable PDF with
 * embedded text, using Google ML Kit text recognition.
 */
class OcrPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityOcrPdfBinding
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnRunOcr.setOnClickListener { runOcr() }
        binding.btnRunOcr.isEnabled = false
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnRunOcr.isEnabled = true
    }

    private fun runOcr() {
        val uri = selectedPdfUri ?: return
        runWithProgress(
            binding.progressBar,
            onSuccess = { out ->
                shareFile(out)
            }
        ) {
            // Render PDF pages to bitmaps, then OCR each page
            val outDir = File(getExternalFilesDir(null), "ocr_pages_${System.currentTimeMillis()}")
            outDir.mkdirs()
            val pageImages = ConversionUtils.pdfToJpg(this, uri, outDir)

            val allText = StringBuilder()
            for (pageFile in pageImages) {
                val bitmap = android.graphics.BitmapFactory.decodeFile(pageFile.absolutePath)
                if (bitmap != null) {
                    val text = recognizeTextSync(bitmap)
                    allText.appendLine(text)
                    allText.appendLine("--- Page ---")
                    bitmap.recycle()
                }
            }

            val out = outputFile("ocr_${System.currentTimeMillis()}.pdf")
            PdfWriter(FileOutputStream(out)).use { writer ->
                PdfDocument(writer).use { pdfDoc ->
                    val doc = Document(pdfDoc)
                    doc.add(Paragraph(allText.toString()).setFontSize(10f))
                }
            }
            out
        }
    }

    private suspend fun recognizeTextSync(bitmap: Bitmap): String =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { result -> cont.resume(result.text) {} }
                .addOnFailureListener { e -> cont.resume("") {} }
        }

    override fun onDestroy() {
        super.onDestroy()
        recognizer.close()
    }
}
