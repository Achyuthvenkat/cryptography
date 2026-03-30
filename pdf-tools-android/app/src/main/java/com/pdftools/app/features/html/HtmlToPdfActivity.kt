package com.pdftools.app.features.html

import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityHtmlToPdfBinding
import com.pdftools.app.features.BasePdfActivity

/**
 * HTML to PDF — loads a URL in a WebView and prints it to PDF using Android's
 * built-in print framework.
 */
class HtmlToPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityHtmlToPdfBinding
    private var pageLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHtmlToPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    pageLoaded = true
                    binding.btnConvert.isEnabled = true
                }
            }
        }

        binding.btnLoadUrl.setOnClickListener {
            val url = binding.etUrl.text.toString().trim()
            if (url.isEmpty()) return@setOnClickListener
            val fullUrl = if (url.startsWith("http")) url else "https://$url"
            binding.webView.loadUrl(fullUrl)
            binding.btnConvert.isEnabled = false
            pageLoaded = false
        }

        binding.btnConvert.setOnClickListener { printToPdf() }
        binding.btnConvert.isEnabled = false
    }

    private fun printToPdf() {
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        val jobName = "HTML_to_PDF_${System.currentTimeMillis()}"
        val printAdapter = binding.webView.createPrintDocumentAdapter(jobName)
        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()
        printManager.print(jobName, printAdapter, printAttributes)
    }
}
