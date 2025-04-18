package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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