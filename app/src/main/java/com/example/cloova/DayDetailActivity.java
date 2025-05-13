package com.example.cloova; // Убедитесь, что пакет правильный

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
// import com.bumptech.glide.Glide; // Если будете использовать Glide для иконок погоды из URL

import com.example.cloova.model.ClothingItem;
import com.example.cloova.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random; // Для простого выбора одежды из категории

public class DayDetailActivity extends AppCompatActivity {

    private static final String TAG = "DayDetailActivity";

    // Ключи для Intent Extras (создайте их как public static final в WeatherForecastActivity или Constants)
    public static final String EXTRA_DATE_STR = "EXTRA_DATE_STR"; // "YYYY-MM-DD"
    public static final String EXTRA_MAX_TEMP = "EXTRA_MAX_TEMP";
    public static final String EXTRA_MIN_TEMP = "EXTRA_MIN_TEMP";
    public static final String EXTRA_WIND_KPH = "EXTRA_WIND_KPH";
    public static final String EXTRA_HUMIDITY = "EXTRA_HUMIDITY";
    public static final String EXTRA_WEATHER_ICON_URL = "EXTRA_WEATHER_ICON_URL"; // Или _CODE, если передаете код
    public static final String EXTRA_WEATHER_DESCRIPTION = "EXTRA_WEATHER_DESCRIPTION";
    public static final String EXTRA_WEATHER_CODE = "EXTRA_WEATHER_CODE";
    public static final String EXTRA_CITY_NAME = "EXTRA_CITY_NAME"; // Для отображения города

    private DatabaseHelper dbHelper;
    private long currentUserId = DatabaseHelper.DEFAULT_USER_ID;
    private User currentUser;

    // UI элементы для данных дня
    private MaterialButton btnLocationDetail;
    private TextView tvDetailTemp, tvWindValueDetail, tvHumidityValueDetail;
    private ImageView ivDetailWeatherIcon;
    private TextView btnBackToWeeklyDetail;
    private TextView tvOutfitSuggestionDetail;
    private ImageView ivMannequin; // Заглушка для манекена

    // UI для отображения предложенной одежды (простой вариант - TextView)
    private LinearLayout suggestedOutfitContainer; // Добавьте этот LinearLayout в XML под манекеном

    // FAB кнопки
    private FloatingActionButton fabEditOutfit, fabAddFavorite, fabRefresh;

    // Нижняя навигация (кастомная)
    private ImageView navProfileIcon, navHomeIcon, navFavoritesIcon;

    // Данные дня, полученные из Intent
    private String dateString;
    private double maxTemp;
    private double minTemp;
    private String weatherIconUrl;
    private String weatherDescription;
    private String currentCity;
    private double windKph;
    private int humidity;
    private int weatherCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_detail);
        Log.d(TAG, "onCreate: Activity starting");

        dbHelper = new DatabaseHelper(this);
        initViews();
        retrieveIntentData();
        retrieveUserIdAndLoadUser(); // Загрузит пользователя и затем одежду

        setupClickListeners();
    }

    private void initViews() {
        Log.d(TAG, "initViews: Initializing UI elements");
        btnLocationDetail = findViewById(R.id.btn_location_detail);
        tvDetailTemp = findViewById(R.id.tv_detail_temp);
        ivDetailWeatherIcon = findViewById(R.id.iv_detail_weather_icon);
        tvWindValueDetail = findViewById(R.id.tv_wind_value_detail); // Предполагаем, что эти ID есть
        tvHumidityValueDetail = findViewById(R.id.tv_humidity_value_detail); // Предполагаем, что эти ID есть
        btnBackToWeeklyDetail = findViewById(R.id.btn_back_to_weekly_detail);
        tvOutfitSuggestionDetail = findViewById(R.id.tv_outfit_suggestion_detail);
        ivMannequin = findViewById(R.id.iv_mannequin);

        suggestedOutfitContainer = findViewById(R.id.suggested_outfit_container_ll); // Убедитесь, что такой ID есть

        fabEditOutfit = findViewById(R.id.fab_edit_outfit_detail);
        fabAddFavorite = findViewById(R.id.fab_add_favorite_detail);
        fabRefresh = findViewById(R.id.fab_refresh_detail);

        navProfileIcon = findViewById(R.id.nav_profile_icon);
        navHomeIcon = findViewById(R.id.nav_home_icon);
        navFavoritesIcon = findViewById(R.id.nav_favorites_icon);
    }

    private void retrieveIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            dateString = intent.getStringExtra(EXTRA_DATE_STR);
            maxTemp = intent.getDoubleExtra(EXTRA_MAX_TEMP, 20.0); // Значение по умолчанию
            minTemp = intent.getDoubleExtra(EXTRA_MIN_TEMP, 10.0); // Значение по умолчанию
            weatherIconUrl = intent.getStringExtra(EXTRA_WEATHER_ICON_URL);
            weatherDescription = intent.getStringExtra(EXTRA_WEATHER_DESCRIPTION);
            weatherCode = intent.getIntExtra(EXTRA_WEATHER_CODE, 1000);
            this.currentCity = intent.getStringExtra(EXTRA_CITY_NAME);
            if (this.currentCity == null || this.currentCity.isEmpty()) {
                this.currentCity = WeatherForecastActivity.FALLBACK_CITY;
                Log.w(TAG, "retrieveIntentData: CityName not found in Intent, using fallback: " + this.currentCity);
            }
            windKph = intent.getDoubleExtra(EXTRA_WIND_KPH, 0.0); // Значение по умолчанию
            humidity = intent.getIntExtra(EXTRA_HUMIDITY, 0);     // Значение по умолчанию

            Log.d(TAG, "retrieveIntentData: Date: " + dateString + ", MaxT: " + maxTemp +
                    ", Wind: " + windKph + " kph, Humidity: " + humidity + "%" + ", City: " + this.currentCity + ", Weather Code" + weatherCode);
            displayStaticDayData();
        } else {
            Log.e(TAG, "retrieveIntentData: Intent is null, cannot display day details.");
            Toast.makeText(this, "Ошибка: нет данных о дне", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // В DayDetailActivity.java

    private int getWeatherIconResourceByApiCode(int apiCode) {
        Log.d(TAG, "getWeatherIconResourceByApiCode: Mapping API code " + apiCode);
        // Основывайтесь на https://www.weatherapi.com/docs/weather_conditions.json
        // Замените R.drawable.xxx на ID ВАШИХ иконок
        switch (apiCode) {
            case 1000: // Sunny / Clear
                // Здесь можно добавить логику для дня/ночи, если у вас есть is_day флаг для дня
                // и разные иконки для ясного дня и ясной ночи.
                // Пока просто возвращаем одну "солнечную" иконку.
                return R.drawable.wb_sunny; // Ваша иконка для "Ясно/Солнечно"
            case 1003: // Partly cloudy
                return R.drawable.partly_cloudy; // Ваша иконка для "Переменная облачность"
            case 1006: // Cloudy
            case 1009: // Overcast
                return R.drawable.cloud; // Ваша иконка для "Облачно"
            case 1030: // Mist
            case 1135: // Fog
            case 1147: // Freezing fog
                return R.drawable.foggy; // Ваша иконка для "Туман/Дымка"
            case 1063: // Patchy rain possible
            case 1150: // Patchy light drizzle
            case 1153: // Light drizzle
            case 1180: // Patchy light rain
            case 1183: // Light rain
                return R.drawable.rainy; // Ваша иконка для "Небольшой дождь"
            case 1066: // Patchy snow possible
            case 1210: // Patchy light snow
            case 1213: // Light snow
                return R.drawable.weather_snowy_24dp_e3e3e3_fill0_wght400_grad0_opsz24; // Ваша иконка для "Небольшой снег"
            case 1069: // Patchy sleet possible
            case 1204: // Light sleet
            case 1207: // Moderate or heavy sleet
            case 1249: // Light sleet showers
            case 1252: // Moderate or heavy sleet showers
                return R.drawable.weather_mix; // Ваша иконка для "Дождь со снегом"
            case 1072: // Patchy freezing drizzle possible
            case 1168: // Freezing drizzle
            case 1171: // Heavy freezing drizzle
                return R.drawable.icerain; // Ваша иконка для "Ледяной дождь"
            case 1087: // Thundery outbreaks possible
            case 1273: // Patchy light rain with thunder
            case 1276: // Moderate or heavy rain with thunder
                return R.drawable.thunderstorm; // Ваша иконка для "Гроза"
            case 1114: // Blowing snow
            case 1117: // Blizzard
            case 1216: // Patchy moderate snow
            case 1219: // Moderate snow
            case 1222: // Patchy heavy snow
            case 1225: // Heavy snow
            case 1255: // Light snow showers
            case 1258: // Moderate or heavy snow showers
                return R.drawable.weather_snowy_24dp_e3e3e3_fill0_wght400_grad0_opsz24; // Ваша иконка для "Снег"
            case 1186: // Moderate rain at times
            case 1189: // Moderate rain
            case 1192: // Heavy rain at times
            case 1195: // Heavy rain
            case 1240: // Light rain shower
            case 1243: // Moderate or heavy rain shower
            case 1246: // Torrential rain shower
                return R.drawable.rainy; // Ваша иконка для "Дождь"
            case 1279: // Patchy light snow with thunder
            case 1282: // Moderate or heavy snow with thunder
                return R.drawable.weather_mix; // Ваша иконка "Снег с грозой"

            default:
                Log.w(TAG, "Unknown weather code: " + apiCode + ", using default icon.");
                return R.drawable.cloud; // Ваша иконка по умолчанию
        }
    }

    private void displayStaticDayData() {
        if (this.currentCity != null && !this.currentCity.isEmpty()) {
            btnLocationDetail.setText(this.currentCity);
        } else {
            btnLocationDetail.setText("Город не указан");
        }

        tvDetailTemp.setText(String.format(Locale.getDefault(), "%.0f°", maxTemp)); // Отображаем дневную (max)

        if (ivDetailWeatherIcon != null) {
            ivDetailWeatherIcon.setImageResource(getWeatherIconResourceByApiCode(this.weatherCode));
        } else {
            Log.e(TAG, "ivDetailWeatherIcon is null!");
        }

        // --- ОТОБРАЖАЕМ ВЕТЕР И ВЛАЖНОСТЬ ---
        tvWindValueDetail.setText(String.format(Locale.getDefault(), "до %.0f м/с", windKph / 3.6)); // Переводим км/ч в м/с и округляем
        tvHumidityValueDetail.setText(String.format(Locale.getDefault(), "%d%%", humidity));
    }

    private void retrieveUserIdAndLoadUser() {
        SharedPreferences prefs = getSharedPreferences(DatabaseHelper.SHARED_PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getLong(DatabaseHelper.PREF_KEY_LOGGED_IN_USER_ID, DatabaseHelper.DEFAULT_USER_ID);

        if (currentUserId != DatabaseHelper.DEFAULT_USER_ID) {
            new LoadUserAndOutfitTask().execute(currentUserId);
        } else {
            Log.e(TAG, "retrieveUserIdAndLoadUser: Invalid userId, cannot load user or outfit.");
            Toast.makeText(this, R.string.error_auth, Toast.LENGTH_SHORT).show();
            // Возможно, перенаправить на логин
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadUserAndOutfitTask extends AsyncTask<Long, Void, UserOutfitData> {
        @Override
        protected UserOutfitData doInBackground(Long... userIds) {
            long userId = userIds[0];
            User user = dbHelper.getUserInfo(userId);
            List<ClothingItem> suggestedOutfit = null;

            if (user != null && user.getGender() != null && !user.getGender().isEmpty()) {
                // Предполагаемый стиль пользователя (первый из его списка или другой механизм)
                List<String> userStyles = dbHelper.getUserStyles(userId);
                String preferredStyle = (userStyles != null && !userStyles.isEmpty()) ? userStyles.get(0) : "Повседневный";

                // Маппинг описания погоды из API в названия из нашего каталога
                // Это ОЧЕНЬ упрощенный маппинг, его нужно расширить
                List<String> conditionNamesForDB = new ArrayList<>();
                if (weatherDescription != null) {
                    String descLower = weatherDescription.toLowerCase();
                    if (descLower.contains("солн") || descLower.contains("ясно")) conditionNamesForDB.add("Солнечно");
                    if (descLower.contains("облач")) conditionNamesForDB.add("Облачно"); // Учитывает и "переменная"
                    if (descLower.contains("дожд")) conditionNamesForDB.add("Дождь"); // Учитывает и "небольшой"
                    if (descLower.contains("снег")) conditionNamesForDB.add("Снег");
                    if (descLower.contains("ветер") || descLower.contains("ветрен")) conditionNamesForDB.add("Ветрено");
                    if (descLower.contains("туман")) conditionNamesForDB.add("Туман");
                    if (conditionNamesForDB.isEmpty()) conditionNamesForDB.add("Переменная облачность"); // Запасной вариант
                } else {
                    conditionNamesForDB.add("Переменная облачность"); // Если описания нет
                }

                suggestedOutfit = dbHelper.getSuggestedOutfit(
                        (int) Math.round(maxTemp), // Используем maxTemp как основную температуру для подбора
                        conditionNamesForDB,
                        preferredStyle,
                        user.getGender()
                );
            }
            return new UserOutfitData(user, suggestedOutfit);
        }

        @Override
        protected void onPostExecute(@Nullable UserOutfitData result) {
            if (result != null && result.user != null) {
                currentUser = result.user; // Сохраняем для других нужд (например, передача города)
                Log.d(TAG, "User data loaded: " + currentUser.getName());
                if (result.outfit != null) {
                    Log.d(TAG, "Suggested outfit loaded, items: " + result.outfit.size());
                    displaySuggestedOutfit(result.outfit);
                } else {
                    Log.w(TAG, "Suggested outfit is null.");
                    tvOutfitSuggestionDetail.setText("Не удалось подобрать образ.");
                }
            } else {
                Log.e(TAG, "Failed to load user data.");
                tvOutfitSuggestionDetail.setText("Ошибка загрузки данных пользователя.");
            }
        }
    }

    // Вспомогательный класс для передачи User и Outfit
    private static class UserOutfitData {
        User user;
        List<ClothingItem> outfit;
        UserOutfitData(User user, List<ClothingItem> outfit) {
            this.user = user;
            this.outfit = outfit;
        }
    }


    private void displaySuggestedOutfit(List<ClothingItem> outfitItems) {
        if (suggestedOutfitContainer == null) {
            Log.e(TAG, "suggestedOutfitContainer is null, cannot display outfit.");
            return;
        }
        suggestedOutfitContainer.removeAllViews(); // Очищаем предыдущие

        if (outfitItems == null || outfitItems.isEmpty()) {
            tvOutfitSuggestionDetail.setText("Подходящей одежды не найдено.");
            ivMannequin.setVisibility(View.GONE); // Скрываем манекен, если нет одежды
            return;
        }

        tvOutfitSuggestionDetail.setText(getString(R.string.outfit_suggestion_placeholder)); // Восстанавливаем текст
        ivMannequin.setVisibility(View.VISIBLE); // Показываем манекен

        // *** УПРОЩЕННАЯ ЛОГИКА ВЫБОРА ОДНОГО ПРЕДМЕТА НА КАТЕГОРИЮ ***
        // В реальном приложении здесь должна быть более сложная система
        // для "надевания" на манекен или формирования комплекта.

        ClothingItem top = null;
        ClothingItem bottom = null;
        ClothingItem outerwear = null;
        ClothingItem shoes = null;

        List<ClothingItem> availableTops = new ArrayList<>();
        List<ClothingItem> availableBottoms = new ArrayList<>();
        List<ClothingItem> availableOuterwear = new ArrayList<>();
        List<ClothingItem> availableShoes = new ArrayList<>();

        for (ClothingItem item : outfitItems) {
            if (item.getCategory().equalsIgnoreCase("Верх")) availableTops.add(item);
            if (item.getCategory().equalsIgnoreCase("Низ")) availableBottoms.add(item);
            if (item.getCategory().equalsIgnoreCase("Верхняя одежда")) availableOuterwear.add(item);
            if (item.getCategory().equalsIgnoreCase("Обувь")) availableShoes.add(item);
            // TODO: Добавить другие категории (Головной убор, Аксессуары)
        }

        Random random = new Random();
        if (!availableTops.isEmpty()) top = availableTops.get(random.nextInt(availableTops.size()));
        if (!availableBottoms.isEmpty()) bottom = availableBottoms.get(random.nextInt(availableBottoms.size()));
        if (!availableOuterwear.isEmpty()) outerwear = availableOuterwear.get(random.nextInt(availableOuterwear.size()));
        if (!availableShoes.isEmpty()) shoes = availableShoes.get(random.nextInt(availableShoes.size()));

        // Отображаем просто названиями (для начала)
        if (outerwear != null) addOutfitItemToView(outerwear.getName() + " (" + outerwear.getCategory() + ")");
        if (top != null) addOutfitItemToView(top.getName() + " (" + top.getCategory() + ")");
        if (bottom != null) addOutfitItemToView(bottom.getName() + " (" + bottom.getCategory() + ")");
        if (shoes != null) addOutfitItemToView(shoes.getName() + " (" + shoes.getCategory() + ")");

        // TODO: Более сложная логика отображения на манекене с использованием item.getImageResourceName()
        // Например, можно иметь несколько ImageView для слоев одежды на манекене и устанавливать им src.
        // Glide.with(this).load(getResources().getIdentifier(selectedTop.getImageResourceName(), "drawable", getPackageName())).into(mannequinTopLayerImageView);
    }

    private void addOutfitItemToView(String itemName) {
        if (suggestedOutfitContainer != null) {
            TextView textView = new TextView(this);
            textView.setText(itemName);
            textView.setTextSize(16); // sp
            textView.setPadding(0, 4, 0, 4); // dp
            suggestedOutfitContainer.addView(textView);
        }
    }


    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up");
        if (btnBackToWeeklyDetail != null) {
            btnBackToWeeklyDetail.setOnClickListener(v -> finish()); // Просто закрываем, возвращаясь к списку
        }
        if (fabEditOutfit != null) {
            fabEditOutfit.setOnClickListener(v -> Toast.makeText(this, "Редактировать образ (TODO)", Toast.LENGTH_SHORT).show());
        }
        if (fabAddFavorite != null) {
            fabAddFavorite.setOnClickListener(v -> Toast.makeText(this, "Добавить в избранное (TODO)", Toast.LENGTH_SHORT).show());
        }
        if (fabRefresh != null) {
            fabRefresh.setOnClickListener(v -> {
                Log.d(TAG, "Refresh clicked, refetching weather for: " + currentCity);
                if (currentCity != null && !currentCity.isEmpty()) {
                    retrieveUserIdAndLoadUser(); // Это перезагрузит и пользователя и одежду
                } else {
                    Toast.makeText(this, "Город не определен для обновления", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Обработчики для кастомной нижней навигации
        if (navProfileIcon != null) {
            navProfileIcon.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // Привести существующий экземпляр наверх
                startActivity(intent);
                // finish(); // Возможно, не нужно закрывать, чтобы пользователь мог вернуться
            });
        }
        if (navHomeIcon != null) {
            // TODO: Определить, куда ведет "Дом" (WeatherForecastActivity или другой главный экран)
            Toast.makeText(this, "Переход на главный экран (TODO)", Toast.LENGTH_SHORT).show();
        }
        if (navFavoritesIcon != null) {
            // TODO: Переход на экран избранного
            Toast.makeText(this, "Переход в избранное (TODO)", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}