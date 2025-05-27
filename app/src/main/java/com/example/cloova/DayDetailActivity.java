package com.example.cloova; // Убедитесь, что пакет правильный

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.app.DatePickerDialog;
import java.util.Calendar;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random; // Для простого выбора одежды из категории

public class DayDetailActivity extends AppCompatActivity {

    private static final String TAG = "DayDetailActivity";

    // Ключи для Intent Extras (как public static final в WeatherForecastActivity или Constants)
    public static final String EXTRA_DATE_STR = "EXTRA_DATE_STR"; // "YYYY-MM-DD"
    public static final String EXTRA_MAX_TEMP = "EXTRA_MAX_TEMP";
    public static final String EXTRA_MIN_TEMP = "EXTRA_MIN_TEMP";
    public static final String EXTRA_WIND_KPH = "EXTRA_WIND_KPH";
    public static final String EXTRA_HUMIDITY = "EXTRA_HUMIDITY";
    public static final String EXTRA_WEATHER_ICON_URL = "EXTRA_WEATHER_ICON_URL";
    public static final String EXTRA_WEATHER_DESCRIPTION = "EXTRA_WEATHER_DESCRIPTION";
    public static final String EXTRA_WEATHER_CODE = "EXTRA_WEATHER_CODE";
    public static final String EXTRA_CITY_NAME = "EXTRA_CITY_NAME";
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
    private FloatingActionButton fabAddFavorite, fabRefresh, fabPlanOutfit;

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
    private Random outfitRandomGenerator = new Random();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

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
        tvWindValueDetail = findViewById(R.id.tv_wind_value_detail);
        tvHumidityValueDetail = findViewById(R.id.tv_humidity_value_detail);
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
        fabPlanOutfit = findViewById(R.id.fab_plan_outfit_detail);

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

