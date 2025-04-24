package com.example.cloova.model;
import com.google.gson.annotations.SerializedName;
public class Condition {
    @SerializedName("text")
    public String text; // Описание (напр. "Местами дождь")
    @SerializedName("icon")
    public String iconUrlPath; // Относительный URL иконки (напр. "//cdn.weatherapi.com/weather/64x64/day/176.png")
    @SerializedName("code")
    public int code; // Код состояния погоды
}