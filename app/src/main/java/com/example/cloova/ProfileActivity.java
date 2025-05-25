/*
package com.example.cloova;

import android.annotation.SuppressLint; // Добавлено для AsyncTask
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log; // Добавлено для логирования
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ProfileActivity"; // Тег для логов

    private DatabaseHelper dbHelper;
    private long userId = DatabaseHelper.DEFAULT_USER_ID;

    // --- UI элементы ---
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageView profileImage;
    private TextView profileUsername;
    private TextView infoName, infoSurname, infoDob, infoUsernameInCard, infoStyle;
    // --- Контейнеры для include-элементов ---
    private ConstraintLayout settingLocation, settingLanguage, settingQuestionnaire, settingSavedLooks, settingPlannedLooks;
    private TextView contactDevelopersLink;
    // --- Элементы заголовка Drawer ---
    private TextView drawerHeaderName;
    private TextView drawerHeaderLogin;
    private ImageView drawerHeaderImage;
    private User currentUser = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: Activity starting");

        // --- Инициализация Toolbar ---
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d(TAG, "onCreate: Toolbar initialized");

        // --- Инициализация Drawer ---
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        Log.d(TAG, "onCreate: Drawer and NavigationView initialized");

        // --- Связываем Drawer и Toolbar (создает иконку гамбургера) ---
        // Убедитесь, что у вас есть строки R.string.navigation_drawer_open и R.string.navigation_drawer_close
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        Log.d(TAG, "onCreate: ActionBarDrawerToggle synced");

        // --- Инициализация остальных UI ---
        profileImage = findViewById(R.id.profile_image);
        profileUsername = findViewById(R.id.profile_username);
        infoName = findViewById(R.id.info_name);
        infoDob = findViewById(R.id.info_dob);
        infoUsernameInCard = findViewById(R.id.info_username_in_card);
        infoStyle = findViewById(R.id.info_style);
        contactDevelopersLink = findViewById(R.id.contact_developers_link);

        // --- Инициализация include-элементов (контейнеров) ---
        settingLocation = findViewById(R.id.setting_location);
        settingLanguage = findViewById(R.id.setting_language);
        settingQuestionnaire = findViewById(R.id.setting_questionnaire);
        settingSavedLooks = findViewById(R.id.setting_saved_looks);
        settingPlannedLooks = findViewById(R.id.setting_planned_looks);
        Log.d(TAG, "onCreate: Main UI elements initialized");

        // --- Настройка внешнего вида строк настроек (иконки и метки) ---
        setupSettingViews();
        Log.d(TAG, "onCreate: Setting views configured");

        // --- Инициализация элементов заголовка Drawer ---
        View headerView = navigationView.getHeaderView(0);
        drawerHeaderName = headerView.findViewById(R.id.drawer_header_name);
        drawerHeaderLogin = headerView.findViewById(R.id.drawer_header_login);
        drawerHeaderImage = headerView.findViewById(R.id.drawer_header_image);
        Log.d(TAG, "onCreate: Drawer header views initialized");


        // --- Инициализация DB Helper ---
        dbHelper = new DatabaseHelper(this);
        Log.d(TAG, "onCreate: DatabaseHelper initialized");

        // --- Получение ID пользователя ---
        retrieveUserId();
        Log.d(TAG, "onCreate: Retrieved userId = " + userId);

        // --- Проверка и запуск загрузки данных ---
        if (userId == DatabaseHelper.DEFAULT_USER_ID) {
            Log.e(TAG, "onCreate: Invalid userId, redirecting to login");
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            redirectToLogin(); // Используем метод для перенаправления
            return; // Выходим, чтобы не продолжать
        } else {
            Log.d(TAG, "onCreate: Starting LoadProfileTask for userId = " + userId);
            new LoadProfileTask().execute(userId);
        }

        // --- Обработчики нажатий ---
        setupClickListeners();
        Log.d(TAG, "onCreate: Click listeners set up");
    }

    // --- Метод получения ID пользователя ---
    private void retrieveUserId() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(DatabaseHelper.EXTRA_USER_ID)) {
            userId = intent.getLongExtra(DatabaseHelper.EXTRA_USER_ID, DatabaseHelper.DEFAULT_USER_ID);
            Log.d(TAG, "retrieveUserId: Got userId from Intent: " + userId);
        }
        // Если в Intent нет (например, Activity была восстановлена), пробуем из SharedPreferences
        if (userId == DatabaseHelper.DEFAULT_USER_ID) {
            SharedPreferences prefs = getSharedPreferences(DatabaseHelper.SHARED_PREFS_NAME, MODE_PRIVATE);
            userId = prefs.getLong(DatabaseHelper.PREF_KEY_LOGGED_IN_USER_ID, DatabaseHelper.DEFAULT_USER_ID);
            Log.d(TAG, "retrieveUserId: Got userId from SharedPreferences: " + userId);
        }
    }

    // --- Метод настройки иконок и меток для строк настроек ---
    private void setupSettingViews() {
        Log.d(TAG, "setupSettingViews: Setting up views");
        setupSingleSettingView(settingLocation, R.drawable.location_on, R.string.setting_location_label);
        setupSingleSettingView(settingLanguage, R.drawable.language, R.string.setting_language_label);
        setupSingleSettingView(settingQuestionnaire, R.drawable.description, R.string.setting_questionnaire_label);
        setupSingleSettingView(settingSavedLooks, R.drawable.bookmark_border, R.string.setting_saved_looks_label);
        setupSingleSettingView(settingPlannedLooks, R.drawable.event, R.string.setting_planned_looks_label);
    }

    // --- Вспомогательный метод для настройки одной строки ---
    private void setupSingleSettingView(ConstraintLayout container, int iconResId, int labelResId) {
        if (container == null) {
            Log.w(TAG, "setupSingleSettingView: Container is null for labelResId " + getString(labelResId));
            return;
        }
        try {
            ImageView icon = container.findViewById(R.id.setting_icon);
            TextView label = container.findViewById(R.id.setting_label);
            if (icon != null) {
                icon.setImageResource(iconResId);
            } else {
                Log.w(TAG, "setupSingleSettingView: Icon not found in container for labelResId " + getString(labelResId));
            }
            if (label != null) {
                label.setText(labelResId);
            } else {
                Log.w(TAG, "setupSingleSettingView: Label not found in container for labelResId " + getString(labelResId));
            }
        } catch (Exception e) {
            Log.e(TAG, "setupSingleSettingView: Error setting up view for labelResId " + getString(labelResId), e);
        }
    }


    // --- Метод установки обработчиков ---
    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up click listeners");
        if (settingLocation != null) {
            settingLocation.setOnClickListener(v -> Toast.makeText(this, "Переход к настройкам местоположения (TODO)", Toast.LENGTH_SHORT).show());
        }
        if (settingLanguage != null) {
            settingLanguage.setOnClickListener(v -> Toast.makeText(this, "Переход к настройкам языка (TODO)", Toast.LENGTH_SHORT).show());
        }
        if (settingQuestionnaire != null) {
            settingQuestionnaire.setOnClickListener(v -> Toast.makeText(this, "Переход к анкете (TODO)", Toast.LENGTH_SHORT).show());
        }
        if (settingSavedLooks != null) {
            settingSavedLooks.setOnClickListener(v -> Toast.makeText(this, "Переход к сохраненным образам (TODO)", Toast.LENGTH_SHORT).show());
        }
        if (settingPlannedLooks != null) {
            settingPlannedLooks.setOnClickListener(v -> Toast.makeText(this, "Переход к запланированным образам (TODO)", Toast.LENGTH_SHORT).show());
        }
        if (contactDevelopersLink != null) {
            contactDevelopersLink.setOnClickListener(v -> {
                Log.d(TAG, "Contact Developers link clicked");
                try {
                    // Используйте вашу актуальную ссылку
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/cloova_app"));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Could not open contact link", e);
                    Toast.makeText(this, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    // --- AsyncTask для загрузки профиля ---
    @SuppressLint("StaticFieldLeak")
    private class LoadProfileTask extends AsyncTask<Long, Void, ProfileData> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "LoadProfileTask: onPreExecute");
            // Можно показать ProgressBar здесь
        }

        @Override
        protected ProfileData doInBackground(Long... userIds) {
            if (userIds.length == 0) {
                Log.e(TAG, "LoadProfileTask: No userId provided");
                return null;
            }
            long idToLoad = userIds[0];
            Log.d(TAG, "LoadProfileTask: Loading data for userId " + idToLoad);
            try {
                User user = dbHelper.getUserInfo(idToLoad);
                List<String> styles = dbHelper.getUserStyles(idToLoad);
                // TODO: Загрузить другие списки при необходимости
                if (user != null) {
                    Log.d(TAG, "LoadProfileTask: User info loaded successfully");
                    return new ProfileData(user, styles);
                } else {
                    Log.e(TAG, "LoadProfileTask: User info not found for userId " + idToLoad);
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "LoadProfileTask: Error loading profile data", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(@Nullable ProfileData profileData) {
            super.onPostExecute(profileData);
            Log.d(TAG, "LoadProfileTask: onPostExecute");
            // Можно скрыть ProgressBar здесь

            if (profileData == null || profileData.user == null) {
                Log.e(TAG, "LoadProfileTask: Failed to load profile data or user is null");
                Toast.makeText(ProfileActivity.this, "Не удалось загрузить данные профиля", Toast.LENGTH_SHORT).show();
                // Можно не перенаправлять сразу, а показать сообщение об ошибке
                // redirectToLogin();
            } else {
                Log.d(TAG, "LoadProfileTask: Displaying profile data");
                currentUser = profileData.user;
                displayUserProfileData(profileData);
                updateDrawerHeader(profileData.user);
            }
        }
    }
    // --- Вспомогательный класс для передачи данных ---
    private static class ProfileData {
        final User user;
        final List<String> styles;
        // Добавьте другие списки сюда, если загружаете их в doInBackground
        ProfileData(User user, List<String> styles) {
            this.user = user;
            this.styles = styles;
        }
    }
    // --- Конец AsyncTask ---


    // --- Метод для отображения данных ---
    private void displayUserProfileData(ProfileData profileData) {
        Log.d(TAG, "displayUserProfileData: Updating UI");
        User user = profileData.user;
        List<String> styles = profileData.styles;

        // Отображаем основную информацию
        profileUsername.setText(user.getLogin() != null ? "@" + user.getLogin() : getString(R.string.not_available_short)); // Используем строку
        infoName.setText(user.getName() != null ? user.getName() : getString(R.string.not_specified)); // Используем строку
        infoDob.setText(user.getBirthDate() != null ? formatBirthDate(user.getBirthDate()) : getString(R.string.not_specified));
        infoUsernameInCard.setText(user.getLogin() != null ? "@" + user.getLogin() : getString(R.string.not_available_short));

        // Отображаем первый стиль
        if (styles != null && !styles.isEmpty()) {
            infoStyle.setText(styles.get(0));
        } else {
            infoStyle.setText(getString(R.string.style_not_selected)); // Используем строку
        }

        // Отображаем значения в строках настроек
        updateSettingValue(settingLocation, user.getCity(), getString(R.string.not_specified));
        updateSettingValue(settingLanguage, user.getLanguage(), "Русский"); // Язык по умолчанию

        // Устанавливаем заглушки для остальных
        updateSettingValue(settingQuestionnaire, null, "");
        updateSettingValue(settingSavedLooks, null, "");
        updateSettingValue(settingPlannedLooks, null, "");

        Log.d(TAG, "Avatar Res ID from User object: " + user.getAvatarResId());

        // Отображение аватара (лучше использовать Glide/Picasso для URL или сложных путей)
        if (user.getAvatarResId() != 0) {
            profileImage.setImageResource(user.getAvatarResId());
            Log.d(TAG, "Setting avatar to Res ID: " + user.getAvatarResId());
        } else {
            profileImage.setImageResource(R.drawable.default_avatar1);
            Log.d(TAG, "Avatar Res ID is 0 or invalid, setting default: R.drawable.default_avatar1");
        }
    }

    // --- Вспомогательный метод для обновления значения в строке настроек ---
    private void updateSettingValue(ConstraintLayout container, @Nullable String value, String defaultValue) {
        if (container != null) {
            TextView valueTextView = container.findViewById(R.id.setting_value);
            if (valueTextView != null) {
                valueTextView.setText(value != null && !value.isEmpty() ? value : defaultValue);
            } else {
                Log.w(TAG, "updateSettingValue: Value TextView not found in container");
            }
        } else {
            Log.w(TAG, "updateSettingValue: Container is null");
        }
    }


    // --- Метод обновления шапки Drawer ---
    private void updateDrawerHeader(User user) {
        if (drawerHeaderName == null || drawerHeaderLogin == null || drawerHeaderImage == null) {
            Log.w(TAG, "updateDrawerHeader: Drawer header views not initialized yet");
            return;
        }
        Log.d(TAG, "updateDrawerHeader: Updating drawer header");
        drawerHeaderName.setText(user.getName() != null ? user.getName() : getString(R.string.user_default_name)); // Используйте строку
        drawerHeaderLogin.setText(user.getLogin() != null ? "@" + user.getLogin() : "");

        // TODO: Установка аватара в drawerImage (лучше через Glide/Picasso)
        if (user.getAvatarResId() != 0) {
            drawerHeaderImage.setImageResource(user.getAvatarResId());
        } else {
            drawerHeaderImage.setImageResource(R.drawable.default_avatar1);
        }
    }

    // --- Метод для форматирования даты ---
    private String formatBirthDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return getString(R.string.not_specified);
        }
        // TODO: Добавить реальное форматирование даты, если нужно
        // Например, из "YYYY-MM-DD" в "DD.MM.YYYY"
        return rawDate;
    }

    // --- Обработка нажатий на пункты меню в Toolbar ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_toolbar_menu, menu);
        Log.d(TAG, "onCreateOptionsMenu: Menu inflated");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "onOptionsItemSelected: Item selected: " + item.getTitle());
        if (id == R.id.action_edit_profile) {
            Log.d(TAG, "onOptionsItemSelected: Edit profile action selected");
            Intent editIntent = new Intent(this, EditProfileActivity.class); // Создайте эту Activity
            editIntent.putExtra(DatabaseHelper.EXTRA_USER_ID, userId);
            startActivity(editIntent);
            return true;
        }
        // Для обработки кнопки "назад" в Toolbar (если она там есть)
        // if (id == android.R.id.home) {
        //     onBackPressed();
        //     return true;
        // }
        return super.onOptionsItemSelected(item);
    }

    // --- Обработка нажатий на пункты меню в Navigation Drawer ---
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "onNavigationItemSelected: Item selected: " + item.getTitle());
        String userCity = null;
        if (currentUser != null) {
            userCity = currentUser.getCity();
            Log.d(TAG, "onNavigationItemSelected: User city from currentUser: " + userCity);
        } else {
            Toast.makeText(this, "Данные пользователя еще загружаются", Toast.LENGTH_SHORT).show();
        }

        if (id == R.id.nav_home) {
            // TODO: Переход на главный экран
            Log.d(TAG, "Navigation to WeatherForecastActivity");
            Intent weatherIntent = new Intent(this, WeatherForecastActivity.class);
            weatherIntent.putExtra(WeatherForecastActivity.EXTRA_CITY_NAME, (userCity != null && !userCity.isEmpty()) ? userCity : WeatherForecastActivity.FALLBACK_CITY);
            startActivity(weatherIntent);
        } else if (id == R.id.nav_profile) {
            // Мы уже здесь, ничего не делаем
        } else if (id == R.id.nav_settings) {
            // TODO: Переход на экран настроек
            Toast.makeText(this, "Переход на настройки (TODO)", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            logoutUser();
        }

        drawerLayout.closeDrawer(GravityCompat.START); // Закрываем Drawer
        return true;
    }

    // --- Метод выхода ---
    private void logoutUser() {
        Log.d(TAG, "logoutUser: Logging out user");
        SharedPreferences prefs = getSharedPreferences(DatabaseHelper.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(DatabaseHelper.PREF_KEY_LOGGED_IN_USER_ID);
        boolean removed = editor.commit(); // !!! ВОТ ЗДЕСЬ СОХРАНЕНИЕ ИЗМЕНЕНИЙ (УДАЛЕНИЯ) !!!
        if (!removed) {
            Log.e(TAG, "logoutUser: Failed to commit SharedPreferences changes!");
        }
        redirectToMain(); // Используем новый метод
    }

    // --- Метод перенаправления ---
    private void redirectToLogin() { // Переименовать бы в redirectToMain
        Log.d(TAG, "redirectToMain: Redirecting to MainActivity"); // Изменили лог
        Intent intent = new Intent(this, MainActivity.class); // !!! ИДЕМ В MainActivity !!!
        // !!! ОСТАВЛЯЕМ ФЛАГИ ОЧИСТКИ СТЕКА !!!
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectToMain() {
        redirectToLogin(); // Пока просто вызываем старый, т.к. он делает то же самое
    }

    // --- Обработка кнопки "Назад" ---
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            Log.d(TAG, "onBackPressed: Closing drawer");
        } else {
            Log.d(TAG, "onBackPressed: Calling super.onBackPressed");
            super.onBackPressed(); // Стандартное поведение "назад"
        }
    }

    // --- Удаляем onDestroy ---
    // Жизненным циклом БД управляет SQLiteOpenHelper
}*/

package com.example.cloova;


import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private long userId;
    private TextView logOutButton;
    private ImageView editProfileButton;
    private ImageView mainPageButton;
    private ImageView likedLooksButton;
    private LinearLayout contactTelegaLayout;
    private LinearLayout myAnketaLayout;
    private LinearLayout likedLooksLayout;
    private LinearLayout plannedLooksLayout;
    private LinearLayout changeLanguage;
    private Spinner stylesSpinner;
    private String selectedUserStyle;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_profile);

            // Получаем ID пользователя
            userId = getIntent().getLongExtra("USER_ID", -1);
            if (userId == -1) {
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                userId = prefs.getLong("user_id", -1);
            }

            if (userId == -1) {
                Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            dbHelper = new DatabaseHelper(this);
            displayUserProfile();

        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при загрузке профиля", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }

        stylesSpinner = findViewById(R.id.info_style);
        stylesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUserStyle = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "Style selected in Spinner: " + selectedUserStyle);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Если ничего не выбрано, убедитесь, что selectedUserStyle имеет значение по умолчанию
                if (selectedUserStyle == null || selectedUserStyle.isEmpty() || "Стили не выбраны".equals(selectedUserStyle)) {
                    selectedUserStyle = "Повседневный";
                }
            }
        });

        logOutButton = findViewById(R.id.log_out);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vihod(v);
            }
        });

        contactTelegaLayout = findViewById(R.id.contact_telega);
        contactTelegaLayout.setOnClickListener(v -> openSocialLink(
                "tg://resolve?domain=cloova_app",  // Intent для приложения Telegram
                "https://t.me/cloova_app"          // Fallback ссылка
        ));

        changeLanguage = findViewById(R.id.block2);
        changeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeLanguage(v);
            }
        });

        myAnketaLayout = findViewById(R.id.block3);
        myAnketaLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyAnketa(v);
            }
        });

        likedLooksLayout = findViewById(R.id.block4);
        likedLooksLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LikedLooks(v);
            }
        });

        plannedLooksLayout = findViewById(R.id.block5);
        plannedLooksLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlannedLooks(v);
            }
        });

        editProfileButton = findViewById(R.id.imageEditProf);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Edit(v);
            }
        });

        mainPageButton = findViewById(R.id.main_house_shape);
        mainPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToWeatherWithSelectedStyle();
            }
        });

        likedLooksButton = findViewById(R.id.heart_shape);
        likedLooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LikedLooks(v);
            }
        });


    }

    private void navigateToWeatherWithSelectedStyle() {
        User user = dbHelper.getUserInfo(userId);
        String cityToPass = (user != null && user.getCity() != null && !user.getCity().isEmpty()) ?
                user.getCity() : WeatherForecastActivity.FALLBACK_CITY;

        if (selectedUserStyle == null || selectedUserStyle.isEmpty() || "Стили не выбраны".equals(selectedUserStyle)) {
            List<String> userStyles = dbHelper.getUserStyles(userId);
            selectedUserStyle = (userStyles != null && !userStyles.isEmpty()) ?
                    userStyles.get(0) : "Повседневный";
        }

        Log.d(TAG, "Navigating to Weather. City: " + cityToPass + ", Style: " + selectedUserStyle);

        Intent weatherIntent = new Intent(this, WeatherForecastActivity.class);
        weatherIntent.putExtra(WeatherForecastActivity.EXTRA_CITY_NAME, cityToPass);
        weatherIntent.putExtra(WeatherForecastActivity.EXTRA_USER_STYLE, selectedUserStyle);
        startActivity(weatherIntent);
    }


    public void Vihod(View v) {
        // 1. Очищаем SharedPreferences (сессию)
        SharedPreferences prefs = getSharedPreferences(DatabaseHelper.SHARED_PREFS_NAME, MODE_PRIVATE);
        prefs.edit().remove(DatabaseHelper.PREF_KEY_LOGGED_IN_USER_ID).apply();

        // 2. Создаем Intent для MainActivity с полным сбросом стека
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 3. Запускаем и закрываем текущую Activity
        startActivity(intent);
        finish();
    }
    private void openSocialLink(String appUri, String webUrl) {
        try {
            // Пробуем открыть в приложении
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(appUri));
            startActivity(appIntent);
        } catch (Exception e) {
            try {
                // Если приложение не установлено, открываем в браузере
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
                startActivity(browserIntent);
            } catch (Exception ex) {
                Toast.makeText(this, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void ChangeLanguage(View v) {
        Intent intent = new Intent(this, LanguagesActivity.class);
        intent.putExtra("USER_ID", userId); // Убедитесь, что userId получен
        startActivity(intent);
        // Не вызываем finish() - оставляем ProfileActivity в стеке
    }

    private void Edit(View v) {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra("USER_ID", userId); // Добавьте эту строку
        startActivity(intent);
    }

    private void DayPage(View v) {
        Intent intent = new Intent(this, WeatherForecastActivity.class);
        startActivity(intent);
    }

    private void LikedLooks(View v) {
        Intent intent = new Intent(this, Sohranenki.class);
        startActivity(intent);
    }

    private void PlannedLooks(View v) {
        Intent intent = new Intent(this, Zaplanerki.class);
        startActivity(intent);
    }

    private void MyAnketa(View v) {
        Intent intent = new Intent(this, Anketa.class);
        startActivity(intent);
    }

    private void displayUserProfile() {
        User user = dbHelper.getUserInfo(userId);

        if (user == null) {
            Toast.makeText(this, "Данные пользователя не найдены", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView infoName = findViewById(R.id.info_name);
        TextView infoBirthDate= findViewById(R.id.info_birth_date);
        TextView profileUsername = findViewById(R.id.profile_username);
        TextView infoUsername = findViewById(R.id.info_username);
        stylesSpinner = findViewById(R.id.info_style);
        TextView infoCity = findViewById(R.id.info_city);
        TextView infoLanguage = findViewById(R.id.info_language);
        ImageView infoAvatar = findViewById(R.id.iv_avatar);

        // Установка значений с проверками
        profileUsername.setText(user.getLogin() != null ? "@" + user.getLogin() : getString(R.string.not_available_short)); // Используем строку
        infoName.setText(user.getName() != null ? user.getName() : "Не указано");
        infoBirthDate.setText(user.getBirthDate() != null ? formatBirthDate(user.getBirthDate()) : "Не указана");
        infoUsername.setText(user.getLogin() != null ? user.getLogin() : "Не указано");

        infoCity.setText(user.getCity() != null ? user.getCity() : "Не указано");
        infoLanguage.setText(user.getLanguage() != null ? user.getLanguage() : "Русский");

        if (user.getAvatarResId() != 0) {
            infoAvatar.setImageResource(user.getAvatarResId());
        } else {
            infoAvatar.setImageResource(R.drawable.default_avatar1);
        }

        if (user.getLanguage() != null) {
            String langCode = user.getLanguage().equals("Русский") ? "ru" : "en";
            SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
            editor.putString("app_lang", langCode);
            editor.apply();
        }

        loadUserStyles();

        if (selectedUserStyle != null && stylesSpinner.getAdapter() != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) stylesSpinner.getAdapter();
            int position = adapter.getPosition(selectedUserStyle);
            if (position >= 0) {
                stylesSpinner.setSelection(position);
            }
        }
    }

    private void loadUserStyles() {
        List<String> styles = dbHelper.getUserStyles(userId);
        if (styles != null && !styles.isEmpty()) {
            selectedUserStyle = styles.get(0);
        } else {
            selectedUserStyle = "Повседневный"; // Если стилей нет, используем дефолтный
            styles = new ArrayList<>(); // Инициализируем пустой список для адаптера
            styles.add("Стили не выбраны"); // Добавляем заглушку для Spinner
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                styles != null && !styles.isEmpty() ? styles.toArray(new String[0]) : new String[]{"Стили не выбраны"}
       ) {
            // Для закрытого состояния Spinner
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTypeface(ResourcesCompat.getFont(ProfileActivity.this, R.font.manrope_bold));
                return view;
            }

            // Для выпадающего списка
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTypeface(ResourcesCompat.getFont(ProfileActivity.this, R.font.manrope_bold));
                return view;
            }


        };

        if (styles != null && !styles.isEmpty()) {
            if (selectedUserStyle == null || !styles.contains(selectedUserStyle)) { // Если еще не установлен или нет в списке
                selectedUserStyle = styles.get(0); // Берем первый как дефолтный
            }
        } else {
            selectedUserStyle = "Повседневный"; // Если стилей нет
        }

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        stylesSpinner.setAdapter(adapter);

        if (styles.contains(selectedUserStyle)) {
            int position = adapter.getPosition(selectedUserStyle);
            stylesSpinner.setSelection(position);
        }

        // Применяем стиль к Spinner (для API 21+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stylesSpinner.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);
        }
    }
    private String formatBirthDate(String rawDate) {
        // Простой форматировщик даты (можно заменить на более сложный)
        if (rawDate == null || rawDate.isEmpty()) {
            return "Не указана";
        }
        return rawDate; // или преобразуйте формат по вашему усмотрению
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}