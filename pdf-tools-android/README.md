# PDF Tools — Android Application

A fully-featured Android PDF toolkit providing 27 PDF operations in a single app.

## Features

| Feature | Description |
|---|---|
| **Merge PDF** | Combine multiple PDFs into one in any order |
| **Split PDF** | Separate pages into individual files or custom page ranges |
| **Compress PDF** | Reduce file size while maintaining quality |
| **PDF → Word** | Convert PDF to DOC/DOCX (via API integration) |
| **PDF → PowerPoint** | Convert PDF to PPT/PPTX (via API integration) |
| **PDF → Excel** | Extract tables from PDF to XLSX (via API integration) |
| **Word → PDF** | Convert DOC/DOCX to PDF |
| **PowerPoint → PDF** | Convert PPT/PPTX to PDF |
| **Excel → PDF** | Convert XLS/XLSX to PDF |
| **Edit PDF** | Add text annotations and freehand drawings |
| **PDF → JPG** | Render each page as a JPEG image |
| **JPG → PDF** | Combine images into a PDF |
| **Sign PDF** | Draw and stamp a handwritten signature |
| **Watermark PDF** | Apply text or image watermarks |
| **Rotate PDF** | Rotate pages 90°, 180°, or 270° |
| **HTML → PDF** | Print any webpage to PDF via the Android print framework |
| **Unlock PDF** | Remove password protection |
| **Protect PDF** | Encrypt with AES-256 password |
| **Organize PDF** | Drag-and-drop page reordering; swipe to delete |
| **PDF → PDF/A** | Convert to long-term archiving format |
| **Repair PDF** | Recover data from corrupted PDFs |
| **Page Numbers** | Add page numbers at any position |
| **Scan → PDF** | Capture documents with the camera |
| **OCR PDF** | Make scanned PDFs searchable using ML Kit |
| **Compare PDF** | Side-by-side visual comparison of two PDFs |
| **Redact PDF** | Permanently remove sensitive text/regions |
| **Crop PDF** | Set custom crop boxes on every page |

## Architecture

```
app/src/main/java/com/pdftools/app/
├── MainActivity.kt               – Dashboard with search & tool grid
├── PdfToolsRepository.kt         – Central list of all tools
├── adapter/
│   └── PdfToolsAdapter.kt        – RecyclerView adapter
├── model/
│   └── PdfTool.kt                – Data model for each tool
├── utils/
│   └── PdfUtils.kt               – Core PDF operations (iText7)
└── features/
    ├── BasePdfActivity.kt         – Shared base class (file pickers, progress, share)
    ├── merge/      MergePdfActivity
    ├── split/      SplitPdfActivity
    ├── compress/   CompressPdfActivity
    ├── convert/    PdfToWordActivity, PdfToPowerPointActivity, PdfToExcelActivity,
    │               WordToPdfActivity, PowerPointToPdfActivity, ExcelToPdfActivity,
    │               ConversionUtils
    ├── edit/       EditPdfActivity
    ├── jpg/        PdfToJpgActivity, JpgToPdfActivity
    ├── sign/       SignPdfActivity
    ├── watermark/  WatermarkPdfActivity
    ├── rotate/     RotatePdfActivity
    ├── html/       HtmlToPdfActivity
    ├── unlock/     UnlockPdfActivity
    ├── protect/    ProtectPdfActivity
    ├── organize/   OrganizePdfActivity
    ├── pdfa/       PdfToPdfAActivity
    ├── repair/     RepairPdfActivity
    ├── pagenumbers/ PageNumbersActivity
    ├── scan/       ScanToPdfActivity
    ├── ocr/        OcrPdfActivity
    ├── compare/    ComparePdfActivity
    ├── redact/     RedactPdfActivity
    └── crop/       CropPdfActivity
```

## Key Libraries

| Library | Purpose |
|---|---|
| **iText7 Core 7.2.5** | Merge, split, compress, rotate, watermark, protect, unlock, page numbers, crop, organize, repair |
| **iText Cleanup 3.0.1** | Redact text and rectangular regions |
| **iText PDF/A** | PDF/A-1b conversion |
| **CameraX** | Scan-to-PDF camera capture |
| **ML Kit Text Recognition** | OCR of scanned PDFs |
| **Android PdfRenderer** | PDF-to-JPG page rendering |
| **Android Print Framework** | HTML-to-PDF via WebView |
| **Material Components** | UI components |

## Requirements

- Android 8.0 (API 26) or higher
- Camera permission (for Scan to PDF)
- Storage permissions (for reading/writing PDF files)
- Internet permission (for HTML to PDF URL loading)

## Building

```bash
cd pdf-tools-android
./gradlew assembleDebug
```

Install on a connected device:
```bash
./gradlew installDebug
```

## Notes on Office Conversions

PDF ↔ Office (Word/PowerPoint/Excel) conversions require either:
- A server-side API (iLovePDF, Smallpdf, Adobe PDF Services, Aspose Cloud)
- A commercial Android library (Aspose.Words for Android)

The current implementation shows a placeholder PDF when triggered. Replace the
stub functions in `ConversionUtils.kt` with your preferred API integration.

## PDF/A Conversion

For full PDF/A-1b compliance, provide an ICC colour profile (`srgb.icc`) in
`app/src/main/assets/`. A free sRGB profile can be downloaded from the
[ICC website](https://www.color.org/srgbprofiles.xalter).
