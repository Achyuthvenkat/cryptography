package com.pdftools.app.features.compare

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityComparePdfBinding
import com.pdftools.app.features.BasePdfActivity

/**
 * Compare PDF — loads two PDFs side-by-side and renders pages for visual
 * comparison. The activity uses Android's PdfRenderer to produce page bitmaps
 * and displays them in a split-view.
 */
class ComparePdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityComparePdfBinding
    private var pdfUri1: Uri? = null
    private var pdfUri2: Uri? = null
    private var currentPage = 0
    private var totalPages1 = 0
    private var totalPages2 = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComparePdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile1.setOnClickListener {
            // Launch picker — result handled by tag
            launchPdfPicker()
            pendingPicker = 1
        }
        binding.btnSelectFile2.setOnClickListener {
            launchPdfPicker()
            pendingPicker = 2
        }

        binding.btnPrevPage.setOnClickListener { navigatePage(-1) }
        binding.btnNextPage.setOnClickListener { navigatePage(1) }
        binding.btnCompare.setOnClickListener { loadCurrentPage() }
        updateNavButtons()
    }

    private var pendingPicker = 1

    override fun onPdfSelected(uri: Uri) {
        when (pendingPicker) {
            1 -> {
                pdfUri1 = uri
                binding.tvFile1Name.text = uri.lastPathSegment ?: getString(R.string.file_selected)
                totalPages1 = countPages(uri)
            }
            2 -> {
                pdfUri2 = uri
                binding.tvFile2Name.text = uri.lastPathSegment ?: getString(R.string.file_selected)
                totalPages2 = countPages(uri)
            }
        }
        binding.btnCompare.isEnabled = pdfUri1 != null && pdfUri2 != null
    }

    private fun countPages(uri: Uri): Int {
        return contentResolver.openInputStream(uri)?.use { stream ->
            PdfReader(stream).use { reader ->
                PdfDocument(reader).use { it.numberOfPages }
            }
        } ?: 0
    }

    private fun navigatePage(delta: Int) {
        val max = minOf(totalPages1, totalPages2).coerceAtLeast(1)
        currentPage = (currentPage + delta).coerceIn(0, max - 1)
        loadCurrentPage()
    }

    private fun loadCurrentPage() {
        val uri1 = pdfUri1 ?: return
        val uri2 = pdfUri2 ?: return

        binding.tvPageInfo.text = getString(
            R.string.page_info,
            currentPage + 1,
            minOf(totalPages1, totalPages2)
        )

        renderPage(uri1, currentPage)?.let { binding.pdfView1.setImageBitmap(it) }
        renderPage(uri2, currentPage)?.let { binding.pdfView2.setImageBitmap(it) }
        updateNavButtons()
    }

    private fun renderPage(uri: Uri, pageIndex: Int): android.graphics.Bitmap? {
        val pfd = contentResolver.openFileDescriptor(uri, "r") ?: return null
        return try {
            val renderer = android.graphics.pdf.PdfRenderer(pfd)
            if (pageIndex >= renderer.pageCount) return null
            val page = renderer.openPage(pageIndex)
            val bitmap = android.graphics.Bitmap.createBitmap(
                page.width * 2, page.height * 2,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            page.render(bitmap, null, null,
                android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()
            bitmap
        } finally {
            pfd.close()
        }
    }

    private fun updateNavButtons() {
        val max = minOf(totalPages1, totalPages2).coerceAtLeast(1)
        binding.btnPrevPage.isEnabled = currentPage > 0
        binding.btnNextPage.isEnabled = currentPage < max - 1
    }
}
