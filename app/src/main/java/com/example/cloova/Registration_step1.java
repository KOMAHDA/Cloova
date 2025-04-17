package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;

import android.Manifest;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileDescriptor;
import java.io.InputStream;

import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import androidx.core.content.FileProvider;

public class Registration_step1 extends AppCompatActivity {

    private EditText nameEditText, dateEditText;
    private Spinner citySpinner;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private ImageView profileImage;
    private Bitmap profileBitmap;
    private Button goBackButton;
    private Button goNextButton;
    private List<String> selectedStyles = new ArrayList<>(); // Список выбранных стилей

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_step1);

        nameEditText = findViewById(R.id.nameEditText);
        dateEditText = findViewById(R.id.dateEditText);
        citySpinner = findViewById(R.id.citySpinner);
        setupCitySpinner();
        goBackButton = findViewById(R.id.gobackbutton);
        goNextButton = findViewById(R.id.gonextbutton);
        profileImage = findViewById(R.id.profileImage);

        // Кнопка выбора фото
        Button uploadPhotoBtn = findViewById(R.id.uploadPhotoBtn);
        uploadPhotoBtn.setOnClickListener(v -> showImagePickerDialog());

        // Кнопка выбора стилей
        Button btnSelectStyles = findViewById(R.id.btnSelectStyles);
        btnSelectStyles.setOnClickListener(v -> showStylesDialog());

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Back(v);
            }
        });

        goNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDataValid()) {
                    Next(v);
                }
            }
        });

    }

    public void Back (View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void Next (View v) {

        Intent intent = new Intent(this, Registration_step3.class);
        intent.putExtra("name", nameEditText.getText().toString());
        intent.putExtra("city", citySpinner.getSelectedItem().toString());  // Передаём выбранный город
        intent.putStringArrayListExtra("styles", new ArrayList<>(selectedStyles)); // Список стилей
        if (profileBitmap != null) {
            // Сохраняем фото во временный файл
            String imagePath = saveImageToInternalStorage(profileBitmap);
            // Передаем путь к файлу
            intent.putExtra("profileImagePath", imagePath);
        }
        startActivity(intent);
    }

    // Выбор города. Нужно будет брать его из привязанного источника с погодой наверное
    private void setupCitySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.cities_array,  // Массив городов в res/values/arrays.xml
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(adapter);
    }

    // Выбор стилей (несколько). Возможно, стоит переделать
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

    // Фото профиля
    private void showImagePickerDialog() {
        String[] options = {"Камера", "Галерея"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите источник")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: openCamera(); break;
                        case 1: openGallery(); break;
                    }
                })
                .show();
    }
    private void openCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
            return;
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, CAMERA_REQUEST);
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                try {
                    InputStream stream = getContentResolver().openInputStream(data.getData());
                    profileBitmap = BitmapFactory.decodeStream(stream);
                    profileImage.setImageBitmap(profileBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CAMERA_REQUEST && data != null) {
                profileBitmap = (Bitmap) data.getExtras().get("data");
                profileImage.setImageBitmap(profileBitmap);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }

    // Проверка заполнения полей
    private boolean isDataValid() {
        if (nameEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Введите имя", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (citySpinner.getSelectedItemPosition() == 0) {  // Проверка выбора города
            Toast.makeText(this, "Выберите город", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedStyles.isEmpty()) {
            Toast.makeText(this, "Выберите хотя бы один стиль", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private String saveImageToInternalStorage(Bitmap bitmap) {
        File directory = getApplicationContext().getDir("profile_images", MODE_PRIVATE);
        File imageFile = new File(directory, "temp_profile.jpg");

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
