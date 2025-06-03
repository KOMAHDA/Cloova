package com.example.cloova;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    private static final String API_KEY = "579ae8495e7443b3aa9185358252404";
    private static final int FORECAST_DAYS = 7;
    private static final String AQI = "no";
    private static final String ALERTS = "no";
    private static final String LANGUAGE = "ru";

    public static final String EXTRA_CITY_NAME = "CITY_NAME";
    public static final String EXTRA_USER_STYLE = "USER_STYLE";
    public static final String FALLBACK_CITY = "Самара";

    private RecyclerView recyclerView;
    private ForecastAdapter adapter;
    private List<ForecastDay> displayList = new ArrayList<>();
    private ApiService apiService;
    private ProgressBar progressBar;

    private String currentCity = FALLBACK_CITY;
    private String currentUserStyle = "Повседневный";
    private ImageView navProfileIcon, navHomeIcon, navFavoritesIcon;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_CITY_NAME)) {
            String receivedCity = intent.getStringExtra(EXTRA_CITY_NAME);
            if (receivedCity != null && !receivedCity.isEmpty()) {
                this.currentCity = receivedCity;
            }
        }

        if (intent.hasExtra(EXTRA_USER_STYLE)) {
            String receivedStyle = intent.getStringExtra(EXTRA_USER_STYLE);
            if (receivedStyle != null && !receivedStyle.isEmpty()) {
                this.currentUserStyle = receivedStyle;
            }
        }


        recyclerView = findViewById(R.id.rv_daily_forecast);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ForecastAdapter(this, displayList, this.currentCity, this.currentUserStyle);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);

        fetchWeatherData(currentCity);

        setupBottomNavigation();

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



        Call<WeatherApiResponse> call = apiService.getWeatherForecast(API_KEY, city, FORECAST_DAYS, AQI, ALERTS, LANGUAGE);

        call.enqueue(new Callback<WeatherApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherApiResponse> call, @NonNull Response<WeatherApiResponse> response) {


                if (response.isSuccessful() && response.body() != null && response.body().forecast != null) {
                    Log.d(TAG, "onResponse: Success.");

                    List<ForecastDay> forecastDays = response.body().forecast.forecastDay;

                    if (forecastDays != null) {
                        Log.d(TAG, "onResponse: Received " + forecastDays.size() + " forecast days.");

                        adapter.updateData(forecastDays, city, WeatherForecastActivity.this.currentUserStyle);
                    } else {
                        Log.w(TAG, "onResponse: forecastDay list is null");
                        Toast.makeText(WeatherForecastActivity.this, "Нет данных прогноза", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "onResponse: Error - Code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(WeatherForecastActivity.this, "Ошибка загрузки: " + response.message(), Toast.LENGTH_SHORT).show();
                    adapter.updateData(new ArrayList<>(), city, WeatherForecastActivity.this.currentUserStyle);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherApiResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: Network request failed", t);
                Toast.makeText(WeatherForecastActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                adapter.updateData(new ArrayList<>(), city, WeatherForecastActivity.this.currentUserStyle);
            }
        });
    }

    private void setupBottomNavigation() {
        navProfileIcon = findViewById(R.id.nav_profile_icon_weather);
        navHomeIcon = findViewById(R.id.nav_home_icon_weather);
        navFavoritesIcon = findViewById(R.id.nav_favorites_icon_weather);

        navProfileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        navHomeIcon.setOnClickListener(v -> {
            fetchWeatherData(currentCity);
            Toast.makeText(this, "Прогноз обновлен", Toast.LENGTH_SHORT).show();
        });

        navFavoritesIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, Sohranenki.class);
            startActivity(intent);
        });
    }
}