package com.umc.hellodoctor.core.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import java.io.File

class CameraController(
    private val fragment: Fragment,
    private val onImageCaptured: (Uri) -> Unit,
    private val onPermissionDenied: () -> Unit = {},
    private val onPermissionGranted: () -> Unit = {},
) {
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val requestPermissionLauncher =
        fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (granted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }

    fun checkPermissionAndOpenCamera() {
        val context = fragment.requireContext()
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionGranted()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
    ) {
        val context = fragment.requireContext()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener(
            {
                try {
                    cameraProvider = cameraProviderFuture.get()

                    // Unbind any previously bound camera
                    cameraProvider?.unbindAll()

                    // Create Preview use case
                    val preview =
                        Preview.Builder().build().apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }

                    // Create ImageCapture use case with quality priority
                    imageCapture =
                        ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build()

                    // Select back camera
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    // Bind use cases to camera
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture,
                    )
                } catch (
                    @Suppress("TooGenericExceptionCaught", "SwallowedException")
                    e: IllegalArgumentException,
                ) {
                    // Camera binding failed due to invalid lifecycle owner
                } catch (
                    @Suppress("TooGenericExceptionCaught", "SwallowedException")
                    e: IllegalStateException,
                ) {
                    // Camera binding failed due to invalid state
                } catch (
                    @Suppress("TooGenericExceptionCaught", "SwallowedException")
                    e: RuntimeException,
                ) {
                    // Camera binding failed due to runtime error
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile =
            File(
                fragment.requireContext().cacheDir,
                "photo_${System.currentTimeMillis()}.jpg",
            )

        val outputOptions =
            ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(fragment.requireContext()),
            @Suppress("ObjectLiteralToLambda")
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    onImageCaptured(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    // Image capture failed
                }
            },
        )
    }

    fun unbindCamera() {
        cameraProvider?.unbindAll()
    }
}
