package com.umc.hellodoctor.core.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File

class CameraController(
    private val fragment: Fragment,
    private val onImageCaptured: (Uri) -> Unit,
    private val onPermissionDenied: () -> Unit = {}
) {

    private var photoUri: Uri? = null

    private val takePictureLauncher =
        fragment.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { isSuccess ->
            if (isSuccess && photoUri != null) {
                onImageCaptured(photoUri!!)
            }
        }

    private val requestPermissionLauncher =
        fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                openCamera()
            } else {
                onPermissionDenied()
            }
        }

    fun checkPermissionAndOpenCamera() {
        val context = fragment.requireContext()
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val context = fragment.requireContext()

        val photoFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "photo_${System.currentTimeMillis()}.jpg"
        )
        photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )

        takePictureLauncher.launch(photoUri)
    }
}
