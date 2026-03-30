package com.pdftools.app.features

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.pdftools.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Base class for all feature activities. Provides common patterns:
 * - File picker launchers
 * - Progress overlay helpers
 * - Share / save result helpers
 */
abstract class BasePdfActivity : AppCompatActivity() {

    protected var selectedPdfUri: Uri? = null
    protected var selectedPdfUris: MutableList<Uri> = mutableListOf()
    protected var selectedImageUri: Uri? = null
    protected var selectedImageUris: MutableList<Uri> = mutableListOf()

    // Single-PDF picker
    protected lateinit var pickPdfLauncher: ActivityResultLauncher<Intent>
    // Multi-PDF picker
    protected lateinit var pickMultiPdfLauncher: ActivityResultLauncher<Intent>
    // Single-image picker
    protected lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    // Multi-image picker
    protected lateinit var pickMultiImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickPdfLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedPdfUri = uri
                    onPdfSelected(uri)
                }
            }
        }

        pickMultiPdfLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                selectedPdfUris.clear()
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        selectedPdfUris.add(clipData.getItemAt(i).uri)
                    }
                } else {
                    data.data?.let { selectedPdfUris.add(it) }
                }
                onPdfsSelected(selectedPdfUris)
            }
        }

        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    onImageSelected(uri)
                }
            }
        }

        pickMultiImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                selectedImageUris.clear()
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        selectedImageUris.add(clipData.getItemAt(i).uri)
                    }
                } else {
                    data.data?.let { selectedImageUris.add(it) }
                }
                onImagesSelected(selectedImageUris)
            }
        }
    }

    // Subclasses override these to react to selections
    protected open fun onPdfSelected(uri: Uri) {}
    protected open fun onPdfsSelected(uris: List<Uri>) {}
    protected open fun onImageSelected(uri: Uri) {}
    protected open fun onImagesSelected(uris: List<Uri>) {}

    // -------------------------------------------------------------------------
    // Convenience launchers
    // -------------------------------------------------------------------------

    protected fun launchPdfPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        pickPdfLauncher.launch(intent)
    }

    protected fun launchMultiPdfPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickMultiPdfLauncher.launch(intent)
    }

    protected fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    protected fun launchMultiImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickMultiImageLauncher.launch(intent)
    }

    // -------------------------------------------------------------------------
    // Processing helper
    // -------------------------------------------------------------------------

    /**
     * Runs [block] on the IO dispatcher while showing a loading overlay, then
     * calls [onSuccess] or [onError] on the main thread.
     */
    protected fun runWithProgress(
        progressView: View,
        onSuccess: (File) -> Unit = { shareFile(it) },
        onError: (Exception) -> Unit = { showError(it) },
        block: suspend () -> File
    ) {
        progressView.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { block() }
                progressView.visibility = View.GONE
                onSuccess(result)
            } catch (e: Exception) {
                progressView.visibility = View.GONE
                onError(e)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Share / toast helpers
    // -------------------------------------------------------------------------

    protected fun shareFile(file: File) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_file)))
    }

    protected fun showError(e: Exception) {
        Toast.makeText(this, getString(R.string.error_processing, e.message), Toast.LENGTH_LONG).show()
    }

    protected fun showSnack(message: String, rootView: View) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }

    protected fun outputFile(name: String): File =
        File(getExternalFilesDir(null), name)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
