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

import com.example.cloova.adapter.SavedOutfitsAdapter;
import com.example.cloova.model.SavedOutfit;

import java.util.ArrayList;
import java.util.List;

public class Sohranenki extends AppCompatActivity implements SavedOutfitsAdapter.OnItemDeleteListener { // Имплементируем интерфейс

    private static final String TAG = "SohranenkiActivity";

    private DatabaseHelper dbHelper;
    private long currentUserId = DatabaseHelper.DEFAULT_USER_ID;

    private RecyclerView recyclerView;
    private SavedOutfitsAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyMessage;

    // UI для нижней навигации
    private ImageView navProfileIcon, navHomeIcon, navFavoritesIcon;
    private ImageView gobackbutton;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sohranenki);
        Log.d(TAG, "onCreate: Activity starting.");

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupNavigationListeners();

        retrieveUserIdAndLoadOutfits();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Перезагружаем данные каждый раз, когда активность становится видимой (например, после возвращения из DayDetailActivity)
        retrieveUserIdAndLoadOutfits();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_saved_outfits);
        progressBar = findViewById(R.id.sohranenki_progress_bar);
        emptyMessage = findViewById(R.id.sohranenki_empty_message);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SavedOutfitsAdapter(this, new ArrayList<>(), this); // Передаем this как deleteListener
        recyclerView.setAdapter(adapter);

        // Инициализация кнопок навигации
        gobackbutton = findViewById(R.id.gobackbutton);
        navProfileIcon = findViewById(R.id.nav_profile_icon_sohranenki);
        navHomeIcon = findViewById(R.id.nav_home_icon_sohranenki);
        navFavoritesIcon = findViewById(R.id.nav_favorites_icon_sohranenki);
    }

    private void setupNavigationListeners() {
        gobackbutton.setOnClickListener(v -> finish()); // Просто закрываем

        navProfileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        navHomeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, WeatherForecastActivity.class);
            // Если нужно передать город или стиль, добавьте extra здесь
            // intent.putExtra(WeatherForecastActivity.EXTRA_CITY_NAME, "...");
            // intent.putExtra(WeatherForecastActivity.EXTRA_USER_STYLE, "...");
            startActivity(intent);
        });
        navFavoritesIcon.setOnClickListener(v -> {
            // Мы уже здесь, ничего не делаем, но можно обновить список
            retrieveUserIdAndLoadOutfits();
        });
    }

    private void retrieveUserIdAndLoadOutfits() {
        SharedPreferences prefs = getSharedPreferences(DatabaseHelper.SHARED_PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getLong(DatabaseHelper.PREF_KEY_LOGGED_IN_USER_ID, DatabaseHelper.DEFAULT_USER_ID);

        if (currentUserId != DatabaseHelper.DEFAULT_USER_ID) {
            new LoadSavedOutfitsTask().execute(currentUserId);
        } else {
            Log.e(TAG, "retrieveUserIdAndLoadOutfits: Invalid userId, cannot load saved outfits.");
            Toast.makeText(this, R.string.error_auth, Toast.LENGTH_SHORT).show();
            emptyMessage.setText(R.string.error_auth);
            emptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDeleteClick(SavedOutfit outfitToDelete) {
        // Показываем диалог подтверждения перед удалением
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirmation_title)
                .setMessage(R.string.delete_confirmation_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> new DeleteOutfitTask().execute(outfitToDelete.getId()))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadSavedOutfitsTask extends AsyncTask<Long, Void, List<SavedOutfit>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            emptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }

        @Override
        protected List<SavedOutfit> doInBackground(Long... userIds) {
            long userId = userIds[0];
            return dbHelper.getSavedOutfits(userId);
        }

        @Override
        protected void onPostExecute(@Nullable List<SavedOutfit> result) {
            progressBar.setVisibility(View.GONE);
            if (result != null && !result.isEmpty()) {
                adapter.updateData(result);
                recyclerView.setVisibility(View.VISIBLE);
                emptyMessage.setVisibility(View.GONE);
                Log.d(TAG, "Loaded " + result.size() + " saved outfits.");
            } else {
                adapter.updateData(new ArrayList<>()); // Очищаем список
                emptyMessage.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Log.d(TAG, "No saved outfits found.");
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteOutfitTask extends AsyncTask<Long, Void, Boolean> {
        private long outfitIdToDelete;

        @Override
        protected Boolean doInBackground(Long... params) {
            outfitIdToDelete = params[0];
            return dbHelper.deleteSavedOutfit(outfitIdToDelete);
        }

        @Override
        protected void onPostExecute(Boolean isDeleted) {
            if (isDeleted) {
                Toast.makeText(Sohranenki.this, R.string.outfit_delete_successful, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Outfit with ID " + outfitIdToDelete + " deleted successfully.");
                retrieveUserIdAndLoadOutfits(); // Перезагружаем список после удаления
            } else {
                Toast.makeText(Sohranenki.this, R.string.outfit_delete_failed, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to delete outfit with ID: " + outfitIdToDelete);
            }
        }
    }
}