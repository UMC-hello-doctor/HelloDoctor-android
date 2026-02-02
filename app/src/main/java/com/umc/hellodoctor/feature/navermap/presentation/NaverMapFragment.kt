package com.umc.hellodoctor.feature.navermap.presentation


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.Marker
import com.umc.hellodoctor.R
import com.umc.hellodoctor.core.location.LocationMapViewModel
import com.umc.hellodoctor.databinding.FragmentNaverMapBinding
import com.umc.hellodoctor.feature.navermap.adapter.HospitalAdapter
import com.umc.hellodoctor.feature.navermap.data.HospitalItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@AndroidEntryPoint
class NaverMapFragment : Fragment(), OnMapReadyCallback {
    private val TAG = "NaverMapFragment"
    private var _binding: FragmentNaverMapBinding? = null
    private val binding get() = _binding!!

    private val locationViewModel: LocationMapViewModel by activityViewModels()
    private val hospitalViewModel: HospitalResultViewModel by viewModels()

    private lateinit var naverMap: NaverMap
    private lateinit var hospitalAdapter: HospitalAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNaverMapBinding.inflate(inflater, container, false)

        locationViewModel.loadCurrentLocationOnce()
        Log.i(TAG, "onCreateView: loadlocation")
        locationViewModel.startContinuousLocation()

        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also { fm.beginTransaction().add(R.id.map, it).commit() }
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupBottomSheetBehavior()
        observeHospitalData()
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        Log.i(TAG, "onMapReady: loadlocation")
        // 기존 위치 카메라
        locationViewModel.getLastLocation()?.let { (lat, lng) ->
            naverMap.moveCamera(CameraUpdate.scrollTo(LatLng(lat, lng)))
        }

        // LocationOverlay (기존)
        val locationOverlay = naverMap.locationOverlay.apply {
            subIcon = LocationOverlay.DEFAULT_SUB_ICON_ARROW
            circleOutlineWidth = 0
            isVisible = true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                locationViewModel.uiState.collect { state ->
                    state.currentLat?.let { lat ->
                        state.currentLng?.let { lng ->
                            locationOverlay.position = LatLng(lat, lng)
                        }
                    }
                    state.currentBearing?.let { locationOverlay::setBearing }
                }
            }
        }

        // 지도 클릭 → 내과 병원 검색
        naverMap.setOnMapClickListener { _, latLng ->
            hospitalViewModel.fetchNearbyHospitals(latLng.latitude, latLng.longitude)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupRecyclerView() {

            hospitalAdapter = HospitalAdapter(
                onCallClick = { tel ->
                    val uri = "tel:$tel".toUri()
                    val intent = Intent(Intent.ACTION_DIAL, uri)
                    context?.startActivity(intent)
                },
                onMarkerClick = { lat, lng ->
                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
                    naverMap.moveCamera(cameraUpdate)
                }
            )

            binding.hospitalRecyclerView.apply {
                adapter = hospitalAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            binding.hospitalRecyclerView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        binding.bottomSheetContainer.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        binding.bottomSheetContainer.requestDisallowInterceptTouchEvent(false)
                    }
                }
                v.performClick()  // 클릭 가능성 처리 → Lint 경고 사라짐
                false
            }
    }

    private fun setupBottomSheetBehavior() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetContainer)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    binding.bottomSheetContainer.visibility = View.GONE
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun observeHospitalData() {
        hospitalViewModel.hospitalList.observe(viewLifecycleOwner) { hospitals ->
            hospitalAdapter.submitList(hospitals)
            showHospitalsOnMap(hospitals)
        }
    }



    private val markerList = mutableListOf<Marker>()

    private fun showHospitalsOnMap(hospitals: List<HospitalItem>) {
        // 기존 마커 제거
        markerList.forEach { it.map = null }
        markerList.clear()
        Log.d(TAG, "showHospitalsOnMap: icon생성")
        // 새 마커 추가
        hospitals.forEach { hospital ->

            val marker = Marker().apply {
                position = LatLng(hospital.latitude, hospital.longitude)
                captionText = hospital.name
                map = naverMap
            }
            marker.setOnClickListener {
                val index = hospitals.indexOf(hospital)
                if (index != -1) {
                    val smoothScroller = object : LinearSmoothScroller(requireContext()) {
                        override fun getVerticalSnapPreference() = SNAP_TO_START
                    }.apply { targetPosition = index }
                    (binding.hospitalRecyclerView.layoutManager as? LinearLayoutManager)
                        ?.startSmoothScroll(smoothScroller)
                }
                true
            }
            marker
            markerList.add(marker)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        locationViewModel.stopContinuousLocation()
    }
}

