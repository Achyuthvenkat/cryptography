package com.pdftools.app.features.crop

import android.net.Uri
import android.os.Bundle
import com.itextpdf.kernel.geom.Rectangle
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityCropPdfBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.utils.PdfUtils

/**
 * Crop PDF — sets a crop box on every page to trim margins or select a region.
 */
class CropPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityCropPdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnCrop.setOnClickListener { cropPdf() }
        binding.btnCrop.isEnabled = false
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnCrop.isEnabled = true
    }

    private fun cropPdf() {
        val uri = selectedPdfUri ?: return
        val x      = binding.etX.text.toString().toFloatOrNull() ?: 0f
        val y      = binding.etY.text.toString().toFloatOrNull() ?: 0f
        val width  = binding.etWidth.text.toString().toFloatOrNull() ?: 595f
        val height = binding.etHeight.text.toString().toFloatOrNull() ?: 842f

        runWithProgress(binding.progressBar) {
            val out = outputFile("cropped_${System.currentTimeMillis()}.pdf")
            PdfUtils.cropPdf(this, uri, out, Rectangle(x, y, width, height))
            out
        }
    }
}
