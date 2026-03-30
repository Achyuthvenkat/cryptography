package com.pdftools.app.features.organize

import android.net.Uri
import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityOrganizePdfBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.utils.PdfUtils
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import java.util.Collections

/**
 * Organize PDF — reorder and delete pages in a PDF by dragging and dropping
 * page thumbnails in a RecyclerView.
 */
class OrganizePdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityOrganizePdfBinding
    private val pageOrder = mutableListOf<Int>() // 1-based page numbers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrganizePdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile.setOnClickListener { launchPdfPicker() }
        binding.btnApply.setOnClickListener { applyOrganization() }
        binding.btnApply.isEnabled = false

        setupDragAndDrop()
    }

    private fun setupDragAndDrop() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.START or ItemTouchHelper.END
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                Collections.swap(pageOrder, from, to)
                recyclerView.adapter?.notifyItemMoved(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                pageOrder.removeAt(pos)
                binding.pagesRecyclerView.adapter?.notifyItemRemoved(pos)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.pagesRecyclerView)
    }

    override fun onPdfSelected(uri: Uri) {
        binding.tvSelectedFile.text = uri.lastPathSegment ?: getString(R.string.file_selected)
        // Read page count and populate pageOrder
        pageOrder.clear()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            PdfReader(inputStream).use { reader ->
                PdfDocument(reader).use { pdfDoc ->
                    for (i in 1..pdfDoc.numberOfPages) pageOrder.add(i)
                }
            }
        }
        binding.tvPageCount.text = getString(R.string.page_count, pageOrder.size)
        binding.btnApply.isEnabled = pageOrder.isNotEmpty()
    }

    private fun applyOrganization() {
        val uri = selectedPdfUri ?: return
        if (pageOrder.isEmpty()) return
        runWithProgress(binding.progressBar) {
            val out = outputFile("organized_${System.currentTimeMillis()}.pdf")
            PdfUtils.organizePdf(this, uri, out, pageOrder)
            out
        }
    }
}
