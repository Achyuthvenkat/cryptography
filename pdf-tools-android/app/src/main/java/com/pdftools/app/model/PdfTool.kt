package com.pdftools.app.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Represents a single PDF tool shown on the main dashboard.
 */
data class PdfTool(
    val id: ToolId,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val category: ToolCategory
)

enum class ToolCategory {
    ORGANIZE,
    CONVERT,
    EDIT,
    SECURITY,
    OPTIMIZE,
    OTHER
}

enum class ToolId {
    MERGE_PDF,
    SPLIT_PDF,
    COMPRESS_PDF,
    PDF_TO_WORD,
    PDF_TO_POWERPOINT,
    PDF_TO_EXCEL,
    WORD_TO_PDF,
    POWERPOINT_TO_PDF,
    EXCEL_TO_PDF,
    EDIT_PDF,
    PDF_TO_JPG,
    JPG_TO_PDF,
    SIGN_PDF,
    WATERMARK_PDF,
    ROTATE_PDF,
    HTML_TO_PDF,
    UNLOCK_PDF,
    PROTECT_PDF,
    ORGANIZE_PDF,
    PDF_TO_PDFA,
    REPAIR_PDF,
    PAGE_NUMBERS,
    SCAN_TO_PDF,
    OCR_PDF,
    COMPARE_PDF,
    REDACT_PDF,
    CROP_PDF
}
