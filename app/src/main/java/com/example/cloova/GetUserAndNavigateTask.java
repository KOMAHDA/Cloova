package com.example.cloova;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.cloova.DatabaseHelper;
import com.example.cloova.User;
import com.example.cloova.WeatherForecastActivity;

import java.util.ArrayList;
import java.util.List;

public class GetUserAndNavigateTask extends AsyncTask<Long, Void, GetUserAndNavigateTask.UserDataForNavigation> {

    private static final String TAG = "GetUserAndNavigateTask";
    private Context context;

    public GetUserAndNavigateTask(Context context) {
        this.context = context;
    }

    @Override
    protected UserDataForNavigation doInBackground(Long... userIds) {
        long userId = userIds[0];
        DatabaseHelper dbHelper = new DatabaseHelper(context);
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
            Intent intent = new Intent(context, WeatherForecastActivity.class);
            intent.putExtra(WeatherForecastActivity.EXTRA_CITY_NAME, result.city);
            intent.putExtra(WeatherForecastActivity.EXTRA_USER_STYLE, result.style != null ? result.style : "Повседневный");
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, R.string.error_city_not_found, Toast.LENGTH_SHORT).show();
            Log.w(TAG, "GetUserAndNavigateTask: City or user data not found for navigation.");
        }
    }

    public static class UserDataForNavigation {
        String city;
        String style;

        UserDataForNavigation(String city, String style) {
            this.city = city;
            this.style = style;
        }
    }
}