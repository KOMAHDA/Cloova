package com.example.cloova;

import android.app.Application;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.runtime.Runtime;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Уберите эту строку, так как ключ уже в манифесте
        // MapKitFactory.setApiKey("ваш_api_ключ_яндекс_карт");
        MapKitFactory.setLocale("ru_RU"); // Установите нужную локаль
    }
}
