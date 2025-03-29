package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginWindow = findViewById(R.id.usernameEditText);
        passWindow = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        goBackButton = findViewById(R.id.gobackbutton);

        databaseHelper = new DatabaseHelper(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmit();
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Back(v);
            }
        });

    }

    public void Back (View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void handleSubmit() {
        String login = loginWindow.getText().toString().trim();
        String password = passWindow.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isValidUser = databaseHelper.checkUser(login, password);

        if (isValidUser) {
            // Если checkUser вернул true (пользователь найден)
            Toast.makeText(this, "Вход выполнен успешно!", Toast.LENGTH_LONG).show(); // Исправили текст тоста
            // Переходим на следующий экран (ProfileActivity?)
            // Убедитесь, что ProfileActivity существует
            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish(); // Закрываем этот экран
        } else {
            // Если checkUser вернул false (пользователь не найден или пароль неверный)
            Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_LONG).show();
            // Очищаем поле пароля для безопасности и удобства
            passWindow.setText("");
        }
    }
}