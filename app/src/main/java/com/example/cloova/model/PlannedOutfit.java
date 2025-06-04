package com.example.cloova.model;

import java.util.Map;

public class PlannedOutfit {
    private long id;
    private long userId;
    private String planDate;
    private String dateCreated;
    private String weatherDescription;
    private double temperature;
    private String styleName;
    private Map<String, ClothingItem> outfitItems;

    public PlannedOutfit(long id, long userId, String planDate, String dateCreated, String weatherDescription, double temperature, String styleName, Map<String, ClothingItem> outfitItems) {
        this.id = id;
        this.userId = userId;
        this.planDate = planDate;
        this.dateCreated = dateCreated;
        this.weatherDescription = weatherDescription;
        this.temperature = temperature;
        this.styleName = styleName;
        this.outfitItems = outfitItems;
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getPlanDate() { return planDate; }
    public String getDateCreated() { return dateCreated; }
    public String getWeatherDescription() { return weatherDescription; }
    public double getTemperature() { return temperature; }
    public String getStyleName() { return styleName; }
    public Map<String, ClothingItem> getOutfitItems() { return outfitItems; }
}