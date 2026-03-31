package com.pdftools.app.features.merge

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityMergePdfBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.utils.PdfUtils

/**
 * Merge PDF — combines multiple PDF files into one.
 */
class MergePdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityMergePdfBinding
    private val selectedFiles = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMergePdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnAddFiles.setOnClickListener { launchMultiPdfPicker() }
        binding.btnMerge.setOnClickListener { mergeFiles() }
        updateFileCount()
    }

    override fun onPdfsSelected(uris: List<Uri>) {
        selectedFiles.addAll(uris)
        updateFileCount()
    }

    private fun updateFileCount() {
        binding.tvFileCount.text = getString(R.string.files_selected, selectedFiles.size)
        binding.btnMerge.isEnabled = selectedFiles.size >= 2
    }

    private fun mergeFiles() {
        if (selectedFiles.size < 2) {
            showSnack(getString(R.string.error_select_at_least_two), binding.root)
            return
        }
        runWithProgress(binding.progressBar) {
            val out = outputFile("merged_${System.currentTimeMillis()}.pdf")
            PdfUtils.mergePdfs(this, selectedFiles, out)
            out
        }
    }
}
