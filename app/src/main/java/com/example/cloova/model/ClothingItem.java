package com.example.cloova.model; // Важно!

public class ClothingItem {
    private long clothingId;
    private String name;
    private String category;
    private String imageResourceName;
    private String genderTarget;
    private int minTemp;
    private int maxTemp;
    private boolean isWaterproof;
    private boolean isWindproof;

    public ClothingItem() {}

    // Геттеры и сеттеры
    public long getClothingId() { return clothingId; }
    public void setClothingId(long clothingId) { this.clothingId = clothingId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImageResourceName() { return imageResourceName; }
    public void setImageResourceName(String imageResourceName) { this.imageResourceName = imageResourceName; }
    public String getGenderTarget() { return genderTarget; }
    public void setGenderTarget(String genderTarget) { this.genderTarget = genderTarget; }
    public int getMinTemp() { return minTemp; }
    public void setMinTemp(int minTemp) { this.minTemp = minTemp; }
    public int getMaxTemp() { return maxTemp; }
    public void setMaxTemp(int maxTemp) { this.maxTemp = maxTemp; }
    public boolean isWaterproof() { return isWaterproof; }
    public void setWaterproof(boolean waterproof) { isWaterproof = waterproof; }
    public boolean isWindproof() { return isWindproof; }
    public void setWindproof(boolean windproof) { isWindproof = windproof; }
}