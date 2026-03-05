package com.uffizio.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uffizio.app.R
import com.uffizio.app.data.db.WeatherEntity
import com.uffizio.app.databinding.ItemWeatherBinding
import java.text.SimpleDateFormat
import java.util.*

class WeatherAdapter(
    private var weatherList: List<WeatherEntity>
) : RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    class WeatherViewHolder(
        val binding: ItemWeatherBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {

        val binding = ItemWeatherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return WeatherViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return weatherList.size
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {

        val item = weatherList[position]

        holder.binding.tvDate.text = formatDate(item.date)

        holder.binding.tvTemperature.text = "${item.temperature} °C"

        holder.binding.tvCondition.text =
            item.condition.replaceFirstChar { it.uppercase() }

        val iconUrl =
            "https://openweathermap.org/img/wn/${item.icon}@2x.png"

        Glide.with(holder.itemView.context)
            .load(iconUrl)
            .placeholder(R.drawable.ic_weather_placeholder)
            .error(R.drawable.ic_weather_placeholder)
            .into(holder.binding.ivWeatherIcon)
    }

    fun updateData(newList: List<WeatherEntity>) {
        weatherList = newList
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat =
                SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateString
        }
    }
}