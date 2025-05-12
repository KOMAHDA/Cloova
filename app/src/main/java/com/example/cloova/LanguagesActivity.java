package com.example.cloova;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class LanguagesActivity extends AppCompatActivity {

    private RadioGroup languageRadioGroup;
    private SharedPreferences prefs;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_languages);

        // Получаем ID пользователя
        userId = getIntent().getLongExtra("USER_ID", -1);
        if (userId == -1) {
            finish(); // Закрываем если нет ID
            return;
        }

        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        languageRadioGroup = findViewById(R.id.language_radio_group);

        // Установка текущего выбранного языка
        String currentLang = prefs.getString("app_lang", "ru");
        if (currentLang.equals("en")) {
            languageRadioGroup.check(R.id.radio_english);
        } else {
            languageRadioGroup.check(R.id.radio_russian);
        }

        // Обработчик изменения языка
        languageRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String lang = "ru";
            if (checkedId == R.id.radio_english) {
                lang = "en";
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("app_lang", lang);
            editor.apply();

            DatabaseHelper dbHelper = new DatabaseHelper(this);
            dbHelper.updateUserLanguage(userId, lang.equals("en") ? "English" : "Русский");

            LocaleHelper.setLocale(this, lang);
            recreate();
        });

        // Обработчик кнопки "Назад"
        findViewById(R.id.gobackbutton).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Очищает стек до существующего экземпляра
            startActivity(intent);
        });

        // Обработчики кнопок навигации (без finish())
        findViewById(R.id.profile_shape).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Очищает стек до существующего экземпляра
            startActivity(intent);
        });

        findViewById(R.id.main_house_shape).setOnClickListener(v -> {
            Intent intent = new Intent(this, DayDetailActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        findViewById(R.id.heart_shape).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Стандартное поведение - закрытие текущей Activity
    }
}