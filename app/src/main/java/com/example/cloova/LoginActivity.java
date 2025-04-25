package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.AsyncTask;

public class LoginActivity extends AppCompatActivity {

    private EditText loginWindow;
    private EditText passWindow;
    private Button loginButton;
    private Button goBackButton;
    private DatabaseHelper databaseHelper;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Инициализация SharedPreferences

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
                // Запускаем проверку в фоновом потоке (пример с AsyncTask)
                String login = loginWindow.getText().toString().trim();
                String password = passWindow.getText().toString().trim();

                // Проверка на пустые поля перед запуском задачи
                if (login.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Отключаем кнопку на время проверки
                loginButton.setEnabled(false);
                // Можно показать ProgressBar
                // progressBar.setVisibility(View.VISIBLE);

                new LoginTask().execute(login, password);
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

    @SuppressLint("StaticFieldLeak") // Предупреждение о возможной утечке, для простоты игнорируем
    private class LoginTask extends AsyncTask<String, Void, Long> {

        private String loginAttempt; // Сохраняем логин для получения ID

        @Override
        protected Long doInBackground(String... credentials) {
            loginAttempt = credentials[0];
            String passwordAttempt = credentials[1];

            // Сначала проверяем логин
            if (!databaseHelper.checkLoginExists(loginAttempt)) {
                return -2L; // Код ошибки: пользователь не найден
            }
            // Затем проверяем пароль (с хэшированием!)
            if (!databaseHelper.checkUserCredentials(loginAttempt, passwordAttempt)) {
                return -3L; // Код ошибки: неверный пароль
            }
            // Если все ок, получаем ID
            return databaseHelper.getUserId(loginAttempt); // Вернет -1, если ошибка получения ID
        }

        @Override
        protected void onPostExecute(Long userIdResult) {
            // Выполняется в основном потоке после doInBackground
            // Включаем кнопку обратно
            loginButton.setEnabled(true);
            // Скрываем ProgressBar
            // progressBar.setVisibility(View.GONE);

            if (userIdResult == -1) { // Ошибка получения ID
                Toast.makeText(LoginActivity.this, "Ошибка получения данных пользователя", Toast.LENGTH_LONG).show();
            } else if (userIdResult == -2L) { // Пользователь не найден
                Toast.makeText(LoginActivity.this, "Пользователь не найден", Toast.LENGTH_LONG).show();
                passWindow.setText(""); // Очищаем пароль
            } else if (userIdResult == -3L) { // Неверный пароль
                Toast.makeText(LoginActivity.this, "Неверный пароль", Toast.LENGTH_LONG).show();
                passWindow.setText(""); // Очищаем пароль
            } else {
                // --- Успешный вход ---
                // Сохранение ID пользователя в SharedPreferences
                SharedPreferences prefs = getSharedPreferences(DatabaseHelper.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(DatabaseHelper.PREF_KEY_LOGGED_IN_USER_ID, userIdResult);
                boolean saved = editor.commit(); // !!! ВОТ ЗДЕСЬ СОХРАНЕНИЕ !!!

                Toast.makeText(LoginActivity.this, "Вход выполнен успешно!", Toast.LENGTH_LONG).show();

                // Переход на ProfileActivity
                Intent profileIntent = new Intent(LoginActivity.this, ProfileActivity.class);
                profileIntent.putExtra(DatabaseHelper.EXTRA_USER_ID, userIdResult); // Используем константу

                // Очищаем стек, чтобы ProfileActivity стала корневой (удаляем MainActivity и LoginActivity из стека)
                profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(profileIntent);
                finish(); // Закрываем LoginActivity
            }
        }
    }
}