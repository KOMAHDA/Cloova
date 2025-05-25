package com.example.cloova;

import android.app.Application; // Важно: импортировать android.app.Application
import com.yandex.mapkit.MapKitFactory; // Импортировать MapKitFactory

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MapKitFactory.setApiKey("ed7e3518-2afb-4989-a0b1-0cbf9283558b");
        MapKitFactory.initialize(this);
    }
}