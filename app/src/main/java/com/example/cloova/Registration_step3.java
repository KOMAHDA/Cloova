package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

public class Registration_step3 extends AppCompatActivity {

    private EditText loginEditText, passwordEditText, confPasswordEditText;
    private ImageView goBackButton;
    private Button createButton;
    private DatabaseHelper dbHelper;

    String userLanguage = "Русский";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_step3);

        loginEditText = findViewById(R.id.loginEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confPasswordEditText = findViewById(R.id.confPasswordEditText);

        goBackButton = findViewById(R.id.gobackbutton);
        createButton = findViewById(R.id.createButton);

        dbHelper = new DatabaseHelper(this);

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Back(v);
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Create(v);
            }
        });

    }

    public void Back(View v) {
        Intent intent = new Intent(this, Registration_step2.class);
        startActivity(intent);
    }

    public void Create(View v) {
        // Проверка полей ввода
        if (loginEditText.getText() == null || passwordEditText.getText() == null ||
                confPasswordEditText.getText() == null) {
            Toast.makeText(this, "Ошибка ввода данных", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = loginEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confPasswordEditText.getText().toString().trim();

        // Валидация данных
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Логин и пароль обязательны", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        // Получаем данные из предыдущих шагов
        Intent intent = getIntent();
        String name = intent != null ? intent.getStringExtra("name") : "";
        String gender = intent != null ? intent.getStringExtra("gender") : "";
        String birthDate = intent != null ? intent.getStringExtra("birthDate") : "";
        String city = intent != null ? intent.getStringExtra("city") : "";
        int avatarResId = intent != null ? intent.getIntExtra("avatar", R.drawable.default_avatar1) : R.drawable.default_avatar1;

        // Получаем списки из Registration_step2
        ArrayList<String> colors = intent != null ? intent.getStringArrayListExtra("colors") : new ArrayList<>();
        ArrayList<String> styles = intent != null ? intent.getStringArrayListExtra("styles") : new ArrayList<>();
        ArrayList<String> wardrobe = intent != null ? intent.getStringArrayListExtra("wardrobe") : new ArrayList<>();
        ArrayList<String> accessories = intent != null ? intent.getStringArrayListExtra("accessories") : new ArrayList<>();

        // Логирование для отладки
        Log.d("REG_DEBUG", "Colors: " + colors.toString());
        Log.d("REG_DEBUG", "Wardrobe: " + wardrobe.toString());
        Log.d("REG_DEBUG", "Styles: " + styles.toString());
        Log.d("REG_DEBUG", "Accessories: " + accessories.toString());

        // Регистрируем пользователя
        try {
            // Проверка занятости логина
            if (dbHelper.checkLoginExists(username)) {
                Toast.makeText(this, "Логин уже занят", Toast.LENGTH_SHORT).show();
                return;
            }

            // Добавляем пользователя
            long userId = dbHelper.addUser(username, password, name, gender, birthDate, city, userLanguage, avatarResId);

            if (userId == -1) {
                Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                return;
            }

            // Сохраняем дополнительные данные
            if (!colors.isEmpty()) {
                dbHelper.addUserColors(userId, colors);
                Log.d("DB_DEBUG", "Colors saved for user: " + userId);
            }

            if (!styles.isEmpty()) {
                dbHelper.addUserStyles(userId, styles);
                Log.d("DB_DEBUG", "Styles saved for user: " + userId);
            }

            if (!wardrobe.isEmpty()) {
                dbHelper.addWardrobeItems(userId, wardrobe);
                Log.d("DB_DEBUG", "Wardrobe saved for user: " + userId);
            }

            if (!accessories.isEmpty()) {
                dbHelper.addAccessories(userId, accessories);
                Log.d("DB_DEBUG", "Accessories saved for user: " + userId);
            }

            // Успешная регистрация
            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();

            // Переход на экран входа
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.putExtra("username", username);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            finish();

        } catch (Exception e) {
            Log.e("REG_ERROR", "Registration failed", e);
            Toast.makeText(this, "Ошибка регистрации: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}