package com.pdftools.app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.pdftools.app.adapter.PdfToolsAdapter
import com.pdftools.app.databinding.ActivityMainBinding
import com.pdftools.app.features.compress.CompressPdfActivity
import com.pdftools.app.features.compare.ComparePdfActivity
import com.pdftools.app.features.convert.*
import com.pdftools.app.features.crop.CropPdfActivity
import com.pdftools.app.features.edit.EditPdfActivity
import com.pdftools.app.features.html.HtmlToPdfActivity
import com.pdftools.app.features.jpg.JpgToPdfActivity
import com.pdftools.app.features.jpg.PdfToJpgActivity
import com.pdftools.app.features.merge.MergePdfActivity
import com.pdftools.app.features.ocr.OcrPdfActivity
import com.pdftools.app.features.organize.OrganizePdfActivity
import com.pdftools.app.features.pagenumbers.PageNumbersActivity
import com.pdftools.app.features.pdfa.PdfToPdfAActivity
import com.pdftools.app.features.protect.ProtectPdfActivity
import com.pdftools.app.features.redact.RedactPdfActivity
import com.pdftools.app.features.repair.RepairPdfActivity
import com.pdftools.app.features.rotate.RotatePdfActivity
import com.pdftools.app.features.scan.ScanToPdfActivity
import com.pdftools.app.features.sign.SignPdfActivity
import com.pdftools.app.features.split.SplitPdfActivity
import com.pdftools.app.features.unlock.UnlockPdfActivity
import com.pdftools.app.features.watermark.WatermarkPdfActivity
import com.pdftools.app.model.PdfTool
import com.pdftools.app.model.ToolId

/**
 * Main dashboard activity — shows the full list of PDF tools and allows filtering.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PdfToolsAdapter
    private var allTools: List<PdfTool> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        allTools = PdfToolsRepository.getAllTools(this)
        adapter = PdfToolsAdapter { tool -> openTool(tool) }

        binding.recyclerView.adapter = adapter
        adapter.submitList(allTools)

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim().orEmpty()
                val filtered = if (query.isEmpty()) allTools
                else allTools.filter { tool ->
                    getString(tool.titleRes).contains(query, ignoreCase = true) ||
                        getString(tool.descriptionRes).contains(query, ignoreCase = true)
                }
                adapter.submitList(filtered)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })
    }

    private fun openTool(tool: PdfTool) {
        val targetClass = when (tool.id) {
            ToolId.MERGE_PDF          -> MergePdfActivity::class.java
            ToolId.SPLIT_PDF          -> SplitPdfActivity::class.java
            ToolId.COMPRESS_PDF       -> CompressPdfActivity::class.java
            ToolId.PDF_TO_WORD        -> PdfToWordActivity::class.java
            ToolId.PDF_TO_POWERPOINT  -> PdfToPowerPointActivity::class.java
            ToolId.PDF_TO_EXCEL       -> PdfToExcelActivity::class.java
            ToolId.WORD_TO_PDF        -> WordToPdfActivity::class.java
            ToolId.POWERPOINT_TO_PDF  -> PowerPointToPdfActivity::class.java
            ToolId.EXCEL_TO_PDF       -> ExcelToPdfActivity::class.java
            ToolId.EDIT_PDF           -> EditPdfActivity::class.java
            ToolId.PDF_TO_JPG         -> PdfToJpgActivity::class.java
            ToolId.JPG_TO_PDF         -> JpgToPdfActivity::class.java
            ToolId.SIGN_PDF           -> SignPdfActivity::class.java
            ToolId.WATERMARK_PDF      -> WatermarkPdfActivity::class.java
            ToolId.ROTATE_PDF         -> RotatePdfActivity::class.java
            ToolId.HTML_TO_PDF        -> HtmlToPdfActivity::class.java
            ToolId.UNLOCK_PDF         -> UnlockPdfActivity::class.java
            ToolId.PROTECT_PDF        -> ProtectPdfActivity::class.java
            ToolId.ORGANIZE_PDF       -> OrganizePdfActivity::class.java
            ToolId.PDF_TO_PDFA        -> PdfToPdfAActivity::class.java
            ToolId.REPAIR_PDF         -> RepairPdfActivity::class.java
            ToolId.PAGE_NUMBERS       -> PageNumbersActivity::class.java
            ToolId.SCAN_TO_PDF        -> ScanToPdfActivity::class.java
            ToolId.OCR_PDF            -> OcrPdfActivity::class.java
            ToolId.COMPARE_PDF        -> ComparePdfActivity::class.java
            ToolId.REDACT_PDF         -> RedactPdfActivity::class.java
            ToolId.CROP_PDF           -> CropPdfActivity::class.java
        }
        startActivity(Intent(this, targetClass))
    }
}
