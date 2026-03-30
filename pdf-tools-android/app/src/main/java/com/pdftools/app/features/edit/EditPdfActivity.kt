package com.pdftools.app.features.edit

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityEditPdfBinding
import com.pdftools.app.features.BasePdfActivity
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import java.io.File
import java.io.FileOutputStream

/**
 * Edit PDF — add text annotations and freehand drawings to a PDF.
 */
class EditPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityEditPdfBinding
    private var editMode: EditMode = EditMode.TEXT

    enum class EditMode { TEXT, FREEHAND }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupModeToggle()
        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnApply.setOnClickListener { applyEdits() }
        binding.btnApply.isEnabled = false
    }

    private fun setupModeToggle() {
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                editMode = when (checkedId) {
                    R.id.btnTextMode -> EditMode.TEXT
                    R.id.btnDrawMode -> EditMode.FREEHAND
                    else -> EditMode.TEXT
                }
                binding.textEditGroup.visibility =
                    if (editMode == EditMode.TEXT) View.VISIBLE else View.GONE
                binding.drawingCanvas.visibility =
                    if (editMode == EditMode.FREEHAND) View.VISIBLE else View.GONE
            }
        }
        // Font size spinner
        val sizes = arrayOf("10", "12", "14", "16", "18", "24", "32", "48")
        binding.spinnerFontSize.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, sizes).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnApply.isEnabled = true
    }

    private fun applyEdits() {
        val uri = selectedPdfUri ?: return
        val text = binding.etAnnotationText.text.toString()
        val fontSize = binding.spinnerFontSize.selectedItem.toString().toFloatOrNull() ?: 12f
        val xPos = binding.etXPosition.text.toString().toFloatOrNull() ?: 72f
        val yPos = binding.etYPosition.text.toString().toFloatOrNull() ?: 72f
        val pageNum = binding.etPageNumber.text.toString().toIntOrNull() ?: 1

        runWithProgress(binding.progressBar) {
            val out = outputFile("edited_${System.currentTimeMillis()}.pdf")
            openInputStream(uri)?.use { inputStream ->
                PdfReader(inputStream).use { reader ->
                    PdfWriter(FileOutputStream(out)).use { writer ->
                        PdfDocument(reader, writer).use { pdfDoc ->
                            val doc = Document(pdfDoc)
                            if (text.isNotEmpty()) {
                                val font = PdfFontFactory.createFont()
                                val para = Paragraph(text)
                                    .setFont(font)
                                    .setFontSize(fontSize)
                                    .setFontColor(ColorConstants.BLACK)
                                    .setFixedPosition(pageNum, xPos, yPos, 300f)
                                    .setTextAlignment(TextAlignment.LEFT)
                                doc.add(para)
                            }
                        }
                    }
                }
            }
            out
        }
    }

    private fun openInputStream(uri: Uri) = contentResolver.openInputStream(uri)
}
