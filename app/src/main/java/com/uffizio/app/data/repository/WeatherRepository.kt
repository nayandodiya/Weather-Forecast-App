package com.uffizio.app.data.repository

import androidx.lifecycle.LiveData
import com.uffizio.app.data.api.ApiService
import com.uffizio.app.data.api.Constants
import com.uffizio.app.data.db.WeatherDao
import com.uffizio.app.data.db.WeatherEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val api: ApiService,
    private val dao: WeatherDao
) {

    val weatherData: LiveData<List<WeatherEntity>> = dao.getThreeDayWeather()

    suspend fun fetchWeather(lat: Double, lon: Double) = withContext(Dispatchers.IO) {

        val response = api.getWeather(lat, lon, Constants.API_KEY)

        val threeDays = response.list
            .groupBy { it.dt_txt.substring(0,10) }
            .map { it.value.first() }
            .take(3)

        val entities = threeDays.map {
            WeatherEntity(
                date = it.dt_txt.substring(0,10),
                temperature = it.main.temp,
                condition = it.weather[0].main,
                icon = it.weather[0].icon
            )
        }

        dao.clearAll()
        dao.insertWeather(entities)
    }
}
