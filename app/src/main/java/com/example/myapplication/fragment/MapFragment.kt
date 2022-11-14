package com.example.myapplication.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
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
    private var map: GoogleMap? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private var locationByGps: Location? = null
    private var locationByNetwork: Location? = null
    private var currentLocation: Location? = null

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
        isLocationPermissionGranted()
        binding.fbAddMarker.setOnClickListener {
            this.map?.let {
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

        binding.fbCurrentLocation.setOnClickListener {
            isLocationPermissionGranted()
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (hasGps) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 0F, gpsLocationListener
            )
        }
        if (hasNetwork) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 5000, 0F, networkLocationListener
            )
        }

        val lastKnownLocationByGps =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocationByGps?.let {
            locationByGps = lastKnownLocationByGps
        }
        val lastKnownLocationByNetwork =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        lastKnownLocationByNetwork?.let {
            locationByNetwork = lastKnownLocationByNetwork
        }
        if (locationByGps != null && locationByNetwork != null) {
            currentLocation = if (locationByGps!!.accuracy > locationByNetwork!!.accuracy)
                locationByGps
            else
                locationByNetwork
            setLocation(currentLocation!!.latitude, currentLocation!!.longitude)
        }
    }

    private fun setLocation(latitude: Double, longitude: Double) {
        map?.let {
            it.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(latitude, longitude), it.cameraPosition.zoom
                )
            )
        }
    }

    private val gpsLocationListener: LocationListener =
        LocationListener { location -> locationByGps = location }

    private val networkLocationListener: LocationListener =
        LocationListener { location -> locationByNetwork = location }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var permissionsGiven = true
        permissions.entries.forEach {
            if (!it.value)
                permissionsGiven = false
        }
        if (permissionsGiven)
            loadCurrentLocation()
        else
            Toast.makeText(requireContext(), "Permissions not granted", Toast.LENGTH_LONG)
                .show()
    }

    private fun isLocationPermissionGranted() {
        return if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else
            loadCurrentLocation()
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        currentLocation?.let {
            setLocation(it.latitude, it.longitude)
        }
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
                marker?.let { safeMarker ->
                    setLocation(safeMarker.latitude, safeMarker.longitude)
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