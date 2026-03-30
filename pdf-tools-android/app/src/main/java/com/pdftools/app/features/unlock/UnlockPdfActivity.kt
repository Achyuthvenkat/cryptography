package com.pdftools.app.features.unlock

import android.net.Uri
import android.os.Bundle
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityUnlockPdfBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.utils.PdfUtils

/**
 * Unlock PDF — removes password protection from a PDF.
 */
class UnlockPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityUnlockPdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnlockPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnUnlock.setOnClickListener { unlockPdf() }
        binding.btnUnlock.isEnabled = false
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnUnlock.isEnabled = true
    }

    private fun unlockPdf() {
        val uri = selectedPdfUri ?: return
        val password = binding.etPassword.text.toString()
        runWithProgress(binding.progressBar) {
            val out = outputFile("unlocked_${System.currentTimeMillis()}.pdf")
            PdfUtils.unlockPdf(this, uri, out, password)
            out
        }
    }
}
