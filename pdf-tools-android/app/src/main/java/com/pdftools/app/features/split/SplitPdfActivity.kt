package com.pdftools.app.features.split

import android.net.Uri
import android.os.Bundle
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivitySplitPdfBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.utils.PdfUtils
import java.io.File

/**
 * Split PDF — separates pages of a PDF into individual files or by page ranges.
 */
class SplitPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivitySplitPdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplitPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnSplit.setOnClickListener { splitFile() }
        binding.btnSplit.isEnabled = false
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        binding.btnSplit.isEnabled = true
    }

    private fun splitFile() {
        val uri = selectedPdfUri ?: return
        val rangeText = binding.etPageRanges.text.toString().trim()

        runWithProgress(binding.progressBar,
            onSuccess = { outDir ->
                showSnack(getString(R.string.split_success, outDir.name), binding.root)
            }
        ) {
            val outDir = File(getExternalFilesDir(null), "split_${System.currentTimeMillis()}")
            outDir.mkdirs()

            if (rangeText.isEmpty()) {
                // Split every page into its own file
                PdfUtils.splitPdf(this, uri, outDir)
            } else {
                // Parse ranges like "1-3,5,7-9"
                val ranges = parseRanges(rangeText)
                PdfUtils.splitPdfByRanges(this, uri, ranges, outDir)
            }
            outDir
        }
    }

    /**
     * Parses a range string such as "1-3,5,7-9" into a list of (start, end) pairs.
     */
    private fun parseRanges(text: String): List<Pair<Int, Int>> {
        return text.split(",").mapNotNull { token ->
            val parts = token.trim().split("-")
            when {
                parts.size == 2 -> {
                    val from = parts[0].trim().toIntOrNull()
                    val to = parts[1].trim().toIntOrNull()
                    if (from != null && to != null) Pair(from, to) else null
                }
                parts.size == 1 -> {
                    val page = parts[0].trim().toIntOrNull()
                    if (page != null) Pair(page, page) else null
                }
                else -> null
            }
        }
    }
}
