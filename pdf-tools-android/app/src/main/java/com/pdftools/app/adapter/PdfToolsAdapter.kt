package com.pdftools.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pdftools.app.databinding.ItemPdfToolBinding
import com.pdftools.app.model.PdfTool

/**
 * RecyclerView adapter that displays all PDF tool cards on the main dashboard.
 */
class PdfToolsAdapter(
    private val onToolClick: (PdfTool) -> Unit
) : ListAdapter<PdfTool, PdfToolsAdapter.PdfToolViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfToolViewHolder {
        val binding = ItemPdfToolBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PdfToolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PdfToolViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PdfToolViewHolder(
        private val binding: ItemPdfToolBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tool: PdfTool) {
            binding.toolTitle.setText(tool.titleRes)
            binding.toolDescription.setText(tool.descriptionRes)
            binding.toolIcon.setImageResource(tool.iconRes)
            binding.root.setOnClickListener { onToolClick(tool) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<PdfTool>() {
        override fun areItemsTheSame(oldItem: PdfTool, newItem: PdfTool) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PdfTool, newItem: PdfTool) =
            oldItem == newItem
    }
}
