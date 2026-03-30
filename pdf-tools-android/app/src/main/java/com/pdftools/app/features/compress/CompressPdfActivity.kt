package com.pdftools.app.features.compress

import android.net.Uri
import android.os.Bundle
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityCompressPdfBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.utils.PdfUtils

/**
 * Compress PDF — reduces PDF file size while maintaining quality.
 */
class CompressPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityCompressPdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompressPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnCompress.setOnClickListener { compressFile() }
        binding.btnCompress.isEnabled = false
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)

        // Show original file size
        contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            val sizeKb = pfd.statSize / 1024
            binding.tvOriginalSize.text = getString(R.string.original_size, sizeKb)
        }
        binding.btnCompress.isEnabled = true
    }

    private fun compressFile() {
        val uri = selectedPdfUri ?: return
        runWithProgress(binding.progressBar) {
            val out = outputFile("compressed_${System.currentTimeMillis()}.pdf")
            PdfUtils.compressPdf(this, uri, out)
            out
        }
    }
}
