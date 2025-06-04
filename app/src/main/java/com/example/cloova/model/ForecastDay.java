package com.example.cloova.model;
import com.google.gson.annotations.SerializedName;
public class ForecastDay {
    @SerializedName("date")
    public String date;
    @SerializedName("date_epoch")
    public long dateEpoch;
    @SerializedName("day")
    public DayData day;
}