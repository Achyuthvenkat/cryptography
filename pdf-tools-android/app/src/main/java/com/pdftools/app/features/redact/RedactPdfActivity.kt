package com.pdftools.app.features.redact

import android.net.Uri
import android.os.Bundle
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.pdfcleanup.PdfCleanUpLocation
import com.itextpdf.pdfcleanup.PdfCleaner
import com.itextpdf.pdfcleanup.autosweep.CompositeCleanupStrategy
import com.itextpdf.pdfcleanup.autosweep.RegexBasedCleanupStrategy
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityRedactPdfBinding
import com.pdftools.app.features.BasePdfActivity
import java.io.FileOutputStream

/**
 * Redact PDF — permanently removes sensitive text matching a given regex pattern,
 * and/or a specified rectangular region from each page.
 */
class RedactPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityRedactPdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRedactPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnRedact.setOnClickListener { redactPdf() }
        binding.btnRedact.isEnabled = false
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnRedact.isEnabled = true
    }

    private fun redactPdf() {
        val uri = selectedPdfUri ?: return
        val pattern = binding.etPattern.text.toString().trim()
        val useRegion = binding.cbUseRegion.isChecked
        val x      = binding.etRegionX.text.toString().toFloatOrNull() ?: 0f
        val y      = binding.etRegionY.text.toString().toFloatOrNull() ?: 0f
        val width  = binding.etRegionWidth.text.toString().toFloatOrNull() ?: 100f
        val height = binding.etRegionHeight.text.toString().toFloatOrNull() ?: 20f

        if (pattern.isEmpty() && !useRegion) {
            showSnack(getString(R.string.error_redact_no_input), binding.root)
            return
        }

        runWithProgress(binding.progressBar) {
            val out = outputFile("redacted_${System.currentTimeMillis()}.pdf")
            contentResolver.openInputStream(uri)?.use { inputStream ->
                PdfReader(inputStream).use { reader ->
                    PdfWriter(FileOutputStream(out)).use { writer ->
                        PdfDocument(reader, writer).use { pdfDoc ->
                            if (pattern.isNotEmpty()) {
                                val strategy = CompositeCleanupStrategy().also {
                                    it.add(RegexBasedCleanupStrategy(pattern)
                                        .setRedactionColor(ColorConstants.BLACK))
                                }
                                PdfCleaner.autoSweepCleanUp(pdfDoc, strategy)
                            }
                            if (useRegion) {
                                val cleanupLocations = (1..pdfDoc.numberOfPages).map { pageNum ->
                                    PdfCleanUpLocation(
                                        pageNum,
                                        Rectangle(x, y, width, height),
                                        ColorConstants.BLACK
                                    )
                                }
                                PdfCleaner.cleanUp(pdfDoc, cleanupLocations)
                            }
                        }
                    }
                }
            }
            out
        }
    }
}
