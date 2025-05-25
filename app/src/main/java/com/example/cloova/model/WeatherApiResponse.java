package com.example.cloova.model;
import com.google.gson.annotations.SerializedName;

public class WeatherApiResponse {
    @SerializedName("location")
    public Location location; // Можно добавить, если нужна инфо о городе
    @SerializedName("forecast")
    public Forecast forecast;
    @SerializedName("current")
    public DayData current;
}