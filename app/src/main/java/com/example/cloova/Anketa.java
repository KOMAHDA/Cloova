package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.cloova.R;  // Ваш пакет
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class Anketa extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anketa);

        // Инициализация кнопок выбора
        setupSelectionButtons();

        // Навигация
        setupNavigation();
    }

    private void setupSelectionButtons() {
        int[] buttonIds = {
                R.id.btnSelectColors,
                R.id.btnSelectAccessories, R.id.btnSelectClothes,
                R.id.btnSelectStyles,
        };

        for (int id : buttonIds) {
            Button button = findViewById(id);
            button.setOnClickListener(v -> openSelectionDialog(button));
        }
    }

    private void openSelectionDialog(Button button) {
        String dialogTitle = "";
        if (button.getId() == R.id.btnSelectColors) {
            dialogTitle = getString(R.string.profile_colors_label);
        } else if (button.getId() == R.id.btnSelectAccessories) {
            dialogTitle = getString(R.string.profile_accessories_question);
        } else if (button.getId() == R.id.btnSelectClothes) {
            dialogTitle = getString(R.string.profile_clothes_hint);
        } else if (button.getId() == R.id.btnSelectStyles) {
            dialogTitle = getString(R.string.situazia);
        }

        Intent intent = new Intent(this, SelectionActivity.class);
        intent.putExtra("dialog_title", dialogTitle);
        intent.putExtra("button_id", button.getId());
        startActivityForResult(intent, 1);
    }

    //private void saveProfile() {
    // сохранение данных анкеты
    // SharedPreferences, Room Database или отправить на сервер
    //Toast.makeText(this, R.string.saved_successfully, Toast.LENGTH_SHORT).show();
        /*
        SharedPreferences prefs = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("preferred_colors", ((Button)findViewById(R.id.btnSelectColors)).getText().toString());
        // остальные поля
        editor.apply();
        */
    //}

    private void setupNavigation() {
        ImageView goBackButton = findViewById(R.id.gobackbutton);
        goBackButton.setOnClickListener(v -> finish());

        Button profileBtn = findViewById(R.id.profile_shape);
        Button homeBtn = findViewById(R.id.main_house_shape);
        Button favoritesBtn = findViewById(R.id.heart_shape);

        profileBtn.setOnClickListener(v -> {
            // Переход в профиль
            startActivity(new Intent(this, ProfileActivity.class));
        });

        homeBtn.setOnClickListener(v -> {
            // Переход на главную
            startActivity(new Intent(this, DayDetailActivity.class));
        });

        favoritesBtn.setOnClickListener(v -> {
            // Переход в избранное
            startActivity(new Intent(this, Sohranenki.class));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String selectedValue = data.getStringExtra("selected_value");
            int buttonId = data.getIntExtra("button_id", -1);
            if (buttonId != -1) {
                Button button = findViewById(buttonId);
                button.setText(selectedValue);
            }
        }
    }
}