package com.uffizio.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather ORDER BY date ASC LIMIT 3")
    fun getThreeDayWeather(): LiveData<List<WeatherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weatherList: List<WeatherEntity>)

    @Query("DELETE FROM weather")
    suspend fun clearAll()
}