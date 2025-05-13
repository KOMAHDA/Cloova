package com.example.cloova;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private long userId;
    private DatabaseHelper dbHelper;
    private EditText infoName, infoDob, infoUsernameInCard;
    private TextView infoUsername;
    private User originalUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_edit_profile);

            // Инициализация DatabaseHelper
            dbHelper = new DatabaseHelper(this);

            // Получаем ID пользователя
            userId = getIntent().getLongExtra("USER_ID", -1);
            if (userId == -1) {
                finish(); // Закрываем если нет ID
                return;
            }

            // Инициализация полей ввода
            infoName = findViewById(R.id.info_name);
            infoDob = findViewById(R.id.info_dob);
            infoUsernameInCard = findViewById(R.id.info_username_in_card);
            infoUsername = findViewById(R.id.profile_username);

            // Загрузка данных пользователя
            loadUserData();

            // Обработчик кнопки "Сохранить"
            Button saveButton = findViewById(R.id.save_button);
            saveButton.setOnClickListener(v -> saveProfileChanges());

        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при загрузке образов", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }

        // Обработчик кнопки "Назад"
        findViewById(R.id.gobackbutton).setOnClickListener(v -> {
            // Просто возвращаемся без сохранения
            navigateBackToProfile();
        });

        // Обработчики кнопок навигации (без сохранения)
        findViewById(R.id.profile_shape).setOnClickListener(v -> {
            navigateBackToProfile();
        });

        findViewById(R.id.main_house_shape).setOnClickListener(v -> {
            Intent intent = new Intent(this, DayDetailActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        findViewById(R.id.heart_shape).setOnClickListener(v -> {
            Intent intent = new Intent(this, Sohranenki.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        originalUserData = dbHelper.getUserInfo(userId);
        User user = dbHelper.getUserInfo(userId);
        if (originalUserData != null) {
            infoUsername.setText(user.getLogin() != null ? "@" + user.getLogin() : getString(R.string.not_available_short)); // Используем строку
            infoName.setText(user.getName() != null ? user.getName() : "Не указано");
            infoDob.setText(user.getBirthDate() != null ? formatBirthDate(user.getBirthDate()) : "Не указана");
            infoUsernameInCard.setText(user.getLogin() != null ? user.getLogin() : "Не указано");

        } else {
            Toast.makeText(this, "Не удалось загрузить данные пользователя", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveProfileChanges() {
        // Получаем новые значения из полей ввода
        String newName = infoName.getText().toString().trim();
        String newDob = infoDob.getText().toString().trim();
        String newUsername = infoUsernameInCard.getText().toString().trim();

        // Проверка на пустые поля
        if (newName.isEmpty() || newDob.isEmpty() || newUsername.isEmpty()) {
            Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка, изменились ли данные
        if (newName.equals(originalUserData.getName()) &&
                newDob.equals(originalUserData.getBirthDate()) &&
                newUsername.equals(originalUserData.getLogin())) {
            Toast.makeText(this, "Данные не изменились", Toast.LENGTH_SHORT).show();
            navigateBackToProfile();
            return;
        }

        // Создаем объект User с обновленными данными
        User updatedUser = new User();
        updatedUser.setUserId(userId);
        updatedUser.setName(newName);
        updatedUser.setBirthDate(newDob);
        updatedUser.setLogin(newUsername);
        updatedUser.setGender(originalUserData.getGender());
        updatedUser.setCity(originalUserData.getCity());
        updatedUser.setLanguage(originalUserData.getLanguage());
        updatedUser.setAvatarResId(originalUserData.getAvatarResId());

        // Обновляем данные через DatabaseHelper
        boolean isUpdated = dbHelper.updateUser(updatedUser);

        if (isUpdated) {
            Toast.makeText(this, "Данные успешно сохранены", Toast.LENGTH_SHORT).show();
            navigateBackToProfile();
        } else {
            Toast.makeText(this, "Ошибка при сохранении данных", Toast.LENGTH_SHORT).show();
        }

    }

    private void navigateBackToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private String formatBirthDate(String rawDate) {
        // Простой форматировщик даты (можно заменить на более сложный)
        if (rawDate == null || rawDate.isEmpty()) {
            return "Не указана";
        }
        return rawDate; // или преобразуйте формат по вашему усмотрению
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}