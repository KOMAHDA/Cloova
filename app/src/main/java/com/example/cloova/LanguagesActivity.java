package com.example.cloova;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguagesActivity extends AppCompatActivity {

    private RadioGroup languageRadioGroup;
    private SharedPreferences prefs;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_languages);

        userId = getIntent().getLongExtra("USER_ID", -1);
        if (userId == -1) {
            finish();
            return;
        }

        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        languageRadioGroup = findViewById(R.id.language_radio_group);

        String currentLang = prefs.getString("app_lang", "ru");
        if (currentLang.equals("en")) {
            languageRadioGroup.check(R.id.radio_english);
        } else {
            languageRadioGroup.check(R.id.radio_russian);
        }

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

        findViewById(R.id.gobackbutton).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        findViewById(R.id.profile_shape).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        findViewById(R.id.main_house_shape).setOnClickListener(v -> {
            Intent intent = new Intent(this, DayDetailActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        findViewById(R.id.heart_shape).setOnClickListener(v -> {
            Intent intent = new Intent(this, Sohranenki.class);
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

    }
}