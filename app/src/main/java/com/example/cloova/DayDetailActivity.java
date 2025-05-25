package com.example.cloova; // Убедитесь, что пакет правильный

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
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
import java.util.LinkedHashMap;
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
    private TextView tvNoOutfitMessage;

    private ImageView ivOutfitOuterwear;
    private ImageView ivOutfitTop;
    private ImageView ivOutfitBottom;
    private ImageView ivOutfitShoes;

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

    private ClothingItem lastSelectedOuterwear;
    private ClothingItem lastSelectedTop;
    private ClothingItem lastSelectedBottom;
    private ClothingItem lastSelectedShoes;


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
        tvNoOutfitMessage = findViewById(R.id.tv_no_outfit_message);

        ivMannequin = findViewById(R.id.iv_mannequin);

        ivOutfitOuterwear = findViewById(R.id.iv_outfit_outerwear);
        ivOutfitTop = findViewById(R.id.iv_outfit_top);
        ivOutfitBottom = findViewById(R.id.iv_outfit_bottom);
        ivOutfitShoes = findViewById(R.id.iv_outfit_shoes);

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
                String styleForOutfit = DayDetailActivity.this.outfitPreferredStyle;
                List<String> conditionNamesForDB = mapApiWeatherToDbConditions(weatherDescription, weatherCode);

                Log.d(TAG, "Calling getSuggestedOutfit (wider search) with:");
                Log.d(TAG, "  Temperature (for range): " + ((int) Math.round(DayDetailActivity.this.maxTemp)));
                Log.d(TAG, "  User Gender: " + user.getGender());

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
                    displaySuggestedOutfit(result.allPossibleItems, maxTemp, result.preferredStyle, result.weatherConditions);
                } else {
                    Log.w(TAG, "All possible items list is null. Showing no outfit message.");
                    tvOutfitSuggestionDetail.setVisibility(View.GONE); // Скрыть текстовый список, если нет вещей
                    tvNoOutfitMessage.setVisibility(View.VISIBLE); // Показать сообщение об отсутствии образа
                    ivMannequin.setVisibility(View.GONE); // Скрыть манекен
                }
            } else {
                Log.e(TAG, "Failed to load user data. Showing error message.");
                tvOutfitSuggestionDetail.setVisibility(View.GONE); // Скрыть текстовый список
                tvNoOutfitMessage.setText(getString(R.string.error_loading_user_data)); // Сообщение об ошибке загрузки пользователя
                tvNoOutfitMessage.setVisibility(View.VISIBLE); // Показать сообщение об ошибке
                ivMannequin.setVisibility(View.GONE); // Скрыть манекен
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

        // --- Сбрасываем видимость всех элементов перед новым подбором ---
        ivOutfitOuterwear.setVisibility(View.GONE);
        ivOutfitTop.setVisibility(View.GONE);
        ivOutfitBottom.setVisibility(View.GONE);
        ivOutfitShoes.setVisibility(View.GONE);
        tvOutfitSuggestionDetail.setVisibility(View.GONE); // Скрываем текстовый список
        tvNoOutfitMessage.setVisibility(View.GONE); // Скрываем сообщение об отсутствии образа
        ivMannequin.setVisibility(View.VISIBLE); // Показываем манекен по умолчанию

        boolean isRainyOrSnowy = containsRainOrSnow(currentDbWeatherConditions);
        boolean isWindy = currentDbWeatherConditions.contains("Ветрено");

        if (allPossibleItems.isEmpty()) {
            tvOutfitSuggestionDetail.setVisibility(View.GONE); // Скрываем текстовый список
            tvNoOutfitMessage.setVisibility(View.VISIBLE); // Показываем сообщение об отсутствии образа
            ivMannequin.setVisibility(View.GONE); // Скрываем манекен
            Log.d(TAG, "No possible items found. Showing no outfit message.");
            return;
        }

        Map<String, List<ClothingItem>> itemsByCategory = new HashMap<>();
        for (ClothingItem item : allPossibleItems) {
            itemsByCategory.computeIfAbsent(item.getCategory().toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(item);
        }

        // --- Логика подбора (без изменений) ---
        ClothingItem selectedTop = findBestMatch(itemsByCategory.get("верх"), preferredStyle, currentDbWeatherConditions, currentTemperature);
        ClothingItem selectedBottom = findBestMatch(itemsByCategory.get("низ"), preferredStyle, currentDbWeatherConditions, currentTemperature);
        ClothingItem selectedShoes = findBestMatch(itemsByCategory.get("обувь"), preferredStyle, currentDbWeatherConditions, currentTemperature);

        ClothingItem selectedOuterwear = null;
        boolean needsOuterwear = currentTemperature < 15 || isRainyOrSnowy || isWindy;
        if (needsOuterwear) {
            selectedOuterwear = findBestMatch(itemsByCategory.get("верхняя одежда"), preferredStyle, currentDbWeatherConditions, currentTemperature);
        }

        this.lastSelectedOuterwear = selectedOuterwear;
        this.lastSelectedTop = selectedTop;
        this.lastSelectedBottom = selectedBottom;
        this.lastSelectedShoes = selectedShoes;

        // Специальная логика для платьев/юбок
        if (selectedTop == null && currentUser != null && "Женский".equalsIgnoreCase(currentUser.getGender()) && itemsByCategory.containsKey("платья/юбки")) {
            ClothingItem dressOrSkirt = findBestMatch(itemsByCategory.get("платья/юбки"), preferredStyle, currentDbWeatherConditions, currentTemperature);
            if (dressOrSkirt != null) {
                selectedTop = dressOrSkirt;
                selectedBottom = null;
                Log.d(TAG, "Selected dress/skirt: " + dressOrSkirt.getName());
            }
        }

        // --- Устанавливаем изображения в ImageView ---
        int itemsVisuallyDisplayed = 0; // Считаем, сколько реально элементов отображено на манекене

        if (selectedOuterwear != null) {
            setOutfitImage(ivOutfitOuterwear, selectedOuterwear.getImageResourceName());
            itemsVisuallyDisplayed++;
        }
        if (selectedTop != null) {
            setOutfitImage(ivOutfitTop, selectedTop.getImageResourceName());
            itemsVisuallyDisplayed++;
        }
        if (selectedBottom != null) {
            setOutfitImage(ivOutfitBottom, selectedBottom.getImageResourceName());
            itemsVisuallyDisplayed++;
        }
        if (selectedShoes != null) {
            setOutfitImage(ivOutfitShoes, selectedShoes.getImageResourceName());
            itemsVisuallyDisplayed++;
        }

        // --- Формируем и отображаем ТЕКСТОВОЕ описание образа ---
        StringBuilder outfitTextBuilder = new StringBuilder(getString(R.string.recommended_outfit_title) + "\n");
        int itemsInTextList = 0; // Считаем, сколько элементов в текстовом списке

        if (selectedOuterwear != null) {
            outfitTextBuilder.append("🧥 ").append(selectedOuterwear.getName()).append("\n");
            itemsInTextList++;
        }
        if (selectedTop != null) { // Может быть платьем
            outfitTextBuilder.append(selectedTop.getCategory().equalsIgnoreCase("платья/юбки") ? "👗 " : "👕 ")
                    .append(selectedTop.getName()).append("\n");
            itemsInTextList++;
        }
        if (selectedBottom != null) {
            outfitTextBuilder.append("👖 ").append(selectedBottom.getName()).append("\n");
            itemsInTextList++;
        }
        if (selectedShoes != null) {
            outfitTextBuilder.append("👟 ").append(selectedShoes.getName()).append("\n");
            itemsInTextList++;
        }



        // --- Логика отображения/скрытия на основе количества подобранных элементов ---
        if (itemsVisuallyDisplayed > 0) { // Если хоть что-то подобралось визуально
            tvOutfitSuggestionDetail.setText(outfitTextBuilder.toString());
            tvOutfitSuggestionDetail.setVisibility(View.VISIBLE); // Показываем текстовый список
            tvNoOutfitMessage.setVisibility(View.GONE); // Скрываем сообщение об отсутствии
            ivMannequin.setVisibility(View.VISIBLE); // Показываем манекен (уже должен быть виден)
            Log.d(TAG, "Successfully displayed " + itemsVisuallyDisplayed + " outfit items visually and " + itemsInTextList + " textually.");
        } else { // Если не удалось подобрать ничего
            tvOutfitSuggestionDetail.setVisibility(View.GONE); // Скрываем текстовый список
            tvNoOutfitMessage.setVisibility(View.VISIBLE); // Показываем сообщение об отсутствии образа
            ivMannequin.setVisibility(View.GONE); // Скрываем манекен
            Log.d(TAG, "No suitable outfit found. Displaying fallback message.");
        }
    }

    private void setOutfitImage(ImageView imageView, String imageResourceName) {
        if (imageView == null || imageResourceName == null || imageResourceName.isEmpty()) {
            Log.w(TAG, "setOutfitImage: ImageView is null or imageResourceName is empty.");
            return;
        }
        int resId = getResources().getIdentifier(imageResourceName, "drawable", getPackageName());
        if (resId != 0) {
            imageView.setImageResource(resId);
            imageView.setVisibility(View.VISIBLE);
            Log.d(TAG, "setOutfitImage: Set '" + imageResourceName + "' to " + imageView.getId());
        } else {
            Log.e(TAG, "setOutfitImage: Resource not found for name: " + imageResourceName + ". Setting visibility to GONE.");
            imageView.setVisibility(View.GONE);
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


    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up");
        if (btnBackToWeeklyDetail != null) {
            btnBackToWeeklyDetail.setOnClickListener(v -> finish()); // Просто закрываем, возвращаясь к списку
        }
        if (fabEditOutfit != null) {
            fabEditOutfit.setOnClickListener(v -> Toast.makeText(this, "Редактировать образ (TODO)", Toast.LENGTH_SHORT).show());
        }
        if (fabAddFavorite != null) {
            fabAddFavorite.setOnClickListener(v -> {
                Log.d(TAG, "Add to Favorites button clicked.");
                saveCurrentOutfit(); // Вызываем новый метод для сохранения
            });
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

    private void saveCurrentOutfit() {
        if (currentUserId == DatabaseHelper.DEFAULT_USER_ID || currentUser == null) {
            Toast.makeText(this, R.string.error_auth_save_outfit, Toast.LENGTH_SHORT).show(); // Добавьте эту строку в strings.xml
            Log.e(TAG, "saveCurrentOutfit: User not logged in or currentUser data missing.");
            return;
        }

        Map<String, ClothingItem> currentOutfitMap = new LinkedHashMap<>();
        if (lastSelectedOuterwear != null) currentOutfitMap.put("верхняя одежда", lastSelectedOuterwear);
        if (lastSelectedTop != null) currentOutfitMap.put(lastSelectedTop.getCategory().equalsIgnoreCase("платья/юбки") ? "платья/юбки" : "верх", lastSelectedTop);
        if (lastSelectedBottom != null) currentOutfitMap.put("низ", lastSelectedBottom);
        if (lastSelectedShoes != null) currentOutfitMap.put("обувь", lastSelectedShoes);

        if (currentOutfitMap.isEmpty()) {
            Toast.makeText(this, R.string.outfit_cannot_save_empty, Toast.LENGTH_SHORT).show(); // Добавьте эту строку
            Log.w(TAG, "saveCurrentOutfit: No items in the outfit to save.");
            return;
        }

        new SaveOutfitTask().execute(currentOutfitMap);
    }

    @SuppressLint("StaticFieldLeak")
    private class SaveOutfitTask extends AsyncTask<Map<String, ClothingItem>, Void, Long> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(DayDetailActivity.this, R.string.saving_outfit, Toast.LENGTH_SHORT).show(); // "Сохранение образа..."
        }

        @Override
        protected Long doInBackground(Map<String, ClothingItem>... params) {
            Map<String, ClothingItem> outfitItemsMap = params[0];
            // Используем текущие значения, которые были переданы в Intent или установлены ранее
            return dbHelper.saveOutfit(
                    currentUserId,
                    weatherDescription, // Используем weatherDescription
                    maxTemp,            // Используем maxTemp
                    outfitPreferredStyle, // Используем outfitPreferredStyle
                    outfitItemsMap
            );
        }

        @Override
        protected void onPostExecute(Long result) {
            if (result != -1) {
                Toast.makeText(DayDetailActivity.this, R.string.outfit_saved_successfully, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Outfit saved with ID: " + result);
            } else {
                Toast.makeText(DayDetailActivity.this, R.string.error_saving_outfit, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to save outfit.");
            }
        }
    }
}