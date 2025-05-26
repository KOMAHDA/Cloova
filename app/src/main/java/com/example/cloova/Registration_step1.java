package com.example.cloova;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.widget.AutoCompleteTextView;

import android.view.ViewGroup;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.graphics.drawable.GradientDrawable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import android.text.method.DigitsKeyListener;

import com.yandex.mapkit.geometry.Point;

public class Registration_step1 extends AppCompatActivity {

    private EditText nameEditText, dateEditText;
    private AutoCompleteTextView cityInput;
    private Spinner sexSpinner;
    private ImageView goBackButton;
    private Button goNextButton;
    private Button selectAvatarButton;
    private ImageView avatarImageView;
    private int selectedAvatarResId = R.drawable.default_avatar1;
    private List<String> selectedStyles = new ArrayList<>();
    private List<String> popularCities = Arrays.asList("Москва", "Санкт-Петербург", "Самара", "Казань", "Новосибирск");

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_step1);

        nameEditText = findViewById(R.id.nameEditText);
        dateEditText = findViewById(R.id.dateEditText);
        dateEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        dateEditText.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        dateEditText.addTextChangedListener(new DateTextWatcher());
        cityInput = findViewById(R.id.city_input);
        sexSpinner = findViewById(R.id.sexSpinner);
        goBackButton = findViewById(R.id.gobackbutton);
        goNextButton = findViewById(R.id.gonextbutton);
        selectAvatarButton = findViewById(R.id.selectAvatarButton);
        avatarImageView = findViewById(R.id.avatarImageView);

        setupSexSpinner();
        setupCityInput(); // Заменил setupCitySpinner()

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
                    // Проверяем город перед переходом
                    new CityValidationTask().execute(cityInput.getText().toString().trim());
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

    private void setupCityInput() {
        // Настройка автозаполнения для города
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, popularCities);
        cityInput.setAdapter(adapter);
        cityInput.setThreshold(1);
        cityInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        cityInput.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
    }

    private com.yandex.mapkit.geometry.Point getCoordinatesFromAndroidGeocoder(String cityName) {
        if (!Geocoder.isPresent()) {
            return null;
        }
        Geocoder geocoder = new Geocoder(this, new Locale("ru", "RU"));
        try {
            List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return new Point(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Error getting coordinates", e);
        }
        return null;
    }

    private String getValidCityName(String inputCityName) {
        if (!Geocoder.isPresent()) {
            return null;
        }
        Geocoder geocoder = new Geocoder(this, new Locale("ru", "RU"));
        try {
            List<Address> addresses = geocoder.getFromLocationName(inputCityName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Возвращаем официальное название города из геокодера
                return address.getLocality();
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Error getting city name", e);
        }
        return null;
    }

    private class CityValidationTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String inputCity = params[0];
            return getValidCityName(inputCity);
        }

        @Override
        protected void onPostExecute(String validCityName) {
            if (validCityName != null) {
                // Город найден, обновляем поле ввода и переходим дальше
                cityInput.setText(validCityName);
                Next();
            } else {
                Toast.makeText(Registration_step1.this,
                        "Город не найден. Пожалуйста, уточните название", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void Back(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void Next() {
        Intent intent = new Intent(this, Registration_step2.class);
        intent.putExtra("name", nameEditText.getText().toString());
        intent.putExtra("gender", sexSpinner.getSelectedItem().toString());
        intent.putExtra("birthDate", dateEditText.getText().toString());
        intent.putExtra("city", cityInput.getText().toString()); // Теперь берем из AutoCompleteTextView
        intent.putExtra("avatar", selectedAvatarResId);
        startActivity(intent);
    }

    private void setupSexSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.sex_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexSpinner.setAdapter(adapter);
    }

    private void showAvatarSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_avatar_selection, null);
        builder.setView(dialogView);

        final int[] avatarResources = {
                R.drawable.default_avatar1,
                R.drawable.default_avatar2,
                R.drawable.default_avatar3,
                R.drawable.default_avatar4,
                R.drawable.default_avatar5,
                R.drawable.default_avatar6
        };

        LinearLayout avatarsContainer = dialogView.findViewById(R.id.avatarsContainer);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        for (int i = 0; i < 2; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER);

            for (int j = 0; j < 3; j++) {
                int index = i * 3 + j;
                if (index >= avatarResources.length) break;

                ImageView avatarImage = new ImageView(this);
                int size = dpToPx(80);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
                avatarImage.setLayoutParams(params);

                GradientDrawable mask = new GradientDrawable();
                mask.setShape(GradientDrawable.OVAL);
                mask.setSize(size, size);
                mask.setStroke(dpToPx(2), Color.LTGRAY);
                avatarImage.setBackground(mask);
                avatarImage.setClipToOutline(true);
                avatarImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                avatarImage.setImageResource(avatarResources[index]);

                final int selectedIndex = index;
                avatarImage.setOnClickListener(v -> {
                    selectedAvatarResId = avatarResources[selectedIndex];
                    updateMainAvatar(selectedAvatarResId);
                    dialog.dismiss();
                });

                row.addView(avatarImage);
            }

            avatarsContainer.addView(row);
        }

        dialog.show();
    }

    private void updateMainAvatar(int avatarResId) {
        GradientDrawable mask = new GradientDrawable();
        mask.setShape(GradientDrawable.OVAL);
        mask.setStroke(dpToPx(2), Color.LTGRAY);

        avatarImageView.setBackground(mask);
        avatarImageView.setClipToOutline(true);
        avatarImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatarImageView.setImageResource(avatarResId);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

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

            for (int i = 0; i < cleaned.length(); i++) {
                if (i == 2 || i == 4) formatted.append(".");
                formatted.append(cleaned.charAt(i));
            }

            if (!original.equals(formatted.toString())) {
                s.replace(0, s.length(), formatted);
            }

            dateEditText.setSelection(formatted.length());
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
        if (cityInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Введите город", Toast.LENGTH_SHORT).show();
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