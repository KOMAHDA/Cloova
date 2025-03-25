package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SignUpActivity extends AppCompatActivity{
    private EditText editTextSignUpEmail, editTextSignUpPassword, editTextConfirmPassword;
    private Button buttonSignUp;
    private TextView textViewLogin;
    private ProgressBar progressBarSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_file);

        editTextSignUpEmail = findViewById(R.id.editTextSignUpEmail);
        editTextSignUpPassword = findViewById(R.id.editTextSignUpPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewLogin = findViewById(R.id.textViewLogin);
        progressBarSignUp = findViewById(R.id.progressBarSignUp);

        buttonSignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                registerUser();
            }

        });

        textViewLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
    }

    private void registerUser(){
        String email = editTextSignUpEmail.getText().toString().trim();
        String password = editTextSignUpPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editTextSignUpEmail.setError("Неверный формат почты");
            editTextSignUpEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextSignUpPassword.setError("Неверный формат пароля");
            editTextSignUpPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            editTextConfirmPassword.setError("Неверный формат подтверждения пароля");
            editTextConfirmPassword.requestFocus();
            return;
        }

        if(!password.equals(confirmPassword)){
            editTextConfirmPassword.setError("Пароли не совпадают");
            editTextConfirmPassword.requestFocus();
            return;
        }

        progressBarSignUp.setVisibility(View.VISIBLE);
        Toast.makeText(SignUpActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SignUpActivity.this, ProfileFile.class);
        startActivity(intent);
        finish();


        progressBarSignUp.setVisibility(View.GONE);
    }
}