    private int getWeatherIconResourceByApiCode(int apiCode) {
        Log.d(TAG, "getWeatherIconResourceByApiCode: Mapping API code " + apiCode);
        switch (apiCode) {
            case 1000: // Sunny / Clear
                // добавить логику для дня/ночи, если у вас есть is_day флаг для дня
                // и разные иконки для ясного дня и ясной ночи.
                // Пока просто возвращаем одну "солнечную" иконку.
                return R.drawable.wb_sunny; // иконка для "Ясно/Солнечно"
            case 1003: // Partly cloudy
                return R.drawable.partly_cloudy; // иконка для "Переменная облачность"
            case 1006: // Cloudy
            case 1009: // Overcast
                return R.drawable.cloud; // иконка для "Облачно"
            case 1030: // Mist
            case 1135: // Fog
            case 1147: // Freezing fog
                return R.drawable.foggy; // иконка для "Туман/Дымка"
            case 1063: // Patchy rain possible
            case 1150: // Patchy light drizzle
            case 1153: // Light drizzle
            case 1180: // Patchy light rain
            case 1183: // Light rain
                return R.drawable.rainy; // иконка для "Небольшой дождь"
            case 1066: // Patchy snow possible
            case 1210: // Patchy light snow
            case 1213: // Light snow
                return R.drawable.weather_snowy_24dp_e3e3e3_fill0_wght400_grad0_opsz24; // иконка для "Небольшой снег"
            case 1069: // Patchy sleet possible
            case 1204: // Light sleet
            case 1207: // Moderate or heavy sleet
            case 1249: // Light sleet showers
            case 1252: // Moderate or heavy sleet showers
                return R.drawable.weather_mix; // иконка для "Дождь со снегом"
            case 1072: // Patchy freezing drizzle possible
            case 1168: // Freezing drizzle
            case 1171: // Heavy freezing drizzle
                return R.drawable.icerain; // иконка для "Ледяной дождь"
            case 1087: // Thundery outbreaks possible
            case 1273: // Patchy light rain with thunder
            case 1276: // Moderate or heavy rain with thunder
                return R.drawable.thunderstorm; // иконка для "Гроза"
            case 1114: // Blowing snow
            case 1117: // Blizzard
            case 1216: // Patchy moderate snow
            case 1219: // Moderate snow
            case 1222: // Patchy heavy snow
            case 1225: // Heavy snow
            case 1255: // Light snow showers
            case 1258: // Moderate or heavy snow showers
                return R.drawable.weather_snowy_24dp_e3e3e3_fill0_wght400_grad0_opsz24; // иконка для "Снег"
            case 1186: // Moderate rain at times
            case 1189: // Moderate rain
            case 1192: // Heavy rain at times
            case 1195: // Heavy rain
            case 1240: // Light rain shower
            case 1243: // Moderate or heavy rain shower
            case 1246: // Torrential rain shower
                return R.drawable.rainy; // иконка для "Дождь"
            case 1279: // Patchy light snow with thunder
            case 1282: // Moderate or heavy snow with thunder
                return R.drawable.weather_mix; // иконка "Снег с грозой"

            default:
                Log.w(TAG, "Unknown weather code: " + apiCode + ", using default icon.");
                return R.drawable.cloud; // иконка по умолчанию
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

        // ОТОБРАЖАЕМ ВЕТЕР И ВЛАЖНОСТЬ
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

        // https://www.weatherapi.com/docs/weather_conditions.json
        switch (apiCode) {
            case 1000: // Sunny / Clear
                dbConditions.add("Солнечно");
                break;
            case 1003: // Partly cloudy
                dbConditions.add("Переменная облачность");
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
                // Можно добавить специфическое условие "Ледяной дождь"
                break;
            case 1087: // Thundery outbreaks possible (Возможны грозы)
            case 1273: // Patchy light rain with thunder
            case 1276: // Moderate or heavy rain with thunder
                dbConditions.add("Дождь"); // Гроза обычно с дождем
                // dbConditions.add("Гроза");
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

        // Сбрасываем видимость всех элементов перед новым подбором
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

        List<Map<String, ClothingItem>> outfitCandidates = new ArrayList<>();
        int numAttempts = 5; // Попробуем сгенерировать 5 разных вариантов

        for (int i = 0; i < numAttempts; i++) {
            Log.d(TAG, "Generating outfit candidate #" + (i + 1));
            Random attemptSpecificRandom = new Random();
            ClothingItem currentCandidateTop = null;
            ClothingItem currentCandidateBottom = null;
            ClothingItem currentCandidateShoes = null;
            ClothingItem currentCandidateOuterwear = null;

            boolean isFemaleUser = currentUser != null && "Женский".equalsIgnoreCase(currentUser.getGender());

            // В1: Обычный комплект (Верх + Низ)
            ClothingItem candTopReg = findBestMatch(itemsByCategory.get("верх"), preferredStyle, currentDbWeatherConditions, currentTemperature);
            ClothingItem candBottomReg = (candTopReg != null) ? findBestMatch(itemsByCategory.get("низ"), preferredStyle, currentDbWeatherConditions, currentTemperature) : null;

            // В2: Платье/Юбка (только для женщин)
            ClothingItem candDressOrSkirt = null;
            if (isFemaleUser && itemsByCategory.containsKey("платья/юбки")) {
                candDressOrSkirt = findBestMatch(itemsByCategory.get("платья/юбки"), preferredStyle, currentDbWeatherConditions, currentTemperature);
            }

            // Выбор лучшего основного образа для этой попытки
            boolean chooseDressOrSkirtForThisAttempt = false;
            if (candDressOrSkirt != null && (candTopReg == null || candBottomReg == null)) {
                chooseDressOrSkirtForThisAttempt = true;
            } else if (candDressOrSkirt == null && candTopReg != null && candBottomReg != null) {
                chooseDressOrSkirtForThisAttempt = false;
            } else if (candDressOrSkirt != null && candTopReg != null && candBottomReg != null) {
                chooseDressOrSkirtForThisAttempt = new Random().nextBoolean(); // Случайный выбор
            } else {
                Log.d(TAG, "No complete primary outfit found for candidate #" + (i + 1));
            }

            if (chooseDressOrSkirtForThisAttempt) {
                currentCandidateTop = candDressOrSkirt;
                currentCandidateBottom = null;
            } else {
                currentCandidateTop = candTopReg;
                currentCandidateBottom = candBottomReg;
            }

            // Обувь, верхняя одежда, головной убор - подбираются отдельно для каждого кандидата
            currentCandidateShoes = findBestMatch(itemsByCategory.get("обувь"), preferredStyle, currentDbWeatherConditions, currentTemperature);
            boolean needsOuterwear = currentTemperature < 15 || containsRainOrSnow(currentDbWeatherConditions) || isWindy;
            if (needsOuterwear) {
                currentCandidateOuterwear = findBestMatch(itemsByCategory.get("верхняя одежда"), preferredStyle, currentDbWeatherConditions, currentTemperature);
            }

            // Собираем кандидата в Map
            Map<String, ClothingItem> candidateOutfit = new LinkedHashMap<>();
            if (currentCandidateOuterwear != null) candidateOutfit.put("верхняя одежда", currentCandidateOuterwear);
            if (currentCandidateTop != null) candidateOutfit.put(currentCandidateTop.getCategory().equalsIgnoreCase("платья/юбки") ? "платья/юбки" : "верх", currentCandidateTop);
            if (currentCandidateBottom != null) candidateOutfit.put("низ", currentCandidateBottom);
            if (currentCandidateShoes != null) candidateOutfit.put("обувь", currentCandidateShoes);

            // Добавляем кандидата, только если он не пустой (хотя бы 2-3 элемента)
            if (candidateOutfit.size() >= 2) { // Минимальное количество элементов для "образа"
                outfitCandidates.add(candidateOutfit);
                Log.d(TAG, "Added outfit candidate #" + outfitCandidates.size() + ". Items: " + candidateOutfit.keySet());
            } else {
                Log.d(TAG, "Outfit candidate #" + (i + 1) + " is too sparse. Skipping.");
            }
        }

        // ВЫБОР И ФИНАЛЬНОЕ ОТОБРАЖЕНИЕ ОДНОГО ИЗ КАНДИДАТОВ
        Map<String, ClothingItem> finalOutfit = null;
        if (!outfitCandidates.isEmpty()) {
            // Выбираем случайный образ из списка сгенерированных кандидатов
            finalOutfit = outfitCandidates.get(new Random().nextInt(outfitCandidates.size()));
            Log.d(TAG, "Final outfit chosen randomly from " + outfitCandidates.size() + " candidates.");

            // Извлекаем элементы для отображения из выбранного финального образа
            this.lastSelectedOuterwear = finalOutfit.get("верхняя одежда");
            this.lastSelectedTop = finalOutfit.get("верх"); // или "платья/юбки"
            if (this.lastSelectedTop == null && finalOutfit.containsKey("платья/юбки")) { // Если это платье/юбка
                this.lastSelectedTop = finalOutfit.get("платья/юбки");
            }
            this.lastSelectedBottom = finalOutfit.get("низ");
            this.lastSelectedShoes = finalOutfit.get("обувь");
            // this.lastSelectedHeadwear = finalOutfit.get("головной убор"); // Если есть головной убор

        } else {
            // Если ни один из кандидатов не был достаточно полным
            Log.w(TAG, "No suitable outfit candidates generated. Displaying no outfit message.");
            tvOutfitSuggestionDetail.setVisibility(View.GONE);
            tvNoOutfitMessage.setVisibility(View.VISIBLE);
            ivMannequin.setVisibility(View.GONE);
            return; // Выходим, так как ничего не подобрали
        }

        // Устанавливаем изображения в ImageView (используя finalOutfit)
        int itemsVisuallyDisplayed = 0;
        if (lastSelectedOuterwear != null) { setOutfitImage(ivOutfitOuterwear, lastSelectedOuterwear.getImageResourceName()); itemsVisuallyDisplayed++; }
        if (lastSelectedTop != null) { setOutfitImage(ivOutfitTop, lastSelectedTop.getImageResourceName()); itemsVisuallyDisplayed++; }
        if (lastSelectedBottom != null) { setOutfitImage(ivOutfitBottom, lastSelectedBottom.getImageResourceName()); itemsVisuallyDisplayed++; }
        if (lastSelectedShoes != null) { setOutfitImage(ivOutfitShoes, lastSelectedShoes.getImageResourceName()); itemsVisuallyDisplayed++; }
        // if (lastSelectedHeadwear != null) { setOutfitImage(ivOutfitHeadwear, lastSelectedHeadwear.getImageResourceName()); itemsVisuallyDisplayed++; } // Если есть головной убор


        // Формируем и отображаем ТЕКСТОВОЕ описание образа
        StringBuilder outfitTextBuilder = new StringBuilder(getString(R.string.recommended_outfit_title) + "\n");
        int itemsInTextList = 0;

        if (lastSelectedOuterwear != null) { outfitTextBuilder.append("🧥 ").append(lastSelectedOuterwear.getName()).append("\n"); itemsInTextList++; }
        if (lastSelectedTop != null) {
            outfitTextBuilder.append(lastSelectedTop.getCategory().equalsIgnoreCase("платья/юбки") ? "👗 " : "👕 ")
                    .append(lastSelectedTop.getName()).append("\n");
            itemsInTextList++;
        }
        if (lastSelectedBottom != null) { outfitTextBuilder.append("👖 ").append(lastSelectedBottom.getName()).append("\n"); itemsInTextList++; }
        if (lastSelectedShoes != null) { outfitTextBuilder.append("👟 ").append(lastSelectedShoes.getName()).append("\n"); itemsInTextList++; }

        if (itemsVisuallyDisplayed > 0) {
            tvOutfitSuggestionDetail.setText(outfitTextBuilder.toString());
            tvOutfitSuggestionDetail.setVisibility(View.VISIBLE);
            tvNoOutfitMessage.setVisibility(View.GONE);
            ivMannequin.setVisibility(View.VISIBLE);
            Log.d(TAG, "Successfully displayed " + itemsVisuallyDisplayed + " outfit items visually and " + itemsInTextList + " textually.");
        } else {
            tvOutfitSuggestionDetail.setVisibility(View.GONE);
            tvNoOutfitMessage.setVisibility(View.VISIBLE);
            ivMannequin.setVisibility(View.GONE);
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
    private ClothingItem findBestMatch(@Nullable List<ClothingItem> itemsInCategory,
                                       String preferredStyle,
                                       List<String> currentDbWeatherConditions,
                                       double currentTemperature) {

        Log.d(TAG, "findBestMatch: Called for category (implicit from itemsInCategory).");
        Log.d(TAG, "findBestMatch: Preferred Style: '" + preferredStyle + "'");
        Log.d(TAG, "findBestMatch: Weather Conditions: " + currentDbWeatherConditions.toString());
        Log.d(TAG, "findBestMatch: Current Temperature: " + currentTemperature + "°C");


        if (itemsInCategory == null || itemsInCategory.isEmpty()) {
            Log.d(TAG, "findBestMatch: No items in this category. Returning null.");
            return null;
        }

        List<ScoredClothingItem> scoredItems = new ArrayList<>();

        long preferredStyleId = dbHelper.getStyleIdByName(preferredStyle);
        Log.d(TAG, "findBestMatch: Resolved preferredStyleId for '" + preferredStyle + "': " + preferredStyleId);

        boolean isSpecificStyleChosen = !preferredStyle.equalsIgnoreCase("Повседневный") || preferredStyleId != -1;

        List<Long> conditionIds = new ArrayList<>();
        for (String condName : currentDbWeatherConditions) {
            long id = dbHelper.getWeatherConditionIdByName(condName);
            if (id != -1) conditionIds.add(id);
        }

        boolean isRainyOrSnowy = containsRainOrSnow(currentDbWeatherConditions);
        boolean isWindy = currentDbWeatherConditions.contains("Ветрено");
        Log.d(TAG, "findBestMatch: Is Rainy/Snowy? " + isRainyOrSnowy + ", Is Windy? " + isWindy);


        for (ClothingItem item : itemsInCategory) {
            double score = 0; // Базовый скор

            // 1. Оценка по соответствию стилю (строже)
            boolean itemMatchesStyle = false;
            if (preferredStyleId != -1) { // Если пользователь выбрал конкретный стиль
                List<Long> itemStyleIds = dbHelper.getStylesForClothingItem(item.getClothingId());
                itemMatchesStyle = itemStyleIds.contains(preferredStyleId);
                if (itemMatchesStyle) {
                    score += 1000; // ОЧЕНЬ высокий бонус за соответствие выбранному стилю
                    Log.d(TAG, "  Item '" + item.getName() + "': Style match! Score +1000");
                } else {
                    // Если стиль выбран, но предмет не соответствует - очень большой штраф (почти исключение)
                    score -= 10000; // ГИГАНТСКИЙ ШТРАФ за несоответствие ВЫБРАННОМУ стилю
                    Log.d(TAG, "  Item '" + item.getName() + "': DOES NOT match preferred style. Score -10000");
                }
            } else { // Если стиль не указан (preferredStyleId == -1) или дефолтный "Повседневный"
                itemMatchesStyle = true; // Считаем, что любой стиль подходит, но без бонуса
                // Здесь не даем бонус, т.к. нет специфичного стиля, но и не штрафуем
                Log.d(TAG, "  Item '" + item.getName() + "': No specific preferred style, assuming match.");
            }


            // 2. Оценка по соответствию погодным условиям (общие)
            boolean itemMatchesWeather = false;
            if (!conditionIds.isEmpty()) {
                List<Long> itemConditionIds = dbHelper.getConditionsForClothingItem(item.getClothingId());
                for (long condId : conditionIds) {
                    if (itemConditionIds.contains(condId)) {
                        itemMatchesWeather = true;
                        score += 80; // Хороший бонус
                        Log.d(TAG, "  Item '" + item.getName() + "': General weather match! Score +80");
                        break;
                    }
                }
                if (!itemMatchesWeather) { // Если нет соответствия НИ ОДНОМУ погодному условию
                    score -= 50; // Небольшой штраф
                    Log.d(TAG, "  Item '" + item.getName() + "': Does NOT match any weather conditions. Score -50");
                }
            } else {
                itemMatchesWeather = true; // Если условия не указаны, любые подходят
                score += 40; // Небольшой бонус
                Log.d(TAG, "  Item '" + item.getName() + "': No specific weather conditions set, assuming general weather match. Score +40");
            }

            // 3. Оценка по специальным требованиям (водонепроницаемость, ветрозащита)
            boolean itemMeetsWaterproofRequirement = true;
            if (isRainyOrSnowy && !item.isWaterproof()) {
                itemMeetsWaterproofRequirement = false;
                score -= 500; // Большой ШТРАФ (меньше чем за стиль)
                Log.d(TAG, "  Item '" + item.getName() + "': Fails waterproof requirement. Score -500");
            } else if (isRainyOrSnowy && item.isWaterproof()) {
                score += 50; // Бонус за водонепроницаемость, если она нужна
                Log.d(TAG, "  Item '" + item.getName() + "': Meets waterproof requirement. Score +50");
            }

            boolean itemMeetsWindproofRequirement = true;
            if (isWindy && !item.isWindproof()) {
                itemMeetsWindproofRequirement = false;
                score -= 400; // Большой ШТРАФ (меньше чем за стиль)
                Log.d(TAG, "  Item '" + item.getName() + "': Fails windproof requirement. Score -400");
            } else if (isWindy && item.isWindproof()) {
                score += 40; // Бонус за ветрозащиту, если она нужна
                Log.d(TAG, "  Item '" + item.getName() + "': Meets windproof requirement. Score +40");
            }

            // 4. Оценка по температурному соответствию (чем ближе к идеальному центру, тем лучше)
            double itemMidTemp = (item.getMinTemp() + item.getMaxTemp()) / 2.0;
            double tempDifference = Math.abs(itemMidTemp - currentTemperature);
            score -= tempDifference * 5; // Штраф за разницу
            Log.d(TAG, "  Item '" + item.getName() + "': Temp difference " + tempDifference + ". Score -" + (tempDifference * 5));

            // Если текущая температура находится за пределами диапазона вещи, даем очень большой штраф
            if (currentTemperature < item.getMinTemp() || currentTemperature > item.getMaxTemp()) {
                score -= 200; // Очень большой штраф за выход за пределы диапазона
                Log.d(TAG, "  Item '" + item.getName() + "': Current temp outside item's range. Score -200");
            }

            // Добавляем элемент со скором только если он не получил "смертельный" штраф за стиль
            if (score > -5000) { // Исключаем вещи, которые категорически не подходят по стилю
                scoredItems.add(new ScoredClothingItem(item, score));
                Log.d(TAG, "  Final score for '" + item.getName() + "': " + score + " (Added to candidates).");
            } else {
                Log.d(TAG, "  Final score for '" + item.getName() + "': " + score + " (Too low, excluded from candidates).");
            }
        }

        if (scoredItems.isEmpty()) {
            Log.d(TAG, "findBestMatch: No suitable item found in any category after scoring. Returning null.");
            return null;
        }

        // --- НОВАЯ ЛОГИКА ВЫБОРА: Рандом из "хороших" элементов ---

        // 1. Сортируем все элементы по убыванию скора
        Collections.sort(scoredItems, (s1, s2) -> Double.compare(s2.score, s1.score));

        Log.d(TAG, "findBestMatch: All scored items for this category (sorted):");
        for (ScoredClothingItem sci : scoredItems) {
            Log.d(TAG, "  - '" + sci.item.getName() + "': Score " + sci.score);
        }

        double maxScore = scoredItems.get(0).score;

        double scoreThreshold = maxScore * 0.7; // Если maxScore может быть отрицательным, нужна более сложная логика
        if (maxScore < 0) scoreThreshold = maxScore * 1.3; // Если лучший скор отрицательный, расширяем вниз.


        List<ClothingItem> eligibleItems = new ArrayList<>();
        for (ScoredClothingItem sci : scoredItems) {
            if (sci.score >= scoreThreshold) {
                eligibleItems.add(sci.item);
            } else {
                break;
            }
        }

        if (!eligibleItems.isEmpty()) { // Убедимся, что список не пуст ПЕРЕД тем, как брать из него элементы
            ClothingItem chosenItem = eligibleItems.get(new Random().nextInt(eligibleItems.size())); // <-- ЭТА СТРОКА
            Log.d(TAG, "findBestMatch: Randomly selected from " + eligibleItems.size() + " eligible items. Chosen: '" + chosenItem.getName() + "' (Score: " + scoredItems.get(scoredItems.indexOf(new ScoredClothingItem(chosenItem, 0))).score + ")");
            return chosenItem;
        } else {
            // Если eligibleItems пуст (даже лучший предмет не преодолел порог)
            Log.d(TAG, "findBestMatch: No truly suitable item found after applying score threshold (" + scoreThreshold + "). Returning null.");
            return null;
        }
    }


    private static class ScoredClothingItem {
        ClothingItem item;
        double score;

        ScoredClothingItem(ClothingItem item, double score) {
            this.item = item;
            this.score = score;
        }

        // Переопределяем equals() и hashCode() для корректного сравнения по 'item'
        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ScoredClothingItem that = (ScoredClothingItem) obj;
            return item.equals(that.item); // Сравниваем по самому ClothingItem
        }

        @Override
        public int hashCode() {
            return item.hashCode();
        }
    }



    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up");
        if (btnBackToWeeklyDetail != null) {
            btnBackToWeeklyDetail.setOnClickListener(v -> finish());
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

        if (fabPlanOutfit != null) {
            fabPlanOutfit.setOnClickListener(v -> {
                Log.d(TAG, "Plan Outfit button clicked.");
                showDatePickerDialog();
            });
        }

        // Обработчики для кастомной нижней навигации
        if (navProfileIcon != null) {
            navProfileIcon.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // Привести существующий экземпляр наверх
                startActivity(intent);
            });
        }
        if (navFavoritesIcon != null) {
            navFavoritesIcon.setOnClickListener(v -> {
                Intent intent = new Intent(this, Sohranenki.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            });
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

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Форматируем выбранную дату в YYYY-MM-DD для базы данных
                    String formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    Log.d(TAG, "Selected plan date: " + formattedDate);
                    planCurrentOutfit(formattedDate);
                }, year, month, day);

        // Опционально: можно ограничить выбор даты (например, только будущее)
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Только сегодняшняя и будущие даты

        datePickerDialog.show();
    }

    private void planCurrentOutfit(String planDate) {
        if (currentUserId == DatabaseHelper.DEFAULT_USER_ID || currentUser == null) {
            Toast.makeText(this, R.string.error_auth_plan_outfit, Toast.LENGTH_SHORT).show(); // Добавь эту строку в strings.xml
            Log.e(TAG, "planCurrentOutfit: User not logged in or currentUser data missing.");
            return;
        }

        Map<String, ClothingItem> currentOutfitMap = new LinkedHashMap<>();

        // Убедись, что lastSelectedOuterwear, lastSelectedTop и т.д. являются ПОЛЯМИ КЛАССА DayDetailActivity
        // и заполняются в displaySuggestedOutfit(). Это критически важно!
        if (lastSelectedOuterwear != null) currentOutfitMap.put("верхняя одежда", lastSelectedOuterwear);
        if (lastSelectedTop != null) currentOutfitMap.put(lastSelectedTop.getCategory().equalsIgnoreCase("платья/юбки") ? "платья/юбки" : "верх", lastSelectedTop);
        if (lastSelectedBottom != null) currentOutfitMap.put("низ", lastSelectedBottom);
        if (lastSelectedShoes != null) currentOutfitMap.put("обувь", lastSelectedShoes);

        if (currentOutfitMap.isEmpty()) {
            Toast.makeText(this, R.string.outfit_cannot_plan_empty, Toast.LENGTH_SHORT).show(); // Добавь эту строку
            Log.w(TAG, "planCurrentOutfit: No items in the outfit to plan.");
            return;
        }

        new PlanOutfitTask(planDate).execute(currentOutfitMap);
    }

    // AsyncTask для сохранения запланированного образа в фоновом потоке
    @SuppressLint("StaticFieldLeak")
    private class PlanOutfitTask extends AsyncTask<Map<String, ClothingItem>, Void, Long> {
        private String planDate;

        public PlanOutfitTask(String planDate) {
            this.planDate = planDate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(DayDetailActivity.this, R.string.planning_outfit, Toast.LENGTH_SHORT).show(); // "Планирование образа..."
        }

        @Override
        protected Long doInBackground(Map<String, ClothingItem>... params) {
            Map<String, ClothingItem> outfitItemsMap = params[0];
            // Используем текущие значения, которые были переданы в Intent или установлены ранее
            return dbHelper.savePlannedOutfit(
                    currentUserId,
                    planDate,
                    weatherDescription,
                    maxTemp,
                    outfitPreferredStyle,
                    outfitItemsMap
            );
        }

        @Override
        protected void onPostExecute(Long result) {
            if (result != -1) {
                Toast.makeText(DayDetailActivity.this, R.string.outfit_planned_successfully, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Planned outfit saved with ID: " + result);
            } else {
                Toast.makeText(DayDetailActivity.this, R.string.error_planning_outfit, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to plan outfit.");
            }
        }
    }
}