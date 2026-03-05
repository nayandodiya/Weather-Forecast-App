package com.uffizio.app.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.uffizio.app.R
import com.uffizio.app.data.repository.WeatherRepository
import com.uffizio.app.utils.NetworkUtils
import com.uffizio.app.utils.Resource
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _weatherState =
        MediatorLiveData<Resource<List<com.uffizio.app.data.db.WeatherEntity>>>()

    val weatherState: LiveData<Resource<List<com.uffizio.app.data.db.WeatherEntity>>> =
        _weatherState

    init {
        _weatherState.addSource(repository.weatherData) { data ->
            if (!data.isNullOrEmpty()) {
                _weatherState.value = Resource.Success(data)
            }
        }
    }

    fun loadWeather(context: Context, lat: Double, lon: Double, isManualRefresh: Boolean = false) {
        viewModelScope.launch {

            val hasInternet = NetworkUtils.isInternetAvailable(context)
            val cached = repository.weatherData.value

            if (!hasInternet) {
                if (!cached.isNullOrEmpty()) {

                    _weatherState.value = Resource.Success(cached)
                } else {

                    if (isManualRefresh) {
                        _weatherState.value = Resource.Error(context.getString(R.string.no_internet_connection))
                    } else {

                        _weatherState.value = Resource.Error(context.getString(R.string.no_internet_connection))
                    }
                }
                return@launch
            }

            try {
                _weatherState.value = Resource.Loading()
                repository.fetchWeather(lat, lon)

            } catch (e: Exception) {
                if (!cached.isNullOrEmpty()) {
                    _weatherState.value = Resource.Success(cached)
                } else {
                    _weatherState.value =
                        Resource.Error(context.getString(R.string.something_went_wrong_please_try_again))
                }
            }
        }
    }

}