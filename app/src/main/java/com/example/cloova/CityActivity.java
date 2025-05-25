package com.example.cloova;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.mapview.MapView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CityActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private MapView mapView;
    private AutoCompleteTextView cityInput;
    private Button searchButton;
    private Button saveCityButton;
    private long userId;
    private String currentCity;
    private List<String> popularCities = Arrays.asList("Москва", "Санкт-Петербург", "Самара", "Казань", "Новосибирск");

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Инициализация MapKit должна быть ДО super.onCreate()
        MapKitFactory.setApiKey("ed7e3518-2afb-4989-a0b1-0cbf9283558b");
        MapKitFactory.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        userId = getIntent().getLongExtra("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        User user = dbHelper.getUserInfo(userId);
        if (user != null) {
            currentCity = user.getCity();
        }

        mapView = findViewById(R.id.mapview);
        if (mapView == null) {
            Toast.makeText(this, "Ошибка инициализации карты", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        saveCityButton = findViewById(R.id.save_city_button);
        cityInput = findViewById(R.id.city_input);
        searchButton = findViewById(R.id.search_button);

        // Настройка автозаполнения
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, popularCities);
        cityInput.setAdapter(adapter);
        cityInput.setThreshold(1);
        cityInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        cityInput.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        if (currentCity != null) {
            cityInput.setText(currentCity);
            updateMap(currentCity);
        }

        searchButton.setOnClickListener(v -> {
            String cityName = cityInput.getText().toString().trim();
            if (!cityName.isEmpty()) {
                updateMap(cityName);
            } else {
                Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show();
            }
        });

        saveCityButton.setOnClickListener(v -> {
            String selectedCity = cityInput.getText().toString().trim();
            if (!selectedCity.isEmpty()) {
                if (user != null) {
                    user.setCity(selectedCity);
                    if (dbHelper.updateUser(user)) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Введите город", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.gobackbutton).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private Point getCoordinatesFromAndroidGeocoder(String cityName) {
        if (!Geocoder.isPresent()) {
            return null;
        }
        Geocoder geocoder = new Geocoder(this, new Locale("ru", "RU"));
        try {
            List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return new Point(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Error getting coordinates", e);
        }
        return null;
    }

    private void updateMap(String cityName) {
        if (cityName == null || cityName.isEmpty()) {
            Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show();
            return;
        }
        new GeocodeTask().execute(cityName);
    }

    private class GeocodeTask extends AsyncTask<String, Void, Point> {
        @Override
        protected Point doInBackground(String... params) {
            String cityName = params[0];
            return getCoordinatesFromAndroidGeocoder(cityName);
        }

        @Override
        protected void onPostExecute(Point point) {
            if (point != null) {
                mapView.getMap().move(
                        new CameraPosition(point, 12.0f, 0.0f, 0.0f),
                        new Animation(Animation.Type.SMOOTH, 1),
                        null
                );
            } else {
                Toast.makeText(CityActivity.this,
                        "Город не найден", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) {
            mapView = null;
        }
        super.onDestroy();
    }
}