package com.example.myapplication.repository

import android.content.Context
import com.example.myapplication.database.AppDatabase
import com.example.myapplication.database.model.Marker

class MainRepository constructor(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context)
) {
    suspend fun saveMarker(marker: Marker) {
        database.getMarkerDao().saveMarker(marker)
    }

    suspend fun deleteMarker(latitude: Double, longitude: Double) {
        database.getMarkerDao().deleteMarker(latitude, longitude)
    }

    fun getAllMarkers() = database.getMarkerDao().getAllMarkers()
}