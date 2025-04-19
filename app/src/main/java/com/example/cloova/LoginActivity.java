package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText loginWindow;
    private EditText passWindow;
    private Button loginButton;
    private Button goBackButton;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Инициализация SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Инициализация UI элементов
        loginWindow = findViewById(R.id.usernameEditText);
        passWindow = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        goBackButton = findViewById(R.id.gobackbutton);

        // Инициализация DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Обработчик кнопки входа
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmit();
            }
        });

        // Обработчик кнопки "Назад"
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Back(v);
            }
        });
    }

    public void Back(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Закрываем текущую активити
    }

    private void handleSubmit() {
        String login = loginWindow.getText().toString().trim();
        String password = passWindow.getText().toString().trim();

        // Проверка на пустые поля
        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка существования пользователя
        if (!databaseHelper.checkLoginExists(login)) {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_LONG).show();
            passWindow.setText("");
            return;
        }

        // Проверка учетных данных
        if (!databaseHelper.checkUserCredentials(login, password)) {
            Toast.makeText(this, "Неверный пароль", Toast.LENGTH_LONG).show();
            passWindow.setText("");
            return;
        }

        // Получение ID пользователя
        long userId = databaseHelper.getUserId(login);
        if (userId == -1) {
            Toast.makeText(this, "Ошибка получения данных пользователя", Toast.LENGTH_LONG).show();
            return;
        }

        // Сохранение ID пользователя
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("user_id", userId);
        editor.apply();

        // Успешный вход
        Toast.makeText(this, "Вход выполнен успешно!", Toast.LENGTH_LONG).show();

        // Переход на ProfileActivity
        Intent profileIntent = new Intent(LoginActivity.this, ProfileActivity.class);
        profileIntent.putExtra("USER_ID", userId);
        startActivity(profileIntent);
        finish(); // Закрываем текущую активити
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}