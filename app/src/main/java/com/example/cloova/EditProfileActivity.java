package com.example.cloova;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_edit_profile);

        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при загрузке образов", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }
    }
}
