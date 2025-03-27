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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginWindow = findViewById(R.id.usernameEditText);
        passWindow = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        goBackButton = findViewById(R.id.gobackbutton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmit();
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
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


        Toast.makeText(this, "Вход успешен!", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
        startActivity(intent);

        finish();
    }
}