package com.example.cloova.model;
import com.google.gson.annotations.SerializedName;
public class DayData {
    @SerializedName("maxtemp_c")
    public double maxTempC; // Наша "Дневная" температура
    @SerializedName("mintemp_c")
    public double minTempC; // Наша "Ночная" температура
    @SerializedName("condition")
    public Condition condition;
    @SerializedName("maxwind_kph")
    public double maxwind_kph; // Максимальная скорость ветра в км/ч

    @SerializedName("avghumidity")
    public int avghumidity;   // Средняя влажность в %
}
