package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class RegistrationActivity extends AppCompatActivity {

    private EditText loginWindow;
    private EditText passWindow;
    private EditText confirmpassWindow;
    private Button registrationButton;
    private Button goBackButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        loginWindow = findViewById(R.id.loginEditText);
        passWindow = findViewById(R.id.passwordEditText);
        confirmpassWindow = findViewById(R.id.confirmPasswordEditText);
        registrationButton = findViewById(R.id.registerButton);
        goBackButton = findViewById(R.id.gobackbutton);

        databaseHelper = new DatabaseHelper(this);

        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmit();
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void handleSubmit() {
        String login = loginWindow.getText().toString().trim();
        String password = passWindow.getText().toString().trim();
        String confirmPassword = confirmpassWindow.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            passWindow.setText("");
            confirmpassWindow.setText("");
            return;
        }

        boolean isAdded = databaseHelper.addUser(login, password);

        if(isAdded){
            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(RegistrationActivity.this, ProfileActivity.class);
            startActivity(intent);

            finish();
        }
        else {
            // Если addUser вернул false (ошибка или логин занят)
            Toast.makeText(this, "Ошибка регистрации. Возможно, логин уже занят.", Toast.LENGTH_LONG).show();
            // Можно очистить поле логина, чтобы пользователь ввел другое
            loginWindow.setText("");
        }
    }
}