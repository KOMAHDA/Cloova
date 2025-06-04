package com.example.cloova;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final int REQUEST_CODE_SELECT_STYLE = 101;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private long userId;
    private DatabaseHelper dbHelper;
    private EditText infoName, infoDob, infoUsernameInCard;
    private TextView profileUsername;
    private User originalUserData;
    private int selectedAvatarResId;

    private ImageView ivAvatar;
    private ImageButton btnChangeAvatar;
    private TextView btnSelectStyle;


    private List<String> currentSelectedStyles = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_edit_profile);

            dbHelper = new DatabaseHelper(this);

            userId = getIntent().getLongExtra("USER_ID", -1);
            if (userId == -1) {
                Toast.makeText(this, R.string.error_auth, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            infoName = findViewById(R.id.info_name);
            infoDob = findViewById(R.id.info_dob);
            infoUsernameInCard = findViewById(R.id.info_username_in_card);
            profileUsername = findViewById(R.id.profile_username);

            ivAvatar = findViewById(R.id.iv_avatar);
            btnChangeAvatar = findViewById(R.id.btn_change_avatar);
            btnSelectStyle = findViewById(R.id.btn_select_style);

            loadUserData();


            btnChangeAvatar.setOnClickListener(v -> showAvatarSelectionDialog());
            btnSelectStyle.setOnClickListener(v -> openStyleSelectionDialog());

            Button saveButton = findViewById(R.id.save_button);
            saveButton.setOnClickListener(v -> saveProfileChanges());

        } catch (Exception e) {
            Toast.makeText(this, R.string.error_loading_profile, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            e.printStackTrace();
            finish();
        }

        findViewById(R.id.gobackbutton).setOnClickListener(v -> navigateBackToProfile());
        findViewById(R.id.profile_shape).setOnClickListener(v -> navigateBackToProfile());

        findViewById(R.id.main_house_shape).setOnClickListener(v -> {
            String city = originalUserData != null && originalUserData.getCity() != null ? originalUserData.getCity() : WeatherForecastActivity.FALLBACK_CITY;

            String style = currentSelectedStyles != null && !currentSelectedStyles.isEmpty() ? currentSelectedStyles.get(0) : "Повседневный";

            Intent intent = new Intent(this, WeatherForecastActivity.class);
            intent.putExtra(WeatherForecastActivity.EXTRA_CITY_NAME, city);
            intent.putExtra(WeatherForecastActivity.EXTRA_USER_STYLE, style);
            startActivity(intent);
        });

        findViewById(R.id.heart_shape).setOnClickListener(v -> {
            Intent intent = new Intent(this, Sohranenki.class);
            startActivity(intent);
        });

        TextView deleteProfileButton = findViewById(R.id.delete_profile);
        deleteProfileButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void loadUserData() {
        originalUserData = dbHelper.getUserInfo(userId);
        if (originalUserData != null) {
            profileUsername.setText(originalUserData.getLogin() != null ? "@" + originalUserData.getLogin() : getString(R.string.not_available_short));
            infoName.setText(originalUserData.getName() != null ? originalUserData.getName() : "");
            infoDob.setText(originalUserData.getBirthDate() != null ? originalUserData.getBirthDate() : "");
            infoUsernameInCard.setText(originalUserData.getLogin() != null ? originalUserData.getLogin() : "");

            selectedAvatarResId = originalUserData.getAvatarResId();
            if (selectedAvatarResId != 0) {
                updateMainAvatar(selectedAvatarResId);
            } else {
                updateMainAvatar(R.drawable.default_avatar1);
            }


            currentSelectedStyles = dbHelper.getUserStyles(userId);
            updateStyleButtonText();

        } else {
            Toast.makeText(this, R.string.error_loading_user_data, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateStyleButtonText() {
        if (currentSelectedStyles != null && !currentSelectedStyles.isEmpty()) {
            if (currentSelectedStyles.size() == 1) {
                btnSelectStyle.setText(currentSelectedStyles.get(0));
            } else {
                btnSelectStyle.setText(getString(R.string.selected_count, currentSelectedStyles.size()));
            }
        } else {
            btnSelectStyle.setText(getString(R.string.profile_style_label_not_selected));
        }
    }


    private void saveProfileChanges() {
        String newName = infoName.getText().toString().trim();
        String newDob = infoDob.getText().toString().trim();
        String newUsername = infoUsernameInCard.getText().toString().trim();

        if (newName.isEmpty() || newDob.isEmpty() || newUsername.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }


        boolean profileChanged = !newName.equals(originalUserData.getName()) ||
                !newDob.equals(originalUserData.getBirthDate()) ||
                !newUsername.equals(originalUserData.getLogin()) ||
                selectedAvatarResId != originalUserData.getAvatarResId();




        boolean stylesChanged = false;
        List<String> originalStyles = originalUserData.getStyles();
        if (originalStyles == null) originalStyles = new ArrayList<>();

        if (currentSelectedStyles.size() != originalStyles.size() || !currentSelectedStyles.containsAll(originalStyles) || !originalStyles.containsAll(currentSelectedStyles)) {
            stylesChanged = true;
        }


        if (!profileChanged && !stylesChanged) {
            Toast.makeText(this, R.string.data_not_changed, Toast.LENGTH_SHORT).show();
            navigateBackToProfile();
            return;
        }

        User updatedUser = new User();
        updatedUser.setUserId(userId);
        updatedUser.setName(newName);
        updatedUser.setBirthDate(newDob);
        updatedUser.setLogin(newUsername);
        updatedUser.setGender(originalUserData.getGender());
        updatedUser.setCity(originalUserData.getCity());
        updatedUser.setLanguage(originalUserData.getLanguage());
        updatedUser.setAvatarResId(selectedAvatarResId);

        boolean isProfileUpdated = dbHelper.updateUser(updatedUser);
        boolean isStylesUpdated = true;

        if (stylesChanged) {
            dbHelper.deleteUserStyles(userId);
            if (!currentSelectedStyles.isEmpty()) {
                dbHelper.addUserStyles(userId, currentSelectedStyles);
            }
            Log.d(TAG, "User styles updated. New styles: " + currentSelectedStyles.toString());
        }

        if (isProfileUpdated || stylesChanged) {
            Toast.makeText(this, R.string.saved_successfully, Toast.LENGTH_SHORT).show();
            navigateBackToProfile();
        } else {
            Toast.makeText(this, R.string.error_saving_data, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateBackToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    private void showAvatarSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_avatar_selection, null);
        builder.setView(dialogView);

        final int[] avatarResources = {
                R.drawable.default_avatar1,
                R.drawable.default_avatar2,
                R.drawable.default_avatar3
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

        ivAvatar.setBackground(mask);
        ivAvatar.setClipToOutline(true);
        ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivAvatar.setImageResource(avatarResId);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }



    private void openStyleSelectionDialog() {
        List<String> availableStyles = dbHelper.getAllStylesFromCatalog();
        if (availableStyles.isEmpty()) {
            Toast.makeText(this, R.string.no_styles_available, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = availableStyles.toArray(new String[0]);
        boolean[] checkedItems = new boolean[items.length];


        for (int i = 0; i < items.length; i++) {
            checkedItems[i] = currentSelectedStyles.contains(items[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_style_title)
                .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton(R.string.done, (dialog, id) -> {
                    currentSelectedStyles.clear();
                    for (int i = 0; i < items.length; i++) {
                        if (checkedItems[i]) {
                            currentSelectedStyles.add(items[i]);
                        }
                    }
                    updateStyleButtonText();
                    Toast.makeText(this, getString(R.string.selected) + currentSelectedStyles.toString(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());

        builder.create().show();
    }



    private String formatBirthDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return getString(R.string.not_specified);
        }
        return rawDate;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirmation_title)
                .setMessage(R.string.delete_confirmation_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteUserProfile())
                .setNegativeButton(R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteUserProfile() {
        boolean isDeleted = dbHelper.deleteUser(userId);

        if (isDeleted) {
            SharedPreferences prefs = getSharedPreferences(DatabaseHelper.SHARED_PREFS_NAME, MODE_PRIVATE);
            prefs.edit().clear().apply();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            Toast.makeText(this, R.string.profile_deleted_successfully, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.error_deleting_profile, Toast.LENGTH_SHORT).show();
        }
    }
}