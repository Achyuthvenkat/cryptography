package com.pdftools.app.features.rotate

import android.net.Uri
import android.os.Bundle
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityRotatePdfBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.utils.PdfUtils

/**
 * Rotate PDF — rotate all pages of a PDF by 90, 180 or 270 degrees.
 */
class RotatePdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityRotatePdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRotatePdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnRotate.setOnClickListener { rotatePdf() }
        binding.btnRotate.isEnabled = false
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnRotate.isEnabled = true
    }

    private fun rotatePdf() {
        val uri = selectedPdfUri ?: return
        val degrees = when (binding.rotationGroup.checkedRadioButtonId) {
            R.id.radio180 -> 180
            R.id.radio270 -> 270
            else          -> 90
        }
        runWithProgress(binding.progressBar) {
            val out = outputFile("rotated_${System.currentTimeMillis()}.pdf")
            PdfUtils.rotatePdf(this, uri, out, degrees)
            out
        }
    }
}
