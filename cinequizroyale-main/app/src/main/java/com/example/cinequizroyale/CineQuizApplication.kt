package com.example.cinequizroyale

import android.app.Application

class CineQuizApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize the CloudStorageProvider with application context
        CloudStorageProvider.initialize(this)
    }
}