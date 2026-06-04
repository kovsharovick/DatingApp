package com.example.androiddatingapp.ui.components

import android.content.Context
import coil.ImageLoader
import com.example.androiddatingapp.data.api.ApiClient

object AppImageLoader {

    @Volatile
    private var instance: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        return instance ?: synchronized(this) {
            instance ?: ImageLoader.Builder(context.applicationContext)
                .okHttpClient(ApiClient.mediaHttpClient)
                .crossfade(true)
                .build()
                .also { instance = it }
        }
    }

    fun install(context: Context) {
        coil.Coil.setImageLoader(get(context))
    }
}
