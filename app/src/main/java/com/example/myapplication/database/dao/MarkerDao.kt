package com.example.myapplication.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.database.model.Marker

@Dao
interface MarkerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMarker(marker: Marker)

    @Query("SELECT * FROM MARKER")
    fun getAllMarkers(): LiveData<List<Marker>>

    @Query("DELETE FROM MARKER WHERE latitude=:latitude AND longitude=:longitude")
    suspend fun deleteMarker(latitude: Double, longitude: Double)
}