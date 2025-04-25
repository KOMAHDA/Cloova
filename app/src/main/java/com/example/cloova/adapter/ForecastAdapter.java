package com.example.cloova.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cloova.R;
import com.example.cloova.model.ForecastDay;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private List<ForecastDay> forecastDays; // Список объектов ForecastDay
    private Context context;
    private static final String TAG = "ForecastAdapter";

    // Конструктор
    public ForecastAdapter(Context context, List<ForecastDay> forecastDays) {
        this.context = context;
        this.forecastDays = forecastDays;
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
        holder.bind(forecastDay);
    }

    @Override
    public int getItemCount() {
        return forecastDays == null ? 0 : forecastDays.size();
    }

    // Метод для обновления данных
    public void updateData(List<ForecastDay> newForecasts) {
        this.forecastDays.clear();
        if (newForecasts != null) {
            this.forecastDays.addAll(newForecasts);
        }
        notifyDataSetChanged();
    }

    // --- ViewHolder ---
    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDayOfWeek;
        ImageView ivWeatherIcon;
        TextView tvTempDay, tvTempNight; // Используем TextView
        ImageButton btnOutfit;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDayOfWeek = itemView.findViewById(R.id.tv_day_of_week);
            ivWeatherIcon = itemView.findViewById(R.id.iv_weather_icon);
            tvTempDay = itemView.findViewById(R.id.tv_temp_day); // Находим TextView
            tvTempNight = itemView.findViewById(R.id.tv_temp_night); // Находим TextView
            btnOutfit = itemView.findViewById(R.id.btn_outfit);
        }

        void bind(ForecastDay forecast) {
            // Форматируем и устанавливаем дату
            tvDate.setText(formatDisplayDate(forecast.date));
            tvDayOfWeek.setText(formatDisplayDayOfWeek(forecast.date));

            // Устанавливаем температуры (из объекта day)
            if (forecast.day != null) {
                tvTempDay.setText(String.format(Locale.getDefault(), "%.0f°", forecast.day.maxTempC));
                tvTempNight.setText(String.format(Locale.getDefault(), "%.0f°", forecast.day.minTempC));
            } else {
                tvTempDay.setText("N/A");
                tvTempNight.setText("N/A");
            }

            // Установка иконки погоды (из объекта condition внутри day)
            if (forecast.day.condition != null) {
                int weatherCode = forecast.day.condition.code;
                int iconResId = getWeatherIconResourceByCode(weatherCode);
                ivWeatherIcon.setImageResource(iconResId);
                Log.d(TAG, "Binding item for date " + forecast.date + ", Weather code: " + weatherCode + ", Icon Res ID: " + iconResId);
            } else {
                tvTempDay.setText("N/A");
                tvTempNight.setText("N/A");
                ivWeatherIcon.setImageResource(R.drawable.cloud); // Иконка по умолчанию
                Log.w(TAG, "Binding item for date " + forecast.date + ", Day object is null. Setting default icon.");
            }

            btnOutfit.setOnClickListener(v -> { /* Действие */ });
        }

        // --- Вспомогательные методы форматирования даты ---
        private String formatDisplayDate(String inputDate) { // inputDate в формате "YYYY-MM-DD"
            if (inputDate == null) return "N/A";
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM", new Locale("ru")); // "28 марта"
            try {
                Date date = inputFormat.parse(inputDate);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return inputDate; // Возвращаем как есть при ошибке
            }
        }

        private String formatDisplayDayOfWeek(String inputDate) {
            if (inputDate == null) return "N/A";
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE", new Locale("ru")); // "вторник"
            try {
                Date date = inputFormat.parse(inputDate);
                // TODO: Добавить логику "Сегодня", "Завтра"
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
            // Коды взяты из https://www.weatherapi.com/docs/weather_conditions.json
            // Используйте ваши имена файлов иконок!
            switch (code) {
                case 1000: // Sunny / Clear (ночь обрабатывается URL в API, здесь один код)
                    return R.drawable.wb_sunny; // Используйте вашу иконку для Ясно
                case 1003: // Partly cloudy
                    return R.drawable.partly_cloudy; // Ваша иконка переменной облачности
                case 1006: // Cloudy
                case 1009: // Overcast
                    return R.drawable.cloud; // Ваша иконка Облачно/Пасмурно
                case 1030: // Mist
                case 1135: // Fog
                case 1147: // Freezing fog
                    return R.drawable.foggy; // Ваша иконка тумана
                case 1063: // Patchy light rain
                case 1150: // Patchy light drizzle
                case 1180: // Patchy light rain
                case 1183: // Light rain
                case 1240: // Light rain shower
                    return R.drawable.rainy; // Ваша иконка легкого дождя
                case 1066: // Patchy snow possible
                case 1210: // Patchy light snow
                case 1255: // Light snow showers
                    return R.drawable.weather_snowy_24dp_e3e3e3_fill0_wght400_grad0_opsz24; // Ваша иконка легкого снега
                case 1069: // Patchy sleet possible
                case 1249: // Sleet showers
                case 1204: // Light sleet
                case 1207: // Moderate or heavy sleet
                    return R.drawable.weather_mix; // Нужна иконка дождя со снегом
                case 1072: // Patchy freezing drizzle possible
                case 1168: // Freezing drizzle
                case 1171: // Heavy freezing drizzle
                    return R.drawable.weather_snowy_24dp_e3e3e3_fill0_wght400_grad0_opsz24; // Или иконка ледяного дождя
                case 1087: // Thundery outbreaks possible
                case 1273: // Patchy light rain with thunder
                case 1276: // Moderate or heavy rain with thunder
                    return R.drawable.thunderstorm; // Ваша иконка грозы
                case 1114: // Blowing snow
                case 1213: // Light snow
                case 1216: // Patchy moderate snow
                case 1219: // Moderate snow
                case 1222: // Patchy heavy snow
                case 1225: // Heavy snow
                case 1258: // Moderate or heavy snow showers
                    return R.drawable.weather_snowy_24dp_e3e3e3_fill0_wght400_grad0_opsz24; // Ваша иконка снега
                case 1117: // Blizzard
                    return R.drawable.weather_snowy_24dp_e3e3e3_fill0_wght400_grad0_opsz24; // Или специальная иконка метели
                case 1153: // Light drizzle
                case 1186: // Moderate rain at times
                case 1189: // Moderate rain
                case 1192: // Heavy rain at times
                case 1195: // Heavy rain
                case 1243: // Moderate or heavy rain shower
                case 1246: // Torrential rain shower
                    return R.drawable.rainy; // Ваша иконка дождя
                case 1198: // Light freezing rain
                case 1201: // Moderate or heavy freezing rain
                    return R.drawable.weather_mix; // Или иконка ледяного дождя
                case 1237: // Ice pellets
                case 1261: // Light showers of ice pellets
                case 1264: // Moderate or heavy showers of ice pellets
                    return R.drawable.weather_mix; // Или иконка града/ледяной крупы
                case 1279: // Patchy light snow with thunder
                case 1282: // Moderate or heavy snow with thunder
                    return R.drawable.weather_snowy_24dp_e3e3e3_fill0_wght400_grad0_opsz24; // Или иконка грозы со снегом

                default:
                    Log.w(TAG, "getWeatherIconResourceByCode: Unknown weather code: " + code);
                    return R.drawable.cloud; // Ваша иконка по умолчанию
            }
        }
    }
}