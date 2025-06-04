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

public class Sohranenki extends AppCompatActivity implements SavedOutfitsAdapter.OnItemDeleteListener {

    private static final String TAG = "SohranenkiActivity";

    private DatabaseHelper dbHelper;
    private long currentUserId = DatabaseHelper.DEFAULT_USER_ID;

    private RecyclerView recyclerView;
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

        retrieveUserIdAndLoadOutfits();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_saved_outfits);
        progressBar = findViewById(R.id.sohranenki_progress_bar);
        emptyMessage = findViewById(R.id.sohranenki_empty_message);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SavedOutfitsAdapter adapter = new SavedOutfitsAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);


        gobackbutton = findViewById(R.id.gobackbutton);
        navProfileIcon = findViewById(R.id.nav_profile_icon_sohranenki);
        navHomeIcon = findViewById(R.id.nav_home_icon_sohranenki);
        navFavoritesIcon = findViewById(R.id.nav_favorites_icon_sohranenki);
    }

    private void setupNavigationListeners() {
        gobackbutton.setOnClickListener(v -> finish());
        navProfileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        navHomeIcon.setOnClickListener(v -> {

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

            retrieveUserIdAndLoadOutfits();
            Toast.makeText(this, R.string.saved_looks_title_updated, Toast.LENGTH_SHORT).show();
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
                ((SavedOutfitsAdapter) recyclerView.getAdapter()).updateData(result);
                recyclerView.setVisibility(View.VISIBLE);
                emptyMessage.setVisibility(View.GONE);
                Log.d(TAG, "Loaded " + result.size() + " saved outfits.");
            } else {
                ((SavedOutfitsAdapter) recyclerView.getAdapter()).updateData(new ArrayList<>());
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
                retrieveUserIdAndLoadOutfits();
            } else {
                Toast.makeText(Sohranenki.this, R.string.outfit_delete_failed, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to delete outfit with ID: " + outfitIdToDelete);
            }
        }
    }


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
                    style = userStyles.get(0);
                }
            }
            return new UserDataForNavigation(city, style);
        }

        @Override
        protected void onPostExecute(@Nullable UserDataForNavigation result) {
            if (result != null && result.city != null && !result.city.isEmpty()) {
                Intent intent = new Intent(Sohranenki.this, WeatherForecastActivity.class);
                intent.putExtra(WeatherForecastActivity.EXTRA_CITY_NAME, result.city);
                intent.putExtra(WeatherForecastActivity.EXTRA_USER_STYLE, result.style != null ? result.style : "Повседневный");
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            } else {
                Toast.makeText(Sohranenki.this, R.string.error_city_not_found, Toast.LENGTH_SHORT).show();
                Log.w(TAG, "GetUserAndNavigateTask: City or user data not found for navigation.");
            }
        }
    }


    private static class UserDataForNavigation {
        String city;
        String style;

        UserDataForNavigation(String city, String style) {
            this.city = city;
            this.style = style;
        }
    }
}