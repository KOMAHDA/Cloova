package com.example.cloova;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cloova.adapter.PlannedOutfitsAdapter;
import com.example.cloova.model.PlannedOutfit;

import java.util.ArrayList;
import java.util.List;

public class Zaplanerki extends AppCompatActivity implements PlannedOutfitsAdapter.OnItemDeleteListener {

    private static final String TAG = "ZaplanerkiActivity";

    private DatabaseHelper dbHelper;
    private long currentUserId = DatabaseHelper.DEFAULT_USER_ID;

    private RecyclerView recyclerView;
    private PlannedOutfitsAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyMessage;

    private ImageView navProfileIcon, navHomeIcon, navFavoritesIcon;
    private ImageView gobackbutton;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zaplanerki);
        Log.d(TAG, "onCreate: Activity starting.");

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupNavigationListeners();

        retrieveUserIdAndLoadOutfits();
    }

    @Override
    protected void onResume() {
        super.onResume();
        retrieveUserIdAndLoadOutfits();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_planned_outfits);
        progressBar = findViewById(R.id.zaplanerki_progress_bar);
        emptyMessage = findViewById(R.id.zaplanerki_empty_message);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlannedOutfitsAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Инициализация кнопок навигации
        gobackbutton = findViewById(R.id.gobackbutton); // ID из item_planned_outfit
        navProfileIcon = findViewById(R.id.nav_profile_icon_zaplanerki); // ID из item_planned_outfit
        navHomeIcon = findViewById(R.id.nav_home_icon_zaplanerki); // ID из item_planned_outfit
        navFavoritesIcon = findViewById(R.id.nav_favorites_icon_zaplanerki); // ID из item_planned_outfit
    }

    private void setupNavigationListeners() {
        gobackbutton.setOnClickListener(v -> finish()); // Просто закрываем


        navProfileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        navHomeIcon.setOnClickListener(v -> {
            // !!! НОВАЯ ЛОГИКА ДЛЯ ПЕРЕДАЧИ ДАННЫХ В WEATHERFORECASTACTIVITY !!!
            SharedPreferences prefs = getSharedPreferences(DatabaseHelper.SHARED_PREFS_NAME, MODE_PRIVATE);
            long userId = prefs.getLong(DatabaseHelper.PREF_KEY_LOGGED_IN_USER_ID, DatabaseHelper.DEFAULT_USER_ID);

            if (userId != DatabaseHelper.DEFAULT_USER_ID) {
                new GetUserAndNavigateTask().execute(userId);
            } else {
                Toast.makeText(this, R.string.error_auth, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "navHomeIcon clicked: User not logged in.");
            }
        });

        navFavoritesIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, Sohranenki.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }

    private void retrieveUserIdAndLoadOutfits() {
        SharedPreferences prefs = getSharedPreferences(DatabaseHelper.SHARED_PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getLong(DatabaseHelper.PREF_KEY_LOGGED_IN_USER_ID, DatabaseHelper.DEFAULT_USER_ID);

        if (currentUserId != DatabaseHelper.DEFAULT_USER_ID) {
            new LoadPlannedOutfitsTask().execute(currentUserId);
        } else {
            Log.e(TAG, "retrieveUserIdAndLoadOutfits: Invalid userId, cannot load planned outfits.");
            Toast.makeText(this, R.string.error_auth, Toast.LENGTH_SHORT).show();
            emptyMessage.setText(R.string.error_auth);
            emptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDeleteClick(PlannedOutfit outfitToDelete) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_planned_confirmation_title)
                .setMessage(R.string.delete_planned_confirmation_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> new DeletePlannedOutfitTask().execute(outfitToDelete.getId()))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadPlannedOutfitsTask extends AsyncTask<Long, Void, List<PlannedOutfit>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            emptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }

        @Override
        protected List<PlannedOutfit> doInBackground(Long... userIds) {
            long userId = userIds[0];
            return dbHelper.getPlannedOutfits(userId);
        }

        @Override
        protected void onPostExecute(@Nullable List<PlannedOutfit> result) {
            progressBar.setVisibility(View.GONE);
            if (result != null && !result.isEmpty()) {
                adapter.updateData(result);
                recyclerView.setVisibility(View.VISIBLE);
                emptyMessage.setVisibility(View.GONE);
                Log.d(TAG, "Loaded " + result.size() + " planned outfits.");
            } else {
                adapter.updateData(new ArrayList<>());
                emptyMessage.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Log.d(TAG, "No planned outfits found.");
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DeletePlannedOutfitTask extends AsyncTask<Long, Void, Boolean> {
        private long outfitIdToDelete;

        @Override
        protected Boolean doInBackground(Long... params) {
            outfitIdToDelete = params[0];
            return dbHelper.deletePlannedOutfit(outfitIdToDelete);
        }

        @Override
        protected void onPostExecute(Boolean isDeleted) {
            if (isDeleted) {
                Toast.makeText(Zaplanerki.this, R.string.planned_outfit_delete_successful, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Planned outfit with ID " + outfitIdToDelete + " deleted successfully.");
                retrieveUserIdAndLoadOutfits();
            } else {
                Toast.makeText(Zaplanerki.this, R.string.planned_outfit_delete_failed, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to delete planned outfit with ID: " + outfitIdToDelete);
            }
        }
    }

    // --- НОВЫЙ ASYNCTASK ДЛЯ ПОЛУЧЕНИЯ ДАННЫХ ПОЛЬЗОВАТЕЛЯ И НАВИГАЦИИ ---
    @SuppressLint("StaticFieldLeak")
    private class GetUserAndNavigateTask extends AsyncTask<Long, Void, UserDataForNavigation> {
        @Override
        protected UserDataForNavigation doInBackground(Long... userIds) {
            long userId = userIds[0];
            User user = dbHelper.getUserInfo(userId);
            String city = null;
            String style = null;

            if (user != null) {
                city = user.getCity();
                List<String> userStyles = dbHelper.getUserStyles(userId);
                if (userStyles != null && !userStyles.isEmpty()) {
                    style = userStyles.get(0); // Берем первый стиль пользователя
                }
            }
            return new UserDataForNavigation(city, style);
        }

        @Override
        protected void onPostExecute(@Nullable UserDataForNavigation result) {
            if (result != null && result.city != null && !result.city.isEmpty()) {
                Intent intent = new Intent(Zaplanerki.this, WeatherForecastActivity.class);
                intent.putExtra(WeatherForecastActivity.EXTRA_CITY_NAME, result.city);
                intent.putExtra(WeatherForecastActivity.EXTRA_USER_STYLE, result.style != null ? result.style : "Повседневный");
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            } else {
                Toast.makeText(Zaplanerki.this, R.string.error_city_not_found, Toast.LENGTH_SHORT).show();
                Log.w(TAG, "GetUserAndNavigateTask: City or user data not found for navigation.");
            }
        }
    }

    // Вспомогательный класс для передачи данных
    private static class UserDataForNavigation {
        String city;
        String style;

        UserDataForNavigation(String city, String style) {
            this.city = city;
            this.style = style;
        }
    }
}