package com.umc.hellodoctor.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HelloDoctorApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}