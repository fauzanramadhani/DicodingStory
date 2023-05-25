package com.fgr.dicodingstory.ui.home.maps_story

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fgr.dicodingstory.R
import com.fgr.dicodingstory.databinding.ActivityMapsBinding
import com.fgr.dicodingstory.retrofit.ListStoryItemWithMap
import com.fgr.dicodingstory.ui.home.list_story.ListStoryViewModel
import com.fgr.dicodingstory.utils.ViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val viewModel: ListStoryViewModel by viewModels {
        ViewModelFactory(this)
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var boundsBuilder = LatLngBounds.Builder()
    private val indonesianBounds = ArrayList<ListStoryItemWithMap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setMapStyle()

        viewModel.listStoryWithMap.observe(this) { list ->

            // filter stories that are in Indonesian coordinates only
            list.map { item ->
                if (item.lat > -11.00 && item.lat < 6.00 && item.lon > 95.00 && item.lon < 141.00) {
                    indonesianBounds.add(item)
                }
            }

            indonesianBounds.map { indonesianLocated ->
                mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(indonesianLocated.lat, indonesianLocated.lon))
                        .snippet(indonesianLocated.description)
                        .title(indonesianLocated.name)
                )

                boundsBuilder.include(LatLng(indonesianLocated.lat, indonesianLocated.lon))
            }
            val bounds: LatLngBounds = boundsBuilder.build()
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64))
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e("MapsActivity", "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e("MapsActivity", "Can't find style. Error: ", exception)
        }
    }
}