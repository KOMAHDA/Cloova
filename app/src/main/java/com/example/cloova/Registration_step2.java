package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;

public class Registration_step2 extends AppCompatActivity {

    private Button goBackButton;
    private Button goNextButton;

    private List<String> selectedColors = new ArrayList<>(); // Список выбранных цветов
    private List<String> selectedStyles = new ArrayList<>(); // Список выбранных стилей
    private List<String> selectedPrinciple = new ArrayList<>(); // Список выбранных принципов
    private List<String> selectedClothes = new ArrayList<>(); // Список выбранных вещей одежды
    private List<String> selectedAccessories = new ArrayList<>(); // Список выбранных аксессуаров

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_step2);

        goBackButton = findViewById(R.id.gobackbutton);
        goNextButton = findViewById(R.id.gonextbutton);

        Button btnSelectColors = findViewById(R.id.btnSelectColors);
        btnSelectColors.setOnClickListener(v -> showColorsDialog());

        Button btnSelectStyles = findViewById(R.id.btnSelectStyles);
        btnSelectStyles.setOnClickListener(v -> showStylesDialog());

/*        Button btnSelectPrinciple = findViewById(R.id.btnSelectPrinciple);
        btnSelectPrinciple.setOnClickListener(v -> showSPrincipleDialog());*/

        Button btnSelectClothes = findViewById(R.id.btnSelectClothes);
        btnSelectClothes.setOnClickListener(v -> showClothesDialog());

        Button btnSelectAccessories = findViewById(R.id.btnSelectAccessories);
        btnSelectAccessories.setOnClickListener(v -> showAccessoriesDialog());

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Back(v);
            }
        });

        goNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (isDataValid()) {
                    Next(v);
                }*/
                Next(v);
            }
        });

    }

    public void Back(View v) {
        Intent intent = new Intent(this, Registration_step1.class);
        startActivity(intent);
    }

    public void Next(View v) {
        Intent intent = new Intent(this, Registration_step3.class);

        // Передаем данные из первого шага
        intent.putExtra("name", getIntent().getStringExtra("name"));
        intent.putExtra("gender", getIntent().getStringExtra("gender"));
        intent.putExtra("birthDate", getIntent().getStringExtra("birthDate"));
        intent.putExtra("city", getIntent().getStringExtra("city"));
        intent.putExtra("avatar", getIntent().getIntExtra("avatar", R.drawable.default_avatar1));

        // Передаем списки с проверкой
        intent.putStringArrayListExtra("colors",
                selectedColors != null ? new ArrayList<>(selectedColors) : new ArrayList<>());

        intent.putStringArrayListExtra("styles",
                selectedStyles != null ? new ArrayList<>(selectedStyles) : new ArrayList<>());

        intent.putStringArrayListExtra("wardrobe",
                selectedClothes != null ? new ArrayList<>(selectedClothes) : new ArrayList<>());

        intent.putStringArrayListExtra("accessories",
                selectedAccessories != null ? new ArrayList<>(selectedAccessories) : new ArrayList<>());

        startActivity(intent);
    }

    // Выбор стилей (несколько). Возможно, стоит переделать
    private void showColorsDialog() {
        final String[] colors = {"Красный", "Белый", "Зелёный", "Чёрный"};
        final boolean[] checkedItems = new boolean[colors.length]; // Массив для хранения выбранных элементов

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите цвета")
                .setMultiChoiceItems(colors, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Готово", (dialog, id) -> {
                    selectedColors.clear();
                    for (int i = 0; i < colors.length; i++) {
                        if (checkedItems[i]) {
                            selectedColors.add(colors[i]);
                        }
                    }
                    Toast.makeText(this, "Выбрано: " + selectedColors.toString(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", (dialog, id) -> {
                    // Действие при отмене
                });

        builder.create().show();
    }

    private void showStylesDialog() {
        final String[] styles = {"Классический", "Спортивный", "Повседневный", "Богемный"};
        final boolean[] checkedItems = new boolean[styles.length]; // Массив для хранения выбранных элементов

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите стили")
                .setMultiChoiceItems(styles, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Готово", (dialog, id) -> {
                    // Сохраняем выбранные стили
                    selectedStyles.clear();
                    for (int i = 0; i < styles.length; i++) {
                        if (checkedItems[i]) {
                            selectedStyles.add(styles[i]);
                        }
                    }
                    // Можно показать выбранные стили (опционально)
                    Toast.makeText(this, "Выбрано: " + selectedStyles.toString(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", (dialog, id) -> {
                    // Действие при отмене
                });

        builder.create().show();
    }

    private void showClothesDialog() {
        final String[] clothes = {"Трусы", "Штаны", "Футболка", "Рубашка", "Пальто"};
        final boolean[] checkedItems = new boolean[clothes.length]; // Массив для хранения выбранных элементов

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите вещи")
                .setMultiChoiceItems(clothes, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Готово", (dialog, id) -> {
                    selectedClothes.clear();
                    for (int i = 0; i < clothes.length; i++) {
                        if (checkedItems[i]) {
                            selectedClothes.add(clothes[i]);
                        }
                    }
                    // Можно показать выбранные стили (опционально)
                    Toast.makeText(this, "Выбрано: " + selectedClothes.toString(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", (dialog, id) -> {
                    // Действие при отмене
                });

        builder.create().show();
    }

    private void showAccessoriesDialog() {
        final String[] accessories = {"Очки", "Галстук", "Ремень"};
        final boolean[] checkedItems = new boolean[accessories.length]; // Массив для хранения выбранных элементов

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите аксессуары")
                .setMultiChoiceItems(accessories, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Готово", (dialog, id) -> {
                    // Сохраняем выбранные стили
                    selectedStyles.clear();
                    for (int i = 0; i < accessories.length; i++) {
                        if (checkedItems[i]) {
                            selectedStyles.add(accessories[i]);
                        }
                    }
                    // Можно показать выбранные стили (опционально)
                    Toast.makeText(this, "Выбрано: " + selectedStyles.toString(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", (dialog, id) -> {
                    // Действие при отмене
                });

        builder.create().show();
    }
}

