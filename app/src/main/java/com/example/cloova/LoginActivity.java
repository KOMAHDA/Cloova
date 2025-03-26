package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginWindow = findViewById(R.id.editText1);
        passWindow = findViewById(R.id.editText2);
        loginButton = findViewById(R.id.buttonSubmit);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmit();
            }
        });
    }

    private void handleSubmit() {
        String login = loginWindow.getText().toString().trim();
        String password = passWindow.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }


        Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
        startActivity(intent);

        finish();
    }
}