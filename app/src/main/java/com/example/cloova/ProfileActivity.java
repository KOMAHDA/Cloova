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
    private LinearLayout changeCity;
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

            userId = getIntent().getLongExtra("USER_ID", -1);
            if (userId == -1) {

                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                userId = prefs.getLong("user_id", -1);
                if (userId == -1) {
                    Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
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
                "tg://resolve?domain=cloova_app",
                "https://t.me/cloova_app"
        ));

        changeCity = findViewById(R.id.block1);
        changeCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeCity(v);
            }
        });


        changeLanguage = findViewById(R.id.block2);
        changeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeLanguage(v);
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

        SharedPreferences prefs = getSharedPreferences(DatabaseHelper.SHARED_PREFS_NAME, MODE_PRIVATE);
        prefs.edit().remove(DatabaseHelper.PREF_KEY_LOGGED_IN_USER_ID).apply();


        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        startActivity(intent);
        finish();
    }
    private void openSocialLink(String appUri, String webUrl) {
        try {

            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(appUri));
            startActivity(appIntent);
        } catch (Exception e) {
            try {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
                startActivity(browserIntent);
            } catch (Exception ex) {
                Toast.makeText(this, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void ChangeCity(View v) {
        Intent intent = new Intent(this, CityActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {

            displayUserProfile();
        }
    }

    private void ChangeLanguage(View v) {
        Intent intent = new Intent(this, LanguagesActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);

    }

    private void Edit(View v) {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra("USER_ID", userId);
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


        profileUsername.setText(user.getLogin() != null ? "@" + user.getLogin() : getString(R.string.not_available_short));
        infoName.setText(user.getName() != null ? user.getName() : "Не указано");
        infoBirthDate.setText(user.getBirthDate() != null ? formatBirthDate(user.getBirthDate()) : "Не указана");
        infoUsername.setText(user.getLogin() != null ? user.getLogin() : "Не указано");

        infoCity.setText(user.getCity() != null ? user.getCity() : "Не указано");
        infoLanguage.setText(user.getLanguage() != null ? user.getLanguage() : "Русский");

        Log.d("ProfileActivity", "User avatar ID from DB: " + user.getAvatarResId());
        if (user.getAvatarResId() != 0) {
            infoAvatar.setImageResource(user.getAvatarResId());
            Log.d("ProfileActivity", "Setting avatar with ID: " + getResources().getResourceEntryName(user.getAvatarResId()));
        } else {
            infoAvatar.setImageResource(R.drawable.default_avatar1);
            Log.d("ProfileActivity", "Setting default avatar: default_avatar1");
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
            selectedUserStyle = "Повседневный";
            styles = new ArrayList<>();
            styles.add("Стили не выбраны");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                styles != null && !styles.isEmpty() ? styles.toArray(new String[0]) : new String[]{"Стили не выбраны"}
        ) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTypeface(ResourcesCompat.getFont(ProfileActivity.this, R.font.manrope_bold));
                return view;
            }


            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTypeface(ResourcesCompat.getFont(ProfileActivity.this, R.font.manrope_bold));
                return view;
            }


        };

        if (styles != null && !styles.isEmpty()) {
            if (selectedUserStyle == null || !styles.contains(selectedUserStyle)) {
                selectedUserStyle = styles.get(0);
            }
        } else {
            selectedUserStyle = "Повседневный";
        }

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        stylesSpinner.setAdapter(adapter);

        if (styles.contains(selectedUserStyle)) {
            int position = adapter.getPosition(selectedUserStyle);
            stylesSpinner.setSelection(position);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stylesSpinner.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);
        }
    }
    private String formatBirthDate(String rawDate) {

        if (rawDate == null || rawDate.isEmpty()) {
            return "Не указана";
        }
        return rawDate;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}