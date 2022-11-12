package com.example.myapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentMapBinding
import com.example.myapplication.view_model.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var viewBinding: FragmentMapBinding? = null
    private val binding: FragmentMapBinding get() = viewBinding!!
    private lateinit var map: GoogleMap
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.fbAddMarker.setOnClickListener {
            this.map.let {
                mainViewModel.selectedMarker.postValue(
                    com.example.myapplication.database.model.Marker(
                        it.cameraPosition.target.latitude,
                        it.cameraPosition.target.longitude
                    )
                )
                DetailFragment.listener = null
                DetailFragment().show(parentFragmentManager, DetailFragment::javaClass.name)
            }
        }

        binding.fbList.setOnClickListener {
            findNavController().navigate(R.id.listFragment)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        mainViewModel.getAllMarkers().observe(viewLifecycleOwner) {
            map.clear()
            it.forEach { marker ->
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(marker.latitude, marker.longitude))
                        .title(marker.title)
                )
            }
            mainViewModel.showMarker.observe(viewLifecycleOwner) { marker ->
                marker?.let {
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                marker.latitude,
                                marker.longitude
                            ), map.cameraPosition.zoom
                        )
                    )
                    mainViewModel.showMarker.postValue(null)
                }

            }
        }
        map.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        DetailFragment.listener = object : DetailFragment.Listener {
            override fun onDelete(isRemove: Boolean) {
                if (isRemove) {
                    marker.remove()
                    mainViewModel.deleteMarker(
                        marker.position.latitude,
                        marker.position.longitude
                    )
                }
            }
        }
        mainViewModel.selectedMarker.postValue(
            com.example.myapplication.database.model.Marker(
                marker.position.latitude,
                marker.position.longitude,
                marker.title
            )
        )
        DetailFragment().show(parentFragmentManager, DetailFragment::javaClass.name)
        return false
    }
}