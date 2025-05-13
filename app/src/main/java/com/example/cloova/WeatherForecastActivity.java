package com.example.cloova;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cloova.adapter.ForecastAdapter;
import com.example.cloova.model.ForecastDay;
import com.example.cloova.model.WeatherApiResponse;
import com.example.cloova.network.ApiClient;
import com.example.cloova.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherForecastActivity extends AppCompatActivity {

    private static final String TAG = "WeatherForecastActivity";
    // !!! ЗАМЕНИТЕ НА ВАШ КЛЮЧ !!!
    private static final String API_KEY = "579ae8495e7443b3aa9185358252404";
    private static final int FORECAST_DAYS = 7; // Запрашиваем 7 дней
    private static final String AQI = "no";
    private static final String ALERTS = "no";
    private static final String LANGUAGE = "ru";

    public static final String EXTRA_CITY_NAME = "CITY_NAME";
    public static final String FALLBACK_CITY = "Самара";

    private RecyclerView recyclerView;
    private ForecastAdapter adapter;
    private List<ForecastDay> displayList = new ArrayList<>();
    private ApiService apiService;
    private ProgressBar progressBar;
    private Button btnMonthlyForecast;

    private String currentCity = FALLBACK_CITY;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);

        Toolbar toolbar = findViewById(R.id.toolbar_weather);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_CITY_NAME)) {
            String receivedCity = intent.getStringExtra(EXTRA_CITY_NAME);
            if (receivedCity != null && !receivedCity.isEmpty()) {
                this.currentCity = receivedCity; // Используем this.currentCity
            }
        }

        // progressBar = findViewById(R.id.progress_bar_weather); // Найдите ProgressBar
        btnMonthlyForecast = findViewById(R.id.btn_monthly_forecast);
        recyclerView = findViewById(R.id.rv_daily_forecast);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // !!! Создаем адаптер с новым списком !!!
        adapter = new ForecastAdapter(this, displayList, this.currentCity);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);

        fetchWeatherData(currentCity);

        btnMonthlyForecast.setOnClickListener(v -> {
            Toast.makeText(this, "Загрузка прогноза на месяц (TODO)", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchWeatherData(String city) {
        Log.d(TAG, "fetchWeatherData: Fetching weather for " + city);
        // if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // !!! Вызываем новый метод API !!!
        Call<WeatherApiResponse> call = apiService.getWeatherForecast(API_KEY, city, FORECAST_DAYS, AQI, ALERTS, LANGUAGE);

        call.enqueue(new Callback<WeatherApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherApiResponse> call, @NonNull Response<WeatherApiResponse> response) {
                // if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().forecast != null) {
                    Log.d(TAG, "onResponse: Success.");
                    // !!! Получаем List<ForecastDay> !!!
                    List<ForecastDay> forecastDays = response.body().forecast.forecastDay;

                    if (forecastDays != null) {
                        Log.d(TAG, "onResponse: Received " + forecastDays.size() + " forecast days.");
                        // !!! Просто передаем список в адаптер !!!
                        adapter.updateData(forecastDays, city);
                    } else {
                        Log.w(TAG, "onResponse: forecastDay list is null");
                        Toast.makeText(WeatherForecastActivity.this, "Нет данных прогноза", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "onResponse: Error - Code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(WeatherForecastActivity.this, "Ошибка загрузки: " + response.message(), Toast.LENGTH_SHORT).show();
                    adapter.updateData(new ArrayList<>(), city);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherApiResponse> call, @NonNull Throwable t) {
                // if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "onFailure: Network request failed", t);
                Toast.makeText(WeatherForecastActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                adapter.updateData(new ArrayList<>(), city);
            }
        });
    }
}