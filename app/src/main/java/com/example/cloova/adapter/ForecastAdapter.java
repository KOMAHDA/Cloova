package com.example.cloova.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cloova.DayDetailActivity;
import com.example.cloova.R;
import com.example.cloova.WeatherForecastActivity;
import com.example.cloova.model.ForecastDay;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private List<ForecastDay> forecastDays;
    private Context context;
    private String currentCityName;
    private String userPreferredStyle;
    private static final String TAG = "ForecastAdapter";

    public ForecastAdapter(Context context, List<ForecastDay> forecastDays, String cityName, String userStyle) {
        this.context = context;
        this.forecastDays = forecastDays;
        this.currentCityName = cityName;
        this.userPreferredStyle = userStyle;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastDay forecastDay = forecastDays.get(position);
        holder.bind(forecastDay, context, currentCityName, this.userPreferredStyle);
    }

    @Override
    public int getItemCount() {
        return forecastDays == null ? 0 : forecastDays.size();
    }

    public void updateData(List<ForecastDay> newForecasts, String cityName, String userStyle) {
        this.currentCityName = cityName;
        this.userPreferredStyle = userStyle;
        this.forecastDays.clear();
        if (newForecasts != null) {
            this.forecastDays.addAll(newForecasts);
        }
        notifyDataSetChanged();
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDayOfWeek;
        ImageView ivWeatherIcon;
        TextView tvTempDay, tvTempNight;
        ImageButton btnOutfit;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDayOfWeek = itemView.findViewById(R.id.tv_day_of_week);
            ivWeatherIcon = itemView.findViewById(R.id.iv_weather_icon);
            tvTempDay = itemView.findViewById(R.id.tv_temp_day);
            tvTempNight = itemView.findViewById(R.id.tv_temp_night);
            btnOutfit = itemView.findViewById(R.id.btn_outfit);
        }

        void bind(final ForecastDay forecast, final Context context, final String cityName, final String preferredStyleForOutfit) {
            tvDate.setText(formatDisplayDate(forecast.date));
            tvDayOfWeek.setText(formatDisplayDayOfWeek(forecast.date));

            if (forecast.day != null) {
                tvTempDay.setText(String.format(Locale.getDefault(), "%.0f°", forecast.day.maxTempC));
                tvTempNight.setText(String.format(Locale.getDefault(), "%.0f°", forecast.day.minTempC));
            } else {
                tvTempDay.setText("N/A");
                tvTempNight.setText("N/A");
            }

            if (forecast.day != null && forecast.day.condition != null) {
                ivWeatherIcon.setImageResource(getWeatherIconResourceByCode(forecast.day.condition.code));
                Log.d(TAG, "Binding weather icon for code: " + forecast.day.condition.code + ", resource: " + context.getResources().getResourceEntryName(getWeatherIconResourceByCode(forecast.day.condition.code)));
            } else {
                ivWeatherIcon.setImageResource(R.drawable.cloud);
                Log.w(TAG, "No weather condition data for forecast day. Using default cloud icon.");
            }

            btnOutfit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ForecastAdapter", "Outfit button clicked for date: " + forecast.date);
                    Intent intent = new Intent(context, DayDetailActivity.class);

                    intent.putExtra(DayDetailActivity.EXTRA_DATE_STR, forecast.date);
                    if (forecast.day != null) {
                        intent.putExtra(DayDetailActivity.EXTRA_MAX_TEMP, forecast.day.maxTempC);
                        intent.putExtra(DayDetailActivity.EXTRA_MIN_TEMP, forecast.day.minTempC);
                        intent.putExtra(DayDetailActivity.EXTRA_WIND_KPH, forecast.day.maxwind_kph);
                        intent.putExtra(DayDetailActivity.EXTRA_HUMIDITY, forecast.day.avghumidity);
                        if (forecast.day.condition != null) {
                            intent.putExtra(DayDetailActivity.EXTRA_WEATHER_ICON_URL, forecast.day.condition.iconUrlPath);
                            intent.putExtra(DayDetailActivity.EXTRA_WEATHER_DESCRIPTION, forecast.day.condition.text);
                            intent.putExtra(DayDetailActivity.EXTRA_WEATHER_CODE, forecast.day.condition.code);
                        }
                    }
                    intent.putExtra(DayDetailActivity.EXTRA_CITY_NAME, cityName);
                    intent.putExtra(DayDetailActivity.EXTRA_USER_STYLE_FOR_OUTFIT, preferredStyleForOutfit);
                    context.startActivity(intent);
                }
            });
        }

        private String formatDisplayDate(String inputDate) {
            if (inputDate == null) return "N/A";
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM", new Locale("ru"));
            try {
                Date date = inputFormat.parse(inputDate);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return inputDate;
            }
        }

        private String formatDisplayDayOfWeek(String inputDate) {
            if (inputDate == null) return "N/A";
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE", new Locale("ru"));
            try {
                Date date = inputFormat.parse(inputDate);

                String dayName = outputFormat.format(date);
                return capitalize(dayName);
            } catch (ParseException e) {
                return "";
            }
        }

        private String capitalize(String str) {
            if (str == null || str.isEmpty()) return str;
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }


        private int getWeatherIconResourceByCode(int code) {
            Log.d(TAG, "getWeatherIconResourceByCode called with code: " + code);
            switch (code) {
                case 1000:
                    return R.drawable.wb_sunny;
                case 1003:
                    return R.drawable.partly_cloudy;
                case 1006:
                case 1009:
                    return R.drawable.cloud;
                case 1030:
                case 1135:
                case 1147:
                    return R.drawable.foggy;
                case 1063:
                case 1150:
                case 1180:
                case 1183:
                case 1240:
                    return R.drawable.rainy;
                case 1066:
                case 1210:
                case 1255:
                    return R.drawable.weather_snowy_24dp_e3e3e3_fill0_wght400_grad0_opsz24;
                case 1069:
                case 1249:
                case 1204:
                case 1207:
                    return R.drawable.weather_mix;
                case 1072:
                case 1168:
                case 1171:
                    return R.drawable.weather_snowy_24dp_e3e3e3_fill0_wght400_grad0_opsz24;
                case 1087:
                case 1273:
                case 1276:
                    return R.drawable.thunderstorm;
                case 1114:
                case 1213:
                case 1216:
                case 1219:
                case 1222:
                case 1225:
                case 1258:
                    return R.drawable.weather_snowy_24dp_e3e3e3_fill0_wght400_grad0_opsz24;
                case 1117:
                    return R.drawable.weather_snowy_24dp_e3e3e3_fill0_wght400_grad0_opsz24;
                case 1153:
                case 1186:
                case 1189:
                case 1192:
                case 1195:
                case 1243:
                case 1246:
                    return R.drawable.rainy;
                case 1198:
                case 1201:
                    return R.drawable.weather_mix;
                case 1237:
                case 1261:
                case 1264:
                    return R.drawable.weather_mix;
                case 1279:
                case 1282:
                    return R.drawable.weather_snowy_24dp_e3e3e3_fill0_wght400_grad0_opsz24;

                default:
                    Log.w(TAG, "getWeatherIconResourceByCode: Unknown weather code: " + code);
                    return R.drawable.cloud;
            }
        }
    }
}