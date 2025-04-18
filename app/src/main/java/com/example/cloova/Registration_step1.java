package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.util.Calendar;
import android.text.InputType;

import java.util.ArrayList;
import java.util.List;
import android.text.method.DigitsKeyListener;

public class Registration_step1 extends AppCompatActivity {

    private EditText nameEditText, dateEditText;
    private Spinner citySpinner;
    private Spinner sexSpinner;
    private Button goBackButton;
    private Button goNextButton;
    private Button selectAvatarButton;
    private ImageView avatarImageView;
    private int selectedAvatarResId = 0; // ID выбранного аватара
    private List<String> selectedStyles = new ArrayList<>(); // Список выбранных стилей

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_step1);

        nameEditText = findViewById(R.id.nameEditText);
        dateEditText = findViewById(R.id.dateEditText);
        dateEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        dateEditText.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        dateEditText.addTextChangedListener(new DateTextWatcher());
        citySpinner = findViewById(R.id.citySpinner);
        sexSpinner = findViewById(R.id.sexSpinner);
        goBackButton = findViewById(R.id.gobackbutton);
        goNextButton = findViewById(R.id.gonextbutton);
        selectAvatarButton = findViewById(R.id.selectAvatarButton);
        avatarImageView = findViewById(R.id.avatarImageView);

        setupSexSpinner();
        setupCitySpinner();

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

        selectAvatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAvatarSelectionDialog();
            }
        });

    }

    public void Back(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void Next(View v) {
        Intent intent = new Intent(this, Registration_step2.class);
        intent.putExtra("name", nameEditText.getText().toString());
        intent.putExtra("city", citySpinner.getSelectedItem().toString());  // Передаём выбранный город
        intent.putStringArrayListExtra("styles", new ArrayList<>(selectedStyles)); // Список стилей
        intent.putExtra("avatar", selectedAvatarResId); // Передаем ID выбранного аватара
        startActivity(intent);
    }

    private void setupSexSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.sex_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(adapter);
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

    private void showAvatarSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_avatar_selection, null);
        builder.setView(dialogView);

        // Получаем массив аватаров из ресурсов
        final int[] avatarResources = {
                R.drawable.default_avatar1,
                R.drawable.default_avatar2,
                R.drawable.default_avatar3,
                R.drawable.default_avatar4,
                R.drawable.default_avatar5
        };

        LinearLayout avatarsContainer = dialogView.findViewById(R.id.avatarsContainer);

        // Создаем сетку аватаров (3 в строку)
        LinearLayout currentRow = null;
        for (int i = 0; i < avatarResources.length; i++) {
            if (i % 3 == 0) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                avatarsContainer.addView(currentRow);
            }

            final ImageView avatarImage = new ImageView(this);
            // Устанавливаем аватар в круглую форму
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), avatarResources[i]);
            Bitmap circularBitmap = getCircularBitmap(bitmap);
            avatarImage.setImageBitmap(circularBitmap);

            // Параметры для изображения
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(80), // ширина
                    dpToPx(80)  // высота
            );
            params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            avatarImage.setLayoutParams(params);

            final int position = i;
            avatarImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedAvatarResId = avatarResources[position];
                    // Устанавливаем выбранный аватар
                    Bitmap selectedBitmap = BitmapFactory.decodeResource(getResources(), selectedAvatarResId);
                    avatarImageView.setImageBitmap(getCircularBitmap(selectedBitmap));
                    ((AlertDialog) v.getTag()).dismiss();
                }
            });

            currentRow.addView(avatarImage);
        }

        AlertDialog dialog = builder.create();
        // Сохраняем ссылку на диалог для закрытия при выборе аватара
        for (int i = 0; i < avatarsContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) avatarsContainer.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                row.getChildAt(j).setTag(dialog);
            }
        }

        dialog.show();
    }

    // Преобразование Bitmap в круглую форму
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(output);

        final int color = 0xff424242;
        final android.graphics.Paint paint = new android.graphics.Paint();
        final android.graphics.Rect rect = new android.graphics.Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    // Преобразование dp в пиксели
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    // для ввода даты
    private class DateTextWatcher implements TextWatcher {
        private boolean isFormatting;

        private boolean isDeleting;
        private String current = "";
        private int lastValidLength = 0;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            isDeleting = count > after;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (isFormatting) return;
            isFormatting = true;

            String original = s.toString();
            String cleaned = original.replaceAll("[^\\d]", "");
            StringBuilder formatted = new StringBuilder();

            // Форматируем с точками
            for (int i = 0; i < cleaned.length(); i++) {
                if (i == 2 || i == 4) formatted.append(".");
                formatted.append(cleaned.charAt(i));
            }

            // Устанавливаем новый текст
            if (!original.equals(formatted.toString())) {
                s.replace(0, s.length(), formatted);
            }

            // Всегда перемещаем курсор в конец
            dateEditText.setSelection(formatted.length());

            // Валидация
            validateDate(formatted.toString());

            isFormatting = false;
        }

        private void validateDate(String date) {
            String[] parts = date.split("\\.");

            try {
                if (parts.length >= 1 && parts[0].length() == 2) {
                    int day = Integer.parseInt(parts[0]);
                    if (day < 1 || day > 31) {
                        dateEditText.setError("День должен быть 1-31");
                        return;
                    }
                }

                if (parts.length >= 2 && parts[1].length() == 2) {
                    int month = Integer.parseInt(parts[1]);
                    if (month < 1 || month > 12) {
                        dateEditText.setError("Месяц должен быть 1-12");
                        return;
                    }
                }

                if (parts.length >= 3 && parts[2].length() == 4) {
                    int year = Integer.parseInt(parts[2]);
                    if (year < 1900 || year > 2100) {
                        dateEditText.setError("Год должен быть 1900-2100");
                        return;
                    }
                }

                dateEditText.setError(null);
            } catch (NumberFormatException e) {
                dateEditText.setError("Некорректная дата");
            }
        }
    }

    private boolean isDataValid() {
        if (nameEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Введите имя", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (citySpinner.getSelectedItemPosition() == 0) {  // Проверка выбора города
            Toast.makeText(this, "Выберите город", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedAvatarResId == 0) {
            Toast.makeText(this, "Выберите аватар", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (dateEditText.getText().toString().trim().length() < 10) {
            dateEditText.setError("Введите полную дату в формате ДД.ММ.ГГГГ");
            return false;
        }
        if (dateEditText.getError() != null) {
            return false;
        }
        return true;
    }
}

