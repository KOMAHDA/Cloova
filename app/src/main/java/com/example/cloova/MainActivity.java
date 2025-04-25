package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Checking login status...");
        SharedPreferences prefs = getSharedPreferences(DatabaseHelper.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        long loggedInUserId = prefs.getLong(DatabaseHelper.PREF_KEY_LOGGED_IN_USER_ID, DatabaseHelper.DEFAULT_USER_ID);
        Log.d(TAG, "onCreate: Found userId in Prefs: " + loggedInUserId);

        if (loggedInUserId != DatabaseHelper.DEFAULT_USER_ID) {
            // Пользователь уже вошел, перенаправляем в профиль
            Log.d(TAG, "onCreate: User is logged in. Redirecting to ProfileActivity...");
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra(DatabaseHelper.EXTRA_USER_ID, loggedInUserId); // Передаем ID

            // Очищаем стек активностей, чтобы ProfileActivity стала корневой
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish(); // Закрываем MainActivity, чтобы она не оставалась в стеке
            return;   // Выходим из onCreate, чтобы не выполнять остальной код
        } else {
            // Пользователь НЕ вошел, продолжаем обычную инициализацию MainActivity
            Log.d(TAG, "onCreate: User is NOT logged in. Showing MainActivity layout.");
        }

        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Setting main activity layout");

        // Обработчик для иконки Telegram
        ImageView telegaImage = findViewById(R.id.telega);
        telegaImage.setOnClickListener(v -> openSocialLink(
                "tg://resolve?domain=cloova_app",  // Intent для приложения Telegram
                "https://t.me/cloova_app"          // Fallback ссылка
        ));

        // Обработчик для иконки VK
        ImageView vkImage = findViewById(R.id.vk); // Предполагаемый ID вашей иконки VK
        vkImage.setOnClickListener(v -> openSocialLink(
                "vk://vk.com/cloova_app",          // Intent для приложения VK
                "https://vk.com/cloova_app"        // Fallback ссылка
        ));
    }

    // Универсальный метод для открытия соцсетей
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

    public void Vhod(View v) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void Registration(View v) {
        startActivity(new Intent(this, Registration_step1.class));
    }
}