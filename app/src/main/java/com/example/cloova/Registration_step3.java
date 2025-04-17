package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

public class Registration_step3 extends AppCompatActivity {

    private EditText loginEditText, passwordEditText, confPasswordEditText;
    private Button goBackButton;
    private Button createButton;
    private DatabaseHelper dbHelper;

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

    public void Back (View v) {
        Intent intent = new Intent(this, Registration_step2.class);
        startActivity(intent);
    }

/*    public void Create (View v) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("name", nameEditText.getText().toString());
        intent.putExtra("city", citySpinner.getSelectedItem().toString());  // Передаём выбранный город
        intent.putStringArrayListExtra("styles", new ArrayList<>(selectedStyles)); // Список стилей
        if (profileBitmap != null) {
            // Сохраняем фото во временный файл
            String imagePath = saveImageToInternalStorage(profileBitmap);
            // Передаем путь к файлу
            intent.putExtra("profileImagePath", imagePath);
        }
        startActivity(intent);
    }*/


    public void Create (View v) {
        // Получаем данные с текущего экрана
        String username = loginEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confPasswordEditText.getText().toString().trim();

        // Валидация
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        // Получаем данные с первого шага
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String city = intent.getStringExtra("city");
        String birthDate = intent.getStringExtra("birthDate");
        String profileImagePath = intent.getStringExtra("profileImagePath");

        // Сохраняем пользователя в БД
        long userId = dbHelper.addUser(
                username,
                password,
                name,
                city,
                birthDate,
                profileImagePath
        );

        if (userId != -1) {
            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
            // Переходим на главный экран
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity(); // Закрываем все предыдущие активности
        } else {
            Toast.makeText(this, "Ошибка регистрации. Логин занят", Toast.LENGTH_SHORT).show();
        }
    }
}
