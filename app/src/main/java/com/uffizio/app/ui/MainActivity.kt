package com.uffizio.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.uffizio.app.R
import com.uffizio.app.adapter.WeatherAdapter
import com.uffizio.app.data.api.RetrofitClient
import com.uffizio.app.data.db.WeatherDatabase
import com.uffizio.app.data.repository.WeatherRepository
import com.uffizio.app.databinding.ActivityMainBinding
import com.uffizio.app.utils.Resource
import com.uffizio.app.viewmodel.WeatherViewModel
import com.uffizio.app.viewmodel.WeatherViewModelFactory
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: WeatherViewModel
    private lateinit var adapter: WeatherAdapter

    private val LOCATION_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupViewModel()
        observeWeather()

        getCurrentLocation()

        binding.btnRefresh.setOnClickListener {
            getCurrentLocation(isManualRefresh = true)
        }

        binding.swipeRefresh.setOnRefreshListener {
            getCurrentLocation(isManualRefresh = true)
        }
    }

    private fun setupRecyclerView() {

        adapter = WeatherAdapter(emptyList())

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerView.adapter = adapter
    }

    private fun setupViewModel() {

        val dao = WeatherDatabase.getDatabase(this).weatherDao()

        val repository = WeatherRepository(
            RetrofitClient.api,
            dao
        )

        val factory = WeatherViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)
            .get(WeatherViewModel::class.java)
    }

    private fun observeWeather() {

        viewModel.weatherState.observe(this) { state ->

            when (state) {

                is Resource.Loading -> {

                    binding.progressBar.visibility = View.VISIBLE
                    binding.swipeRefresh.visibility = View.GONE
                    binding.tvNoInternet.visibility = View.GONE
                    binding.btnRefresh.visibility = View.GONE
                }

                is Resource.Success -> {

                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.visibility = View.VISIBLE
                    binding.tvNoInternet.visibility = View.GONE
                    binding.btnRefresh.visibility = View.VISIBLE

                    binding.swipeRefresh.isRefreshing = false

                    val data = state.data ?: emptyList()

                    adapter.updateData(data)

                    if (data.isNotEmpty()) {

                        val today = data[0]

                        binding.tvCurrentTemp.text =
                            "${today.temperature}°C"

                        binding.tvCurrentCondition.text =
                            today.condition
                    }
                }

                is Resource.Error -> {

                    binding.progressBar.visibility = View.GONE
                    binding.btnRefresh.visibility = View.VISIBLE
                    binding.swipeRefresh.isRefreshing = false

                    val data = state.data ?: emptyList()

                    if (data.isEmpty()) {

                        binding.tvNoInternet.visibility = View.VISIBLE
                        binding.swipeRefresh.visibility = View.GONE

                    } else {

                        binding.tvNoInternet.visibility = View.GONE
                        binding.swipeRefresh.visibility = View.VISIBLE
                        adapter.updateData(data)
                    }

                    Toast.makeText(
                        this,
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }



    private fun getCurrentLocation(isManualRefresh: Boolean = false) {

        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->

            if (location != null) {

                val lat = location.latitude
                val lon = location.longitude

                setCityName(lat, lon)

                viewModel.loadWeather(this, lat, lon, isManualRefresh)

            } else {

                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { newLocation ->

                    if (newLocation != null) {

                        val lat = newLocation.latitude
                        val lon = newLocation.longitude

                        setCityName(lat, lon)

                        viewModel.loadWeather(
                            this,
                            lat,
                            lon,
                            isManualRefresh
                        )

                    } else {

                        Toast.makeText(
                            this,
                            getString(R.string.unable_to_fetch_location_please_try_again),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun setCityName(lat: Double, lon: Double) {

        try {

            val geocoder =
                Geocoder(this, Locale.getDefault())

            val address =
                geocoder.getFromLocation(lat, lon, 1)

            if (!address.isNullOrEmpty()) {

                val city =
                    address[0].locality ?: "Unknown"

                binding.tvCity.text = city
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == LOCATION_PERMISSION_CODE) {

            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {

                getCurrentLocation()

            } else {

                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {

                    showSettingsDialog()

                } else {

                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_is_required_to_fetch_weather),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showSettingsDialog() {

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.location_permission_is_permanently_denied_please_enable_it_from_settings))
            .setPositiveButton(getString(R.string.open_settings)) { _, _ ->

                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                )

                val uri = Uri.fromParts(
                    "package",
                    packageName,
                    null
                )

                intent.data = uri

                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
