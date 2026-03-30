package com.pdftools.app.features.convert

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityConvertBinding
import com.pdftools.app.features.BasePdfActivity

/**
 * PDF to Word — converts a PDF to DOC/DOCX.
 *
 * Full fidelity PDF→Word conversion requires server-side processing or a
 * commercial library (e.g. iText DITO, Aspose.Words). This activity provides
 * the correct UI scaffolding and delegates the heavy lifting to an API backend.
 */
class PdfToWordActivity : BasePdfActivity() {

    private lateinit var binding: ActivityConvertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConvertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tvConvertTitle.setText(R.string.feature_pdf_to_word)
        binding.tvConvertDescription.setText(R.string.desc_pdf_to_word)
        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnConvert.setOnClickListener { convert() }
        binding.btnConvert.isEnabled = false
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnConvert.isEnabled = true
    }

    private fun convert() {
        val uri = selectedPdfUri ?: return
        runWithProgress(binding.progressBar) {
            val out = outputFile("converted_${System.currentTimeMillis()}.docx")
            ConversionUtils.pdfToWord(this, uri, out)
            out
        }
    }
}
