package com.pdftools.app.features.jpg

import android.net.Uri
import android.os.Bundle
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityPdfToJpgBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.features.convert.ConversionUtils
import java.io.File

/**
 * PDF to JPG — renders each PDF page as a JPEG image.
 */
class PdfToJpgActivity : BasePdfActivity() {

    private lateinit var binding: ActivityPdfToJpgBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfToJpgBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnConvert.setOnClickListener { convertToJpg() }
        binding.btnConvert.isEnabled = false
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnConvert.isEnabled = true
    }

    private fun convertToJpg() {
        val uri = selectedPdfUri ?: return
        runWithProgress(
            binding.progressBar,
            onSuccess = { dir ->
                val files = dir.listFiles() ?: emptyArray()
                showSnack(getString(R.string.jpg_export_success, files.size), binding.root)
            }
        ) {
            val outDir = File(getExternalFilesDir(null), "pdf_pages_${System.currentTimeMillis()}")
            outDir.mkdirs()
            ConversionUtils.pdfToJpg(this, uri, outDir)
            outDir
        }
    }
}
