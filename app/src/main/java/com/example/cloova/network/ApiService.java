package com.example.cloova.network;
import com.example.cloova.model.WeatherApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("v1/forecast.json")
    Call<WeatherApiResponse> getWeatherForecast(
            @Query("key") String apiKey,
            @Query("q") String cityName,
            @Query("days") int days,
            @Query("aqi") String aqi,
            @Query("alerts") String alerts,
            @Query("lang") String language
    );
}