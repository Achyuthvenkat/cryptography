package com.pdftools.app.features.repair

import android.net.Uri
import android.os.Bundle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.ReaderProperties
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivitySimplePdfBinding
import com.pdftools.app.features.BasePdfActivity
import java.io.FileOutputStream

/**
 * Repair PDF — attempts to recover data from a damaged PDF by re-reading it
 * with iText's lenient reader and writing a clean copy.
 */
class RepairPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivitySimplePdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimplePdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tvTitle.setText(R.string.feature_repair_pdf)
        binding.tvDescription.setText(R.string.desc_repair_pdf)
        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnAction.setOnClickListener { repairPdf() }
        binding.btnAction.isEnabled = false
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnAction.isEnabled = true
    }

    private fun repairPdf() {
        val uri = selectedPdfUri ?: return
        runWithProgress(binding.progressBar) {
            val out = outputFile("repaired_${System.currentTimeMillis()}.pdf")
            contentResolver.openInputStream(uri)?.use { inputStream ->
                // Use lenient reading to handle corrupted cross-reference tables
                val readerProps = ReaderProperties().setUnethicalReading(true)
                PdfReader(inputStream, readerProps).use { reader ->
                    PdfWriter(FileOutputStream(out)).use { writer ->
                        PdfDocument(reader, writer).use { }
                    }
                }
            }
            out
        }
    }
}
