package com.example.cloova.model;
import com.google.gson.annotations.SerializedName;
import java.util.List; // Для hourly forecast, если понадобится
public class ForecastDay {
    @SerializedName("date")
    public String date; // "YYYY-MM-DD"
    @SerializedName("date_epoch")
    public long dateEpoch;
    @SerializedName("day")
    public DayData day;
}