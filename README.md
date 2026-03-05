# ☁️ Weather Forecast App

A 3-day weather forecast Android app built for the Uffizio Android Developer Assignment.

## Setup Instructions

1. Clone the repository
2. Open in Android Studio
3. Add your OpenWeather API key in `local.properties` (project root):
```
   WEATHER_API_KEY=your_key_here
```
4. Run on device or emulator (API 24+)

> Get a free key at: https://openweathermap.org/api

---

## Architecture — MVVM
```
UI Layer  →  ViewModel  →  Repository  →  API + Database
```

| Layer | Class | Role |
|---|---|---|
| UI | `MainActivity` | Renders UI, handles permissions |
| UI | `WeatherAdapter` | Binds weather list to RecyclerView |
| ViewModel | `WeatherViewModel` | Holds UI state, calls repository |
| Repository | `WeatherRepository` | Coordinates API and Room DB |
| Remote | `ApiService` + `RetrofitClient` | Retrofit calls to OpenWeatherMap |
| Local | `WeatherDao` + `WeatherDatabase` | Room database for offline cache |

---

## Libraries Used

| Library | Purpose |
|---|---|
| Retrofit 2.9 | HTTP API calls |
| Gson Converter | JSON parsing |
| Room 2.6 | Local database / offline cache |
| Glide 4.16 | Weather icon loading |
| FusedLocationProvider | GPS location |
| Coroutines | Async operations |
| LiveData + ViewModel | Lifecycle-aware UI state |
| SwipeRefreshLayout | Pull-to-refresh |
| Material Components | UI + DayNight theme (Dark Mode) |

---

## Offline Support

- Weather data is saved to Room DB after every successful fetch
- When offline, last cached data is displayed automatically
- Refresh button and pull-to-refresh fetch latest data when online

## Dark Mode

App uses `Theme.MaterialComponents.DayNight` — switches automatically with system setting.