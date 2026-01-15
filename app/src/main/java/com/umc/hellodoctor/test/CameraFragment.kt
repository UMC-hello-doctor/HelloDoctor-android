// CameraFragment.kt
package com.umc.hellodoctor.test

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.umc.hellodoctor.core.camera.CameraController
import com.umc.hellodoctor.databinding.FragmentCameraBinding

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraController: CameraController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraController = CameraController(
            fragment = this,
            onImageCaptured = { uri: Uri ->
                // 찍은 사진을 ImageView에 표시
                Glide.with(this)
                    .load(uri)
                    .into(binding.imagePreview)
            },
            onPermissionDenied = {
                // 권한 거부 시 처리 (토스트, 다이얼로그 등)
                // Toast.makeText(requireContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        )

        binding.btnTakePicture.setOnClickListener {
            cameraController.checkPermissionAndOpenCamera()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
