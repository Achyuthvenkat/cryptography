package com.pdftools.app.features.convert

import android.net.Uri
import android.os.Bundle
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityConvertBinding
import com.pdftools.app.features.BasePdfActivity

/**
 * Word to PDF — converts DOC/DOCX files to PDF.
 */
class WordToPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityConvertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConvertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tvConvertTitle.setText(R.string.feature_word_to_pdf)
        binding.tvConvertDescription.setText(R.string.desc_word_to_pdf)
        binding.btnSelectFile.setOnClickListener { launchDocPicker() }
        binding.btnConvert.setOnClickListener { convert() }
        binding.btnConvert.isEnabled = false
    }

    private fun launchDocPicker() {
        val intent = android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(android.content.Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(android.content.Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ))
        }
        pickPdfLauncher.launch(intent)
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnConvert.isEnabled = true
    }

    private fun convert() {
        val uri = selectedPdfUri ?: return
        runWithProgress(binding.progressBar) {
            val out = outputFile("converted_${System.currentTimeMillis()}.pdf")
            ConversionUtils.wordToPdf(this, uri, out)
            out
        }
    }
}
