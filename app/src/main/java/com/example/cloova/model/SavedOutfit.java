package com.example.cloova.model;

import java.util.Map; // Используем Map для хранения элементов по категориям

public class SavedOutfit {
    private long id;
    private long userId;
    private String dateSaved; // Формат "YYYY-MM-DD HH:MM:SS"
    private String weatherDescription;
    private double temperature;
    private String styleName;
    private Map<String, ClothingItem> outfitItems; // Категория -> ClothingItem

    public SavedOutfit(long id, long userId, String dateSaved, String weatherDescription, double temperature, String styleName, Map<String, ClothingItem> outfitItems) {
        this.id = id;
        this.userId = userId;
        this.dateSaved = dateSaved;
        this.weatherDescription = weatherDescription;
        this.temperature = temperature;
        this.styleName = styleName;
        this.outfitItems = outfitItems;
    }

    // Геттеры
    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getDateSaved() { return dateSaved; }
    public String getWeatherDescription() { return weatherDescription; }
    public double getTemperature() { return temperature; }
    public String getStyleName() { return styleName; }
    public Map<String, ClothingItem> getOutfitItems() { return outfitItems; }
}