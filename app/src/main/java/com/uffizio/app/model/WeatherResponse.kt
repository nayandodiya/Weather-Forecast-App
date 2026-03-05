package com.uffizio.app.model

data class WeatherResponse(
    val list: List<WeatherItem>
)

data class WeatherItem(
    val dt_txt: String,
    val main: Main,
    val weather: List<WeatherInfo>
)

data class Main(
    val temp: Double
)

data class WeatherInfo(
    val main: String,
    val icon: String
)

