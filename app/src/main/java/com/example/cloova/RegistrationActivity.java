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
    private EditText name;
    private Button registrationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        loginWindow = findViewById(R.id.loginEditText);
        passWindow = findViewById(R.id.passwordEditText);
        confirmpassWindow = findViewById(R.id.confirmPasswordEditText);
        registrationButton = findViewById(R.id.registerButton);
        name = findViewById(R.id.nameEditText);

        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmit();
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


        Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(intent);

        finish();
    }
}