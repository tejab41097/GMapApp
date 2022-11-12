package com.example.myapplication.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.database.model.Marker
import com.example.myapplication.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val mainRepository: MainRepository = MainRepository(app)

    val selectedMarker = MutableLiveData<Marker?>()
    val showMarker = MutableLiveData<Marker?>()

    fun save(marker: Marker) {
        viewModelScope.launch(Dispatchers.IO) {
            mainRepository.saveMarker(marker)
        }
    }

    fun getAllMarkers() = mainRepository.getAllMarkers()

    fun deleteMarker(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            mainRepository.deleteMarker(latitude, longitude)
        }
    }

}