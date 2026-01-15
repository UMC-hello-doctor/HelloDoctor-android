package com.umc.hellodoctor.app

import android.app.Application
import com.naver.maps.map.NaverMapSdk
import com.umc.hellodoctor.BuildConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HelloDoctorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NcpKeyClient(BuildConfig.NAVER_MAP_CLIENT_ID)
    }
}