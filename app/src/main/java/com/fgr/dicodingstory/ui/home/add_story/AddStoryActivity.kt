package com.fgr.dicodingstory.ui.home.add_story

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fgr.dicodingstory.R
import com.fgr.dicodingstory.component.CustomProgressDialog
import com.fgr.dicodingstory.databinding.ActivityAddStoryBinding
import com.fgr.dicodingstory.shared_pref.UserPreference
import com.fgr.dicodingstory.utils.REQUIRED_PERMISSIONS
import com.fgr.dicodingstory.utils.allPermissionsGranted
import com.fgr.dicodingstory.utils.rotateFile
import com.fgr.dicodingstory.utils.uriToFile
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.util.concurrent.TimeUnit

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private var getFile: File? = null
    private val viewModel: AddStoryViewModel by viewModels()
    private val progressDialog by lazy { CustomProgressDialog(this) }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var myLocation: LatLng? = null

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        const val CAMERA_X_RESULT = 200
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    createLocationRequest()
                }

                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    createLocationRequest()
                }

                else -> {
                    // No location access granted.
                }
            }
        }


    private val resolutionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_OK ->
                    Log.i(
                        "AddStoryActivity",
                        "onActivityResult: All location settings are satisfied."
                    )

                RESULT_CANCELED ->
                    Toast.makeText(
                        this@AddStoryActivity,
                        resources.getString(R.string.gps_is_required),
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                val myFile = uriToFile(uri, this@AddStoryActivity)
                getFile = myFile
                binding.addStoryPreviewImageView.setImageURI(uri)
            }
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.data?.getSerializableExtra("picture", File::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.data?.getSerializableExtra("picture")
            } as? File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            myFile?.let { file ->
                getFile = file
                rotateFile(file, isBackCamera)
                binding.addStoryPreviewImageView.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.create_story)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val userPreference = UserPreference(this)

        createLocationCallback()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.addStorySwipeLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getMyLastLocation()
            } else {
                myLocation = null
//                stopLocationUpdates()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                progressDialog.start(getString(R.string.please_wait))
            } else {
                progressDialog.stop()
            }
        }

        viewModel.responseBody.observe(this) { response ->
            if (response.isError) {
                //Failed
                Toast.makeText(this, getString(R.string.time_out), Toast.LENGTH_SHORT).show()
            } else {
                //Success
                setResult(Activity.RESULT_OK)
                finish()
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.addStoryCameraButton.setOnClickListener {
            if (!allPermissionsGranted(this)) {
                ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
            } else {
                startTakePhoto()
            }
        }

        binding.addStoryGalleryButton.setOnClickListener {
            startGallery()
        }

        binding.addStoryPostButton.setOnClickListener {
            if (getFile != null && binding.addStoryInputText.text.toString().isNotEmpty()) {
                if (binding.addStorySwipeLocation.isChecked) {
                    viewModel.postStory(
                        "Bearer ${userPreference.getUser().token}",
                        getFile as File,
                        binding.addStoryInputText.text.toString(),
                        myLocation
                    )
                } else {
                    viewModel.postStory(
                        "Bearer ${userPreference.getUser().token}",
                        getFile as File,
                        binding.addStoryInputText.text.toString(),
                        null
                    )
                }
            } else {
                Toast.makeText(this, getString(R.string.empty_post_info), Toast.LENGTH_SHORT).show()
            }
            stopLocationUpdates()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startTakePhoto() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted(baseContext)) {
                Toast.makeText(
                    this,
                    getString(R.string.permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startTakePhoto()
            }
        }
    }

    private fun getMyLastLocation() {
        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            createLocationRequest()
        } else {
            binding.addStorySwipeLocation.isChecked = false
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {

                startLocationUpdates()

                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        myLocation = LatLng(location.latitude, location.longitude)
                        binding.addStorySwipeLocation.isChecked = true
                    } else {
                        binding.addStorySwipeLocation.isChecked = false
                        Toast.makeText(
                            this@AddStoryActivity,
                            "Location is not found. Try Again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                binding.addStorySwipeLocation.isChecked = false
                if (exception is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(this@AddStoryActivity, sendEx.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val lastLatLng = LatLng(location.latitude, location.longitude)
                    myLocation = lastLatLng
                }
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (exception: SecurityException) {
            Log.e("AddStoryActivity", "Error : " + exception.message)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}