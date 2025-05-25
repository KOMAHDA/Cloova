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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    public static final String EXTRA_USER_STYLE_FOR_OUTFIT = "EXTRA_USER_STYLE_FOR_OUTFIT";

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
    private String outfitPreferredStyle;

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

            this.outfitPreferredStyle = intent.getStringExtra(EXTRA_USER_STYLE_FOR_OUTFIT);
            if (this.outfitPreferredStyle == null || this.outfitPreferredStyle.isEmpty()) {
                this.outfitPreferredStyle = "Повседневный"; // Стиль по умолчанию
                Log.w(TAG, "retrieveIntentData: Outfit style not found in Intent, using default: " + this.outfitPreferredStyle);
            }
            Log.d(TAG, "retrieveIntentData: Outfit preferred style: " + this.outfitPreferredStyle);

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

    private List<String> mapApiWeatherToDbConditions(@Nullable String apiDescription, int apiCode) {
        List<String> dbConditions = new ArrayList<>();
        Log.d(TAG, "mapApiWeatherToDbConditions: Mapping API desc='" + apiDescription + "', code=" + apiCode);

        // Приоритет отдаем коду, так как он более однозначен
        // Коды и описания из: https://www.weatherapi.com/docs/weather_conditions.json
        switch (apiCode) {
            case 1000: // Sunny / Clear
                dbConditions.add("Солнечно");
                break;
            case 1003: // Partly cloudy
                dbConditions.add("Переменная облачность");
                // Можно также добавить "Облачно", если это уместно для вашей логики одежды
                // dbConditions.add("Облачно");
                break;
            case 1006: // Cloudy
            case 1009: // Overcast
                dbConditions.add("Облачно");
                break;
            case 1030: // Mist
            case 1135: // Fog
            case 1147: // Freezing fog
                dbConditions.add("Туман");
                break;
            case 1063: // Patchy rain possible
            case 1150: // Patchy light drizzle
            case 1153: // Light drizzle
            case 1180: // Patchy light rain
            case 1183: // Light rain
                dbConditions.add("Небольшой дождь");
                // Также может считаться просто "Дождь"
                if (!dbConditions.contains("Дождь")) dbConditions.add("Дождь");
                break;
            case 1066: // Patchy snow possible
            case 1210: // Patchy light snow
            case 1213: // Light snow
            case 1255: // Light snow showers
                dbConditions.add("Снег"); // Для простоты, все снегопады пока как "Снег"
                break;
            case 1069: // Patchy sleet possible (Мокрый снег)
            case 1204: // Light sleet
            case 1207: // Moderate or heavy sleet
            case 1249: // Light sleet showers
            case 1252: // Moderate or heavy sleet showers
                dbConditions.add("Дождь"); // Мокрый снег - это и дождь, и снег
                dbConditions.add("Снег");
                break;
            case 1072: // Patchy freezing drizzle possible
            case 1168: // Freezing drizzle
            case 1171: // Heavy freezing drizzle (Ледяной дождь)
                dbConditions.add("Дождь"); // Ледяной дождь - это форма дождя
                // Можно добавить специфическое условие "Ледяной дождь", если оно есть в вашем каталоге
                break;
            case 1087: // Thundery outbreaks possible (Возможны грозы)
            case 1273: // Patchy light rain with thunder
            case 1276: // Moderate or heavy rain with thunder
                dbConditions.add("Дождь"); // Гроза обычно с дождем
                // dbConditions.add("Гроза"); // Если есть такое условие в каталоге
                break;
            case 1114: // Blowing snow
            case 1117: // Blizzard (Метель)
                dbConditions.add("Снег");
                dbConditions.add("Ветрено");
                break;
            case 1216: // Patchy moderate snow
            case 1219: // Moderate snow
            case 1222: // Patchy heavy snow
            case 1225: // Heavy snow
            case 1258: // Moderate or heavy snow showers
                dbConditions.add("Снег");
                break;
            case 1186: // Moderate rain at times
            case 1189: // Moderate rain
            case 1192: // Heavy rain at times
            case 1195: // Heavy rain
            case 1240: // Light rain shower
            case 1243: // Moderate or heavy rain shower
            case 1246: // Torrential rain shower
                dbConditions.add("Дождь");
                break;
            case 1279: // Patchy light snow with thunder
            case 1282: // Moderate or heavy snow with thunder
                dbConditions.add("Снег");
                // dbConditions.add("Гроза");
                break;
            // Добавьте другие коды по необходимости
        }

        // Если по коду ничего не определили или хотим дополнить по тексту (менее надежно)
        if (apiDescription != null && dbConditions.isEmpty()) { // Дополняем, только если по коду ничего не нашлось
            String descLower = apiDescription.toLowerCase();
            if (descLower.contains("солн") || descLower.contains("ясно") && !dbConditions.contains("Солнечно")) dbConditions.add("Солнечно");
            if (descLower.contains("облач") && !dbConditions.contains("Облачно") && !dbConditions.contains("Переменная облачность")) dbConditions.add("Облачно");
            if (descLower.contains("перемен") && descLower.contains("облач") && !dbConditions.contains("Переменная облачность")) dbConditions.add("Переменная облачность");
            if (descLower.contains("дожд") && !dbConditions.contains("Дождь") && !dbConditions.contains("Небольшой дождь")) dbConditions.add("Дождь");
            if (descLower.contains("снег") && !dbConditions.contains("Снег")) dbConditions.add("Снег");
            if (descLower.contains("ветер") || descLower.contains("ветрен") && !dbConditions.contains("Ветрено")) dbConditions.add("Ветрено");
            if (descLower.contains("туман") && !dbConditions.contains("Туман")) dbConditions.add("Туман");
        }

        // Если после всех попыток список пуст, добавляем что-то по умолчанию
        if (dbConditions.isEmpty()) {
            Log.w(TAG, "mapApiWeatherToDbConditions: No specific conditions mapped, using default 'Переменная облачность'");
            dbConditions.add("Переменная облачность");
        }

        Log.d(TAG, "mapApiWeatherToDbConditions: Mapped DB conditions: " + dbConditions.toString());
        return dbConditions;
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadUserAndOutfitTask extends AsyncTask<Long, Void, UserOutfitData> {
        @Override
        protected UserOutfitData doInBackground(Long... userIds) {
            long userId = userIds[0];
            User user = dbHelper.getUserInfo(userId);

            if (user != null && user.getGender() != null && !user.getGender().isEmpty()) {
                // Стиль и погодные условия нам все еще нужны для предпочтений
                String styleForOutfit = DayDetailActivity.this.outfitPreferredStyle;

                List<String> conditionNamesForDB = mapApiWeatherToDbConditions(weatherDescription, weatherCode); // Используем обновленный маппер

                Log.d(TAG, "Calling getSuggestedOutfit (wider search) with:");
                Log.d(TAG, "  Temperature (for range): " + ((int) Math.round(DayDetailActivity.this.maxTemp)));
                Log.d(TAG, "  User Gender: " + user.getGender());

                // Вызываем getSuggestedOutfit только с температурой и полом
                List<ClothingItem> allPossibleItems = dbHelper.getSuggestedOutfit(
                        (int) Math.round(DayDetailActivity.this.maxTemp),
                        user.getGender()
                );

                return new UserOutfitData(user, allPossibleItems, DayDetailActivity.this.outfitPreferredStyle, conditionNamesForDB);

            }
            return new UserOutfitData(user, null, DayDetailActivity.this.outfitPreferredStyle, new ArrayList<>());
        }

        @Override
        protected void onPostExecute(@Nullable UserOutfitData result) {
            if (result != null && result.user != null) {
                currentUser = result.user;
                Log.d(TAG, "User data loaded: " + currentUser.getName());
                if (result.allPossibleItems != null) {
                    Log.d(TAG, "All possible outfit items loaded: " + result.allPossibleItems.size());
                    // Передаем все необходимые данные в displaySuggestedOutfit
                    displaySuggestedOutfit(result.allPossibleItems, maxTemp, result.preferredStyle, result.weatherConditions);
                } else {
                    Log.w(TAG, "All possible items list is null.");
                    tvOutfitSuggestionDetail.setText("Не удалось получить список одежды.");
                }
            } else {
                Log.e(TAG, "Failed to load user data.");
                tvOutfitSuggestionDetail.setText("Ошибка загрузки данных пользователя.");
            }
        }
    }

    private static class UserOutfitData {
        User user;
        List<ClothingItem> allPossibleItems;
        String preferredStyle;
        List<String> weatherConditions;

        UserOutfitData(User user, List<ClothingItem> items, String style, List<String> conditions) {
            this.user = user;
            this.allPossibleItems = items;
            this.preferredStyle = style;
            this.weatherConditions = conditions;
        }
    }

    private void displaySuggestedOutfit(List<ClothingItem> allPossibleItems, double currentTemperature,
                                        String preferredStyle, List<String> currentDbWeatherConditions) {
        Log.d(TAG, "displaySuggestedOutfit: Processing " + allPossibleItems.size() + " possible items. PrefStyle: " + preferredStyle + ", Conditions: " + currentDbWeatherConditions);
        if (suggestedOutfitContainer != null) {
            suggestedOutfitContainer.removeAllViews();
        }

        if (allPossibleItems.isEmpty()) {
            tvOutfitSuggestionDetail.setText(getString(R.string.outfit_no_suitable_clothes));
            ivMannequin.setVisibility(View.GONE);
            return;
        }
        ivMannequin.setVisibility(View.VISIBLE);

        Map<String, List<ClothingItem>> itemsByCategory = new HashMap<>();
        for (ClothingItem item : allPossibleItems) {
            itemsByCategory.computeIfAbsent(item.getCategory().toLowerCase(), k -> new ArrayList<>()).add(item);
        }

        // --- Логика выбора с приоритетами и "откатами" ---
        ClothingItem selectedTop = findBestMatch(itemsByCategory.get("верх"), preferredStyle, currentDbWeatherConditions, currentTemperature);
        ClothingItem selectedBottom = findBestMatch(itemsByCategory.get("низ"), preferredStyle, currentDbWeatherConditions, currentTemperature);
        ClothingItem selectedShoes = findBestMatch(itemsByCategory.get("обувь"), preferredStyle, currentDbWeatherConditions, currentTemperature);

        ClothingItem selectedOuterwear = null;
        boolean needsOuterwear = currentTemperature < 15 || containsRainOrSnow(currentDbWeatherConditions);
        if (needsOuterwear) {
            selectedOuterwear = findBestMatch(itemsByCategory.get("верхняя одежда"), preferredStyle, currentDbWeatherConditions, currentTemperature);
        }

        ClothingItem selectedHeadwear = null;
        boolean needsHeadwearForSun = currentTemperature > 20 && currentDbWeatherConditions.contains("Солнечно");
        boolean needsHeadwearForCold = currentTemperature < 5 || containsRainOrSnow(currentDbWeatherConditions) || currentDbWeatherConditions.contains("Ветрено");
        if (needsHeadwearForSun || needsHeadwearForCold) {
            selectedHeadwear = findBestMatch(itemsByCategory.get("головной убор"), preferredStyle, currentDbWeatherConditions, currentTemperature);
        }

        // Специальная логика для платьев/юбок
        if (selectedTop == null && currentUser != null && "Женский".equalsIgnoreCase(currentUser.getGender()) && itemsByCategory.containsKey("платья/юбки")) {
            ClothingItem dressOrSkirt = findBestMatch(itemsByCategory.get("платья/юбки"), preferredStyle, currentDbWeatherConditions, currentTemperature);
            if (dressOrSkirt != null) {
                selectedTop = dressOrSkirt; // Считаем платье/юбку за "верх" в данном контексте
                selectedBottom = null;      // Низ не нужен с платьем/длинной юбкой
            }
        }

        // --- Если для обязательных категорий ничего не найдено, пробуем найти хоть что-то ---
        if (selectedTop == null && itemsByCategory.get("верх") != null) {
            Log.w(TAG, "No ideal 'Top' found, picking any suitable by temp.");
            selectedTop = chooseBestItemForCategory(itemsByCategory.get("верх"), currentTemperature); // Откат к выбору по температуре
        }
        if (selectedBottom == null && itemsByCategory.get("низ") != null && selectedTop != null && !selectedTop.getCategory().equalsIgnoreCase("платья/юбки")) {
            Log.w(TAG, "No ideal 'Bottom' found, picking any suitable by temp.");
            selectedBottom = chooseBestItemForCategory(itemsByCategory.get("низ"), currentTemperature);
        }
        if (selectedShoes == null && itemsByCategory.get("обувь") != null) {
            Log.w(TAG, "No ideal 'Shoes' found, picking any suitable by temp.");
            selectedShoes = chooseBestItemForCategory(itemsByCategory.get("обувь"), currentTemperature);
        }


        // --- Формируем текстовое описание образа ---
        // ... (ваш код формирования StringBuilder, он остается таким же) ...
        StringBuilder outfitTextBuilder = new StringBuilder("Рекомендуемый образ:\n");
        int itemCount = 0;

        if (selectedOuterwear != null) {
            outfitTextBuilder.append("🧥 ").append(selectedOuterwear.getName()).append("\n");
            itemCount++;
        }
        if (selectedTop != null) { // Может быть платьем
            outfitTextBuilder.append(selectedTop.getCategory().equalsIgnoreCase("платья/юбки") ? "👗 " : "👕 ")
                    .append(selectedTop.getName()).append("\n");
            itemCount++;
        }
        if (selectedBottom != null) {
            outfitTextBuilder.append("👖 ").append(selectedBottom.getName()).append("\n");
            itemCount++;
        }
        if (selectedShoes != null) {
            outfitTextBuilder.append("👟 ").append(selectedShoes.getName()).append("\n");
            itemCount++;
        }
        if (selectedHeadwear != null) {
            outfitTextBuilder.append("🧢 ").append(selectedHeadwear.getName()).append("\n");
            itemCount++;
        }

        if (itemCount >= 3) { // Показываем, если есть хотя бы 3 основных элемента (верх, низ, обувь или платье, обувь)
            if (outfitTextBuilder.length() > 0 && outfitTextBuilder.charAt(outfitTextBuilder.length() - 1) == '\n') {
                outfitTextBuilder.setLength(outfitTextBuilder.length() - 1);
            }
            tvOutfitSuggestionDetail.setText(outfitTextBuilder.toString());
        } else {
            tvOutfitSuggestionDetail.setText(getString(R.string.outfit_no_complete_set_fallback)); // Новая строка
        }
    }

    // Вспомогательный метод для проверки наличия дождя или снега
    private boolean containsRainOrSnow(List<String> conditions) {
        if (conditions == null) return false;
        for (String cond : conditions) {
            if (cond.equalsIgnoreCase("Дождь") || cond.equalsIgnoreCase("Небольшой дождь") || cond.equalsIgnoreCase("Снег")) {
                return true;
            }
        }
        return false;
    }

    // Новый метод для выбора лучшего совпадения с учетом стиля и погоды,
// с откатом к выбору только по температуре, если идеального нет.
    @Nullable
    private ClothingItem findBestMatch(@Nullable List<ClothingItem> itemsInCategory, // Все вещи этой категории, подходящие по t и полу
                                       String preferredStyle,
                                       List<String> currentDbWeatherConditions,
                                       double currentTemperature) {
        Log.d(TAG, "findBestMatch: Called for category (implicit from itemsInCategory).");
        Log.d(TAG, "findBestMatch: Preferred Style: '" + preferredStyle + "'");
        Log.d(TAG, "findBestMatch: Weather Conditions: " + currentDbWeatherConditions.toString());
        Log.d(TAG, "findBestMatch: Current Temperature: " + currentTemperature + "°C");

        if (itemsInCategory == null || itemsInCategory.isEmpty()) {
            return null;
        }

        List<ClothingItem> perfectMatch = new ArrayList<>();
        List<ClothingItem> styleMatch = new ArrayList<>();
        List<ClothingItem> weatherMatch = new ArrayList<>();

        long preferredStyleId = dbHelper.getStyleIdByName(preferredStyle); // Вызываем public метод
        List<Long> conditionIds = new ArrayList<>();
        for (String condName : currentDbWeatherConditions) {
            long id = dbHelper.getWeatherConditionIdByName(condName); // Вызываем public метод
            if (id != -1) conditionIds.add(id);
        }

        for (ClothingItem item : itemsInCategory) {
            boolean itemMatchesStyle = false;
            if (preferredStyleId != -1) {
                List<Long> itemStyleIds = dbHelper.getStylesForClothingItem(item.getClothingId());
                if (itemStyleIds.contains(preferredStyleId)) {
                    itemMatchesStyle = true;
                }
            } else {
                itemMatchesStyle = true; // Если стиль не указан, считаем, что любой подходит по стилю
            }

            boolean itemMatchesWeather = false;
            if (!conditionIds.isEmpty()) {
                List<Long> itemConditionIds = dbHelper.getConditionsForClothingItem(item.getClothingId());
                for (long condId : conditionIds) {
                    if (itemConditionIds.contains(condId)) {
                        itemMatchesWeather = true;
                        break;
                    }
                }
            } else {
                itemMatchesWeather = true; // Если условия не указаны, считаем, что любые подходят по погоде
            }

            if (itemMatchesStyle && itemMatchesWeather) {
                perfectMatch.add(item);
            } else if (itemMatchesStyle) {
                styleMatch.add(item);
            } else if (itemMatchesWeather) {
                weatherMatch.add(item);
            }
        }

        Random random = new Random();
        if (!perfectMatch.isEmpty()) {
            Log.d(TAG, "findBestMatch: Found " + perfectMatch.size() + " perfect matches.");
            return perfectMatch.get(random.nextInt(perfectMatch.size()));
        }
        if (!styleMatch.isEmpty()) {
            Log.d(TAG, "findBestMatch: Found " + styleMatch.size() + " style-only matches.");
            return styleMatch.get(random.nextInt(styleMatch.size()));
        }
        if (!weatherMatch.isEmpty()) {
            Log.d(TAG, "findBestMatch: Found " + weatherMatch.size() + " weather-only matches.");
            return weatherMatch.get(random.nextInt(weatherMatch.size()));
        }
        // Если ничего не подошло по стилю/погоде, но вещь подходит по температуре и полу (пришла в itemsInCategory)
        Log.d(TAG, "findBestMatch: No specific matches, returning random from category (" + itemsInCategory.size() + " items).");
        return itemsInCategory.get(random.nextInt(itemsInCategory.size()));
    }

    // Метод chooseBestItemForCategory теперь просто выбирает случайный,
// основная логика в findBestMatch
    @Nullable
    private ClothingItem chooseBestItemForCategory(@Nullable List<ClothingItem> items, double currentTemperature) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items.get(new Random().nextInt(items.size()));
    }

    @Nullable
    private ClothingItem chooseRandomItem(@Nullable List<ClothingItem> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items.get(new Random().nextInt(items.size()));
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