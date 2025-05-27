package com.example.cloova.model;
import com.google.gson.annotations.SerializedName;

public class WeatherApiResponse {
    @SerializedName("location")
    public Location location;
    @SerializedName("forecast")
    public Forecast forecast;
    @SerializedName("current")
    public DayData current;
}