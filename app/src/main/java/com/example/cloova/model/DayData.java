package com.example.cloova.model;
import com.google.gson.annotations.SerializedName;
public class DayData {
    @SerializedName("maxtemp_c")
    public double maxTempC;
    @SerializedName("mintemp_c")
    public double minTempC;
    @SerializedName("condition")
    public Condition condition;
    @SerializedName("maxwind_kph")
    public double maxwind_kph;

    @SerializedName("avghumidity")
    public int avghumidity;
}