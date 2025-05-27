package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class Anketa extends AppCompatActivity {

    // Списки для хранения выбранных значений
    private List<String> selectedColors = new ArrayList<>();       // Предпочитаемые цвета
    private List<String> noLoveColors = new ArrayList<>();         // Цвет, который не носит
    private List<String> mainPrinciples = new ArrayList<>();       // Главный принцип в одежде
    private List<String> accessories = new ArrayList<>();          // Какие аксессуары есть
    private List<String> wardrobeClothes = new ArrayList<>();      // Одежда в гардеробе
    private List<String> noCombinationClothes = new ArrayList<>(); // Одежда, которую не сочетает
    private List<String> situations = new ArrayList<>();           // Ситуации для образов
    private List<String> priority = new ArrayList<>();             // Комфорт или внешний вид

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anketa); // Убедитесь, что у вас есть соответствующий XML-файл

        // Инициализация кнопок и установка обработчиков
        setupButton(R.id.btnSelectColors, R.string.profile_colors_label_ru, getResources().getStringArray(R.array.colors_array), selectedColors);
        setupButton(R.id.colors, R.string.nolove_ru, getResources().getStringArray(R.array.no_love_colors), noLoveColors);
        setupButton(R.id.bnSelectStyles, R.string.profile_clothes_label_ru, getResources().getStringArray(R.array.clothing_principles), mainPrinciples);
        setupButton(R.id.btnSelectAccessories, R.string.profile_accessories_question_ru, getResources().getStringArray(R.array.accessories_array), accessories);
        setupButton(R.id.btnSelectClothes, R.string.profile_clothes_hint_ru, getResources().getStringArray(R.array.wardrobe_clothes), wardrobeClothes);
        setupButton(R.id.btnSelectSoch, R.string.sochetanie_ru, getResources().getStringArray(R.array.no_combination_clothes), noCombinationClothes);
        setupButton(R.id.btnSelectStyles, R.string.situazia_ru, getResources().getStringArray(R.array.situations), situations);
        setupButton(R.id.btnSelectVid, R.string.komfort_ru, getResources().getStringArray(R.array.priority_options), priority);

        // Кнопка отправки анкеты (если нужна)
        Button submitButton = findViewById(R.id.createButton);
        if (submitButton != null) {
            submitButton.setOnClickListener(v -> submitAnketa());
        }
    }

    private void Myhome(View v) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);}

        // Навигация
        setupNavigation();
    }


    private void showMultiChoiceDialog(int titleResId, String[] items, List<String> selectedItemsList) {
        boolean[] checkedItems = new boolean[items.length]; // Массив для отметок выбранных элементов

        // Восстанавливаем ранее выбранные элементы
        for (int i = 0; i < items.length; i++) {
            checkedItems[i] = selectedItemsList.contains(items[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleResId)
                .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked; // Обновляем состояние выбора
                })
                .setPositiveButton(R.string.done_ru, (dialog, id) -> {
                    selectedItemsList.clear(); // Очищаем список перед сохранением новых значений
                    for (int i = 0; i < items.length; i++) {
                        if (checkedItems[i]) {
                            selectedItemsList.add(items[i]); // Добавляем выбранные элементы
                        }
                    }
                    showToast(getString(R.string.selected_ru) + selectedItemsList.toString());
                })
                .setNegativeButton(R.string.cancel_ru, (dialog, id) -> dialog.dismiss());

        builder.create().show();
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


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}