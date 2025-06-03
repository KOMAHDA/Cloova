package com.example.cloova;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

public class SelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        String title = getIntent().getStringExtra("dialog_title");
        final int buttonId = getIntent().getIntExtra("button_id", -1);

        setTitle(title);

        ListView listView = findViewById(R.id.list_view);
        String[] items = getItemsForTitle(title);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_value", items[position]);
            resultIntent.putExtra("button_id", buttonId);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private String[] getItemsForTitle(String title) {

        if (title.equals(getString(R.string.profile_colors_label))) {
            return new String[]{"Красный", "Синий", "Зеленый", "Черный", "Белый"};
        } else if (title.equals(getString(R.string.nolove))) {
            return new String[]{"Розовый", "Оранжевый", "Фиолетовый", "Коричневый"};
        } else if (title.equals(getString(R.string.profile_clothes_label))) {
            return new String[]{"Комфорт", "Стиль", "Универсальность", "Яркость"};
        } else if (title.equals(getString(R.string.profile_accessories_question))) {
            return new String[]{"Очки", "Галстук", "Сережки", "Ремень"};
        } else if (title.equals(getString(R.string.profile_clothes_hint))) {
            return new String[]{"Футболка", "Рубашка", "Джинсы", "Шорты", "Брюки"};
        } else if (title.equals(getString(R.string.sochetanie))) {
            return new String[]{"Футболка и рубашка", "Юбка и худи", "Колготки и шорты", "Брюки и майка"};
        }else if (title.equals(getString(R.string.situazia))) {
            return new String[]{"Учеба", "Работа", "Вечеринка", "Прогулка", "Дача"};
        } else {
            return new String[]{"Комфорт", "Внешний вид"};
        }
    }
}