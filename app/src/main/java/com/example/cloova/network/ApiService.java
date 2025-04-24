package com.example.cloova.network;
import com.example.cloova.model.WeatherApiResponse; // Наша новая модель ответа
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    // Запрос прогноза от WeatherAPI.com
    @GET("v1/forecast.json") // Путь к API прогноза
    Call<WeatherApiResponse> getWeatherForecast(
            @Query("key") String apiKey,       // Ваш API ключ
            @Query("q") String cityName,         // Название города
            @Query("days") int days,             // Количество дней прогноза (до 10 на беспл.)
            @Query("aqi") String aqi,            // Air Quality Index (no)
            @Query("alerts") String alerts,       // Оповещения (no)
            @Query("lang") String language       // Язык ответа (напр: "ru")
    );
}