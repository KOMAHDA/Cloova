package com.example.cloova;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
public class ProfileFile extends AppCompatActivity {
    private ImageView imageViewProfile;
    private TextView textViewName;
    private TextView textViewCity;
    private TextInputEditText editTextUserStyle;
    private AutoCompleteTextView autoCompleteTextViewColors;
    private AutoCompleteTextView autoCompleteTextViewClothes;
    private MaterialButton buttonCreateProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_file);

        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewName = findViewById(R.id.textViewName);
        textViewCity = findViewById(R.id.textViewCity);
        editTextUserStyle = findViewById(R.id.editTextUserStyle);
        autoCompleteTextViewColors = findViewById(R.id.autoCompleteTextViewColors);
        autoCompleteTextViewClothes = findViewById(R.id.autoCompleteTextViewClothes);
        buttonCreateProfile = findViewById(R.id.buttonCreateProfile);

        setupDropdowns();
        buttonCreateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createProfile();
            }
        });
    }

    private void setupDropdowns() { //Выпадающие штуки
        String[] colors = {"Красный", "Зеленый", "Синий", "Желтый", "Белый", "Черный"};

        ArrayAdapter<String> colorsAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, colors);
        autoCompleteTextViewColors.setAdapter(colorsAdapter);

        String[] clothes = {"Футболка", "Джинсы", "Платье", "Юбка", "Рубашка", "Брюки", "Пиджак"};
        ArrayAdapter<String> clothesAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, clothes);
        autoCompleteTextViewClothes.setAdapter(clothesAdapter);
    }

        private void createProfile() {
            String name = textViewName.getText().toString();
            String city = textViewCity.getText().toString();
            String style = editTextUserStyle.getText().toString();
            String selectedColor = autoCompleteTextViewColors.getText().toString();
            String selectedClothes = autoCompleteTextViewClothes.getText().toString();

            String profileData = "Имя: " + name + "\nГород: " + city + "\nСтиль: " + style +
                    "\nЦвет: " + selectedColor + "\nОдежда: " + selectedClothes;

            Toast.makeText(this, profileData, Toast.LENGTH_LONG).show();
        }
}
