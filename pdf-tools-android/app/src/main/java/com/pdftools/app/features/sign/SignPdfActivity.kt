package com.pdftools.app.features.sign

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivitySignPdfBinding
import com.pdftools.app.features.BasePdfActivity
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream

/**
 * Sign PDF — draw a signature on a canvas and stamp it onto the PDF.
 */
class SignPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivitySignPdfBinding
    private lateinit var signatureBitmap: Bitmap
    private lateinit var signatureCanvas: Canvas
    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 5f
    }
    private val path = Path()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnSign.setOnClickListener { signPdf() }
        binding.btnClearSignature.setOnClickListener { clearSignature() }
        binding.btnSign.isEnabled = false

        // Set up drawing surface after layout
        binding.signatureView.post {
            setupSignaturePad()
        }
    }

    private fun setupSignaturePad() {
        val w = binding.signatureView.width.coerceAtLeast(1)
        val h = binding.signatureView.height.coerceAtLeast(200)
        signatureBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        signatureCanvas = Canvas(signatureBitmap)
        signatureCanvas.drawColor(Color.WHITE)
        binding.signatureView.setImageBitmap(signatureBitmap)

        binding.signatureView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    path.moveTo(event.x, event.y)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    path.lineTo(event.x, event.y)
                    signatureCanvas.drawPath(path, paint)
                    binding.signatureView.invalidate()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    path.lineTo(event.x, event.y)
                    signatureCanvas.drawPath(path, paint)
                    binding.signatureView.invalidate()
                    true
                }
                else -> false
            }
        }
    }

    private fun clearSignature() {
        path.reset()
        if (::signatureCanvas.isInitialized) {
            signatureCanvas.drawColor(Color.WHITE)
            binding.signatureView.invalidate()
        }
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnSign.isEnabled = true
    }

    private fun signPdf() {
        val uri = selectedPdfUri ?: return
        val xPos = binding.etXPosition.text.toString().toFloatOrNull() ?: 36f
        val yPos = binding.etYPosition.text.toString().toFloatOrNull() ?: 36f
        val pageNum = binding.etPageNumber.text.toString().toIntOrNull() ?: 1

        runWithProgress(binding.progressBar) {
            // Export signature bitmap to bytes
            val baos = ByteArrayOutputStream()
            signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val sigBytes = baos.toByteArray()

            val out = outputFile("signed_${System.currentTimeMillis()}.pdf")
            contentResolver.openInputStream(uri)?.use { inputStream ->
                PdfReader(inputStream).use { reader ->
                    PdfWriter(FileOutputStream(out)).use { writer ->
                        PdfDocument(reader, writer).use { pdfDoc ->
                            val doc = Document(pdfDoc)
                            val imgData = ImageDataFactory.create(sigBytes)
                            val image = Image(imgData)
                                .setWidth(150f)
                                .setFixedPosition(pageNum, xPos, yPos)
                            doc.add(image)
                        }
                    }
                }
            }
            out
        }
    }
}
