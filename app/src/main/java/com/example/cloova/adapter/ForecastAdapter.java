package com.example.cloova.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
// import com.bumptech.glide.Glide; // Импортируем Glide, если используем
import com.example.cloova.R;
import com.example.cloova.model.ForecastDay; // Используем модель WeatherAPI
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private List<ForecastDay> forecastDays; // Список объектов ForecastDay
    private Context context;

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
            if (forecast.day != null && forecast.day.condition != null) {
                // Вариант 1: Использовать библиотеку (Glide/Picasso) для загрузки URL
                String iconUrl = forecast.day.condition.iconUrlPath;
                if (iconUrl != null && !iconUrl.isEmpty()) {
                    // Glide.with(itemView.getContext())
                    //     .load("https:" + iconUrl) // Добавляем https:
                    //     .placeholder(R.drawable.ic_cloud_24) // Заглушка
                    //     .error(R.drawable.ic_cloud_24) // Ошибка
                    //     .into(ivWeatherIcon);
                    // Пока закомментировано, т.к. не добавили Glide
                    ivWeatherIcon.setImageResource(R.drawable.cloud); // Используем заглушку
                } else {
                    ivWeatherIcon.setImageResource(R.drawable.cloud); // Иконка по умолчанию
                }

                // Вариант 2: Маппинг по коду (если предпочитаете локальные иконки)
                // ivWeatherIcon.setImageResource(getWeatherIconResourceByCode(forecast.day.condition.code));
            } else {
                ivWeatherIcon.setImageResource(R.drawable.cloud); // Иконка по умолчанию
            }


            // TODO: Добавить обработчик для btnOutfit
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

        // --- Маппинг кода погоды на ресурс (если используете локальные иконки) ---
        // TODO: Заполнить этот метод кодами из документации WeatherAPI.com
        private int getWeatherIconResourceByCode(int code) {
            // Смотреть коды здесь: https://www.weatherapi.com/docs/weather_conditions.json
            switch (code) {
                case 1000: return R.drawable.wb_sunny; // Sunny / Clear
                // ... Добавить остальные коды ...
                default: return R.drawable.cloud; // Иконка по умолчанию
            }
        }
    }
}