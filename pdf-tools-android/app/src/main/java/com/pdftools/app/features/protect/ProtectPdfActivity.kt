package com.pdftools.app.features.protect

import android.net.Uri
import android.os.Bundle
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityProtectPdfBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.utils.PdfUtils

/**
 * Protect PDF — encrypts a PDF with a user and owner password.
 */
class ProtectPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityProtectPdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProtectPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnProtect.setOnClickListener { protectPdf() }
        binding.btnProtect.isEnabled = false
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnProtect.isEnabled = true
    }

    private fun protectPdf() {
        val uri = selectedPdfUri ?: return
        val userPassword = binding.etUserPassword.text.toString()
        val ownerPassword = binding.etOwnerPassword.text.toString()
            .ifEmpty { userPassword + "_owner" }

        if (userPassword.isEmpty()) {
            showSnack(getString(R.string.error_password_required), binding.root)
            return
        }

        runWithProgress(binding.progressBar) {
            val out = outputFile("protected_${System.currentTimeMillis()}.pdf")
            PdfUtils.protectPdf(this, uri, out, userPassword, ownerPassword)
            out
        }
    }
}
