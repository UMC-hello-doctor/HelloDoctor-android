package com.umc.hellodoctor.feature.navermap.presentation


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.LocationOverlay
import com.umc.hellodoctor.R
import com.umc.hellodoctor.core.location.LocationMapViewModel
import com.umc.hellodoctor.databinding.FragmentNaverMapBinding
import com.umc.hellodoctor.feature.chat.presentation.ChatViewModel
import com.umc.hellodoctor.feature.navermap.adapter.HospitalAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NaverMapFragment : Fragment(), OnMapReadyCallback {
    private val TAG = "NaverMapFragment"
    private var _binding: FragmentNaverMapBinding? = null
    private val binding get() = _binding!!

    private val locationViewModel: LocationMapViewModel by activityViewModels()
    private val hospitalViewModel: HospitalResultViewModel by viewModels()
    private val chatViewModel: ChatViewModel by activityViewModels()

    private lateinit var naverMap: NaverMap
    private lateinit var hospitalAdapter: HospitalAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var hasPerformedInitialSearch = false
    private var hasMovedCameraToInitialLocation = false

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

        // 현재 진료과 타이틀 설정
        val currentDept = chatViewModel.currentDepartment.value
        Log.d(TAG, "========== onViewCreated 진료과 확인 ==========")
        Log.d(TAG, "currentDepartment.value: $currentDept")
        Log.d(TAG, "진료과 null 여부: ${currentDept.isNullOrEmpty()}")
        binding.departmentTitle.text = if (currentDept.isNullOrEmpty()) "추천 진료과" else currentDept

        observeHospitalData()
        observeLocationForInitialSearch()
        observeLocationForInitialCamera()
    }

    /**
     * 위치 정보 수신 후 첫 병원 검색 실행
     */
    private fun observeLocationForInitialSearch() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                locationViewModel.uiState.collect { state ->
                    if (!hasPerformedInitialSearch) {
                        state.currentLat?.let { lat ->
                            state.currentLng?.let { lng ->
                                Log.d(TAG, "========== 위치 정보 수신 감지 (병원 검색) ==========")
                                Log.d(TAG, "수신 위치: lat=$lat, lng=$lng")
                                Log.d(TAG, "첫 번째 병원 검색 실행")
                                hasPerformedInitialSearch = true
                                searchHospitalsAtCurrentLocation()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 위치 정보와 지도 초기화 후 첫 카메라 이동
     */
    private fun observeLocationForInitialCamera() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                locationViewModel.uiState.collect { state ->
                    if (!hasMovedCameraToInitialLocation && ::naverMap.isInitialized) {
                        state.currentLat?.let { lat ->
                            state.currentLng?.let { lng ->
                                Log.d(TAG, "========== 위치 정보 수신 감지 (카메라 이동) ==========")
                                Log.d(TAG, "수신 위치: lat=$lat, lng=$lng")
                                Log.d(TAG, "첫 번째 카메라 이동 실행")
                                naverMap.moveCamera(CameraUpdate.scrollTo(LatLng(lat, lng)))
                                hasMovedCameraToInitialLocation = true
                                Log.d(TAG, "카메라 이동 완료")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        Log.i(TAG, "onMapReady: map 초기화 완료")

        // LocationOverlay (기존)
        val locationOverlay = naverMap.locationOverlay.apply {
            subIcon = LocationOverlay.DEFAULT_SUB_ICON_ARROW
            circleOutlineWidth = 0
            isVisible = true
        }

        // 위치 업데이트에 따른 오버레이 업데이트
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                locationViewModel.uiState.collect { state ->
                    state.currentLat?.let { lat ->
                        state.currentLng?.let { lng ->
                            locationOverlay.position = LatLng(lat, lng)
                        }
                    }
                    state.currentBearing?.let { bearing ->
                        locationOverlay.bearing = bearing
                    }
                }
            }
        }

        // 지도 클릭 → 해당 위치의 병원 검색
        naverMap.setOnMapClickListener { _, latLng ->
            val department = chatViewModel.currentDepartment.value ?: "내과"
            Log.d(TAG, "========== 지도 클릭 병원 검색 ==========")
            Log.d(TAG, "클릭 위치: lat=${latLng.latitude}, lng=${latLng.longitude}")
            Log.d(TAG, "currentDepartment.value: ${chatViewModel.currentDepartment.value}")
            Log.d(TAG, "사용할 진료과: $department (null 처리 적용: ${chatViewModel.currentDepartment.value == null})")
            hospitalViewModel.fetchNearbyHospitals(latLng.latitude, latLng.longitude, department)
            showBottomSheet()
        }
    }

    private fun setupRecyclerView() {
        hospitalAdapter = HospitalAdapter()
        binding.hospitalRecyclerView.apply {
            adapter = hospitalAdapter
            layoutManager = LinearLayoutManager(requireContext())
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
            binding.loadingProgressBar.visibility = View.GONE
        }

        hospitalViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ApiState.Loading -> {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                }
                is ApiState.Success -> {
                    binding.loadingProgressBar.visibility = View.GONE
                }
                is ApiState.Error -> {
                    binding.loadingProgressBar.visibility = View.GONE
                    Log.e(TAG, "Error: ${state.message}")
                }
            }
        }
    }

    private fun showBottomSheet() {
        binding.bottomSheetContainer.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    /**
     * 현재 위치에서 병원 검색
     */
    private fun searchHospitalsAtCurrentLocation() {
        locationViewModel.getLastLocation()?.let { (lat, lng) ->
            val department = chatViewModel.currentDepartment.value ?: "내과"
            Log.d(TAG, "========== 현재 위치 기반 병원 검색 ==========")
            Log.d(TAG, "현재 위치: lat=$lat, lng=$lng")
            Log.d(TAG, "currentDepartment.value: ${chatViewModel.currentDepartment.value}")
            Log.d(TAG, "사용할 진료과: $department (null 처리 적용: ${chatViewModel.currentDepartment.value == null})")
            Log.d(TAG, "API 호출 준비 완료")
            hospitalViewModel.fetchNearbyHospitals(lat, lng, department)
            showBottomSheet()
        } ?: run {
            Log.w(TAG, "========== 현재 위치 없음 ==========")
            Log.w(TAG, "현재 위치를 가져올 수 없습니다")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        locationViewModel.stopContinuousLocation()
    }
}