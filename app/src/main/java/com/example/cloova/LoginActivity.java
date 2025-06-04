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
import android.widget.ImageView;
import android.widget.Toast;
import android.os.AsyncTask;

public class LoginActivity extends AppCompatActivity {

    private EditText loginWindow;
    private EditText passWindow;
    private Button loginButton;
    private ImageView goBackButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

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

                String login = loginWindow.getText().toString().trim();
                String password = passWindow.getText().toString().trim();


                if (login.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginButton.setEnabled(false);



                new LoginTask().execute(login, password);
            }
        });


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
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    private class LoginTask extends AsyncTask<String, Void, Long> {

        private String loginAttempt;

        @Override
        protected Long doInBackground(String... credentials) {
            loginAttempt = credentials[0];
            String passwordAttempt = credentials[1];


            if (!databaseHelper.checkLoginExists(loginAttempt)) {
                return -2L;
            }

            if (!databaseHelper.checkUserCredentials(loginAttempt, passwordAttempt)) {
                return -3L;
            }

            return databaseHelper.getUserId(loginAttempt);
        }

        @Override
        protected void onPostExecute(Long userIdResult) {
            loginButton.setEnabled(true);


            if (userIdResult == -1) {
                Toast.makeText(LoginActivity.this, "Ошибка получения данных пользователя", Toast.LENGTH_LONG).show();
            } else if (userIdResult == -2L) {
                Toast.makeText(LoginActivity.this, "Пользователь не найден", Toast.LENGTH_LONG).show();
                passWindow.setText("");
            } else if (userIdResult == -3L) {
                Toast.makeText(LoginActivity.this, "Неверный пароль", Toast.LENGTH_LONG).show();
                passWindow.setText("");
            } else {


                SharedPreferences prefs = getSharedPreferences(DatabaseHelper.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(DatabaseHelper.PREF_KEY_LOGGED_IN_USER_ID, userIdResult);
                boolean saved = editor.commit();

                Toast.makeText(LoginActivity.this, "Вход выполнен успешно!", Toast.LENGTH_LONG).show();

                Intent profileIntent = new Intent(LoginActivity.this, ProfileActivity.class);
                profileIntent.putExtra(DatabaseHelper.EXTRA_USER_ID, userIdResult);

                profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(profileIntent);
                finish();
            }
        }
    }
}