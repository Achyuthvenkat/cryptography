package com.pdftools.app.features.jpg

import android.net.Uri
import android.os.Bundle
import android.widget.RadioGroup
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityJpgToPdfBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.utils.PdfUtils

/**
 * JPG to PDF — converts one or multiple images to a single PDF.
 */
class JpgToPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityJpgToPdfBinding
    private val selectedImages = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJpgToPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnAddImages.setOnClickListener { launchMultiImagePicker() }
        binding.btnConvert.setOnClickListener { convert() }
        updateImageCount()
    }

    override fun onImagesSelected(uris: List<Uri>) {
        selectedImages.addAll(uris)
        updateImageCount()
    }

    private fun updateImageCount() {
        binding.tvImageCount.text = getString(R.string.images_selected, selectedImages.size)
        binding.btnConvert.isEnabled = selectedImages.isNotEmpty()
    }

    private fun convert() {
        if (selectedImages.isEmpty()) return
        runWithProgress(binding.progressBar) {
            val out = outputFile("images_${System.currentTimeMillis()}.pdf")
            PdfUtils.imagesToPdf(this, selectedImages, out)
            out
        }
    }
}
