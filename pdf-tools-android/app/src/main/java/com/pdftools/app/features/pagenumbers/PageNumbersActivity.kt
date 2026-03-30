package com.pdftools.app.features.pagenumbers

import android.net.Uri
import android.os.Bundle
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityPageNumbersBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.utils.PdfUtils

/**
 * Page Numbers — adds page numbers to a PDF at a user-specified position.
 */
class PageNumbersActivity : BasePdfActivity() {

    private lateinit var binding: ActivityPageNumbersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPageNumbersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnApply.setOnClickListener { applyPageNumbers() }
        binding.btnApply.isEnabled = false
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnApply.isEnabled = true
    }

    private fun applyPageNumbers() {
        val uri = selectedPdfUri ?: return
        val position = when (binding.spinnerPosition.selectedItem?.toString()) {
            getString(R.string.position_top_left)    -> "top-left"
            getString(R.string.position_top_right)   -> "top-right"
            getString(R.string.position_top_center)  -> "top-center"
            getString(R.string.position_bottom_left) -> "bottom-left"
            getString(R.string.position_bottom_right)-> "bottom-right"
            else                                      -> "bottom-center"
        }
        val startNumber = binding.etStartNumber.text.toString().toIntOrNull() ?: 1
        val fontSize = binding.etFontSize.text.toString().toFloatOrNull() ?: 12f

        runWithProgress(binding.progressBar) {
            val out = outputFile("numbered_${System.currentTimeMillis()}.pdf")
            PdfUtils.addPageNumbers(this, uri, out, position, startNumber, fontSize)
            out
        }
    }
}
