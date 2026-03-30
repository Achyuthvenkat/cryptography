package com.pdftools.app.features.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.pdftools.app.R
import com.pdftools.app.databinding.ActivityScanToPdfBinding
import com.pdftools.app.features.BasePdfActivity
import com.pdftools.app.utils.PdfUtils
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Scan to PDF — uses the device camera to capture document scans and combines
 * them into a PDF.
 */
class ScanToPdfActivity : BasePdfActivity() {

    private lateinit var binding: ActivityScanToPdfBinding
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private val capturedImages = mutableListOf<File>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanToPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnCapture.setOnClickListener { captureImage() }
        binding.btnCreatePdf.setOnClickListener { createPdfFromScans() }
        binding.btnCreatePdf.isEnabled = false

        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> startCamera()
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .setTargetResolution(Size(1280, 720))
                .build()
                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_STILL)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA,
                preview, imageCapture)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        val capture = imageCapture ?: return
        val photoFile = File(
            getExternalFilesDir(null),
            "scan_${System.currentTimeMillis()}.jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        capture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturedImages.add(photoFile)
                    val count = capturedImages.size
                    binding.tvScanCount.text = getString(R.string.scans_captured, count)
                    binding.btnCreatePdf.isEnabled = true
                    showSnack(getString(R.string.scan_captured, count), binding.root)
                }
                override fun onError(exc: ImageCaptureException) {
                    showError(exc)
                }
            }
        )
    }

    private fun createPdfFromScans() {
        if (capturedImages.isEmpty()) return
        runWithProgress(binding.progressBar) {
            val uris = capturedImages.map { android.net.Uri.fromFile(it) }
            val out = outputFile("scanned_${System.currentTimeMillis()}.pdf")
            PdfUtils.imagesToPdf(this, uris, out)
            out
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
