package com.pdftools.app.features.watermark

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityWatermarkPdfBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.utils.PdfUtils

/**
 * Watermark PDF — stamp text or an image watermark over a PDF.
 */
class WatermarkPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityWatermarkPdfBinding
    private var watermarkImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWatermarkPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnApply.setOnClickListener { applyWatermark() }
        binding.btnApply.isEnabled = false

        binding.radioText.setOnCheckedChangeListener { _, isChecked ->
            binding.textWatermarkGroup.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        binding.radioImage.setOnCheckedChangeListener { _, isChecked ->
            binding.imageWatermarkGroup.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.btnSelectImage.setOnClickListener { launchImagePicker() }
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnApply.isEnabled = true
    }

    override fun onImageSelected(uri: Uri) {
        watermarkImageUri = uri
        binding.tvSelectedImage.text = uri.lastPathSegment ?: getString(R.string.image_selected)
    }

    private fun applyWatermark() {
        val uri = selectedPdfUri ?: return
        val opacity = (binding.sliderOpacity.value / 100f).coerceIn(0.01f, 1f)
        val fontSize = binding.etFontSize.text.toString().toFloatOrNull() ?: 52f

        runWithProgress(binding.progressBar) {
            val out = outputFile("watermarked_${System.currentTimeMillis()}.pdf")
            if (binding.radioText.isChecked) {
                val text = binding.etWatermarkText.text.toString().ifEmpty { "CONFIDENTIAL" }
                PdfUtils.addTextWatermark(this, uri, out, text, opacity, fontSize)
            } else {
                val imgUri = watermarkImageUri
                    ?: throw IllegalStateException(getString(R.string.error_no_image_selected))
                PdfUtils.addImageWatermark(this, uri, out, imgUri, opacity)
            }
            out
        }
    }
}
