package com.umc.hellodoctor.feature.navermap.presentation


import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.LocationOverlay
import com.umc.hellodoctor.R
import com.umc.hellodoctor.databinding.FragmentNaverMapBinding
import com.umc.hellodoctor.feature.location.LocationMapViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NaverMapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentNaverMapBinding? = null
    private val locationViewModel : LocationMapViewModel by activityViewModels()
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNaverMapBinding.inflate(inflater, container, false)
        locationViewModel.loadCurrentLocationOnce()
        locationViewModel.startContinuousLocation()
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        locationViewModel.stopContinuousLocation()
    }

    override fun onMapReady(naverMap: NaverMap) {

        locationViewModel.getLastLocation()?.let { (lastLat, lastLng) ->
            val lastLatLng = LatLng(lastLat, lastLng)
            naverMap.moveCamera(CameraUpdate.scrollTo(lastLatLng))
        }

        val locationOverlay = naverMap.locationOverlay.apply {
            subIcon = LocationOverlay.DEFAULT_SUB_ICON_ARROW
            circleOutlineWidth = 0
            isVisible = true
        }
        // uiState 구독해서 위치/각도 반영
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                locationViewModel.uiState.collect { state ->
                    val lat = state.currentLat
                    val lng = state.currentLng
                    if (lat != null && lng != null) {
                        locationOverlay.position = LatLng(lat, lng) // 위치 갱신[web:7]
                    }

                    state.currentBearing?.let { heading ->
                        locationOverlay.bearing = heading  // 각도 갱신[web:8]
                    }

                    // 원하면 카메라도 같이 움직이기
                    // if (lat != null && lng != null) {
                    //     val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
                    //     naverMap.moveCamera(cameraUpdate)
                    // }
                }
            }
        }
    }

}
