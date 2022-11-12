package com.example.myapplication.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Marker(
    val latitude: Double,
    val longitude: Double,
    val title: String?,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L
)