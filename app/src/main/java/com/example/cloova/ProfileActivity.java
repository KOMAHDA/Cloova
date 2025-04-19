package com.example.cloova;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private long userId;
    private ImageView goBackButton;
    private ImageView editProfileButton;

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

        goBackButton = findViewById(R.id.gobackbutton);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Back(v);
            }
        });

        editProfileButton = findViewById(R.id.imageEditProf);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Edit(v);
            }
        });
    }

    public void Back(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void Edit(View v) {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
    }


    private void displayUserProfile() {
        User user = dbHelper.getUserInfo(userId);

        if (user == null) {
            Toast.makeText(this, "Данные пользователя не найдены", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvName = findViewById(R.id.tv_name);
        TextView tvGender = findViewById(R.id.tv_gender);
        TextView tvBirthDate = findViewById(R.id.tv_birth_date);
        TextView tvCity = findViewById(R.id.tv_city);
        ImageView ivAvatar = findViewById(R.id.iv_avatar);

        // Установка значений с проверками
        tvName.setText(user.getName() != null ? user.getName() : "Не указано");
        tvGender.setText(user.getGender() != null ? user.getGender() : "Не указано");
        tvBirthDate.setText(user.getBirthDate() != null ? formatBirthDate(user.getBirthDate()) : "Не указана");
        tvCity.setText(user.getCity() != null ? user.getCity() : "Не указан");

        if (user.getAvatarResId() != 0) {
            ivAvatar.setImageResource(user.getAvatarResId());
        } else {
            ivAvatar.setImageResource(R.drawable.default_avatar1);
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