package com.pdftools.app.features.pdfa

import android.net.Uri
import android.os.Bundle
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivitySimplePdfBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.utils.PdfUtils

/**
 * PDF to PDF/A — converts a PDF to the ISO-standardized PDF/A format for
 * long-term archiving.
 */
class PdfToPdfAActivity : BasePdfActivity() {

    private lateinit var binding: ActivitySimplePdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimplePdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tvTitle.setText(R.string.feature_pdf_to_pdfa)
        binding.tvDescription.setText(R.string.desc_pdf_to_pdfa)
        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnAction.setOnClickListener { convert() }
        binding.btnAction.isEnabled = false
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnAction.isEnabled = true
    }

    private fun convert() {
        val uri = selectedPdfUri ?: return
        runWithProgress(binding.progressBar) {
            val out = outputFile("pdfa_${System.currentTimeMillis()}.pdf")
            PdfUtils.convertToPdfA(this, uri, out)
            out
        }
    }
}
