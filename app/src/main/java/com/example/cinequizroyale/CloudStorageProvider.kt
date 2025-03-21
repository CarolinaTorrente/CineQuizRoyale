package com.example.cinequizroyale

import android.content.Context

// Singleton object to provide application context
object CloudStorageProvider {
    lateinit var context: Context
        private set

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }
}