package com.hiddengems

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HiddenGemsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Application initialization
    }
}
