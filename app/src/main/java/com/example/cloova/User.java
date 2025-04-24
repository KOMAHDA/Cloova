package com.example.cloova;

import java.util.List;

public class User {
    private long userId;
    private String name;
    private String gender;
    private String birthDate;
    private String city;
    private int avatarResId;
    private List<String> colors;
    private List<String> styles;
    private List<String> wardrobe;
    private List<String> accessories;

    private String language;
    private String login;

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public int getAvatarResId() { return avatarResId; }
    public void setAvatarResId(int avatarResId) { this.avatarResId = avatarResId; }

    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }

    public List<String> getStyles() { return styles; }
    public void setStyles(List<String> styles) { this.styles = styles; }

    public List<String> getWardrobe() { return wardrobe; }
    public void setWardrobe(List<String> wardrobe) { this.wardrobe = wardrobe; }

    public List<String> getAccessories() { return accessories; }
    public void setAccessories(List<String> accessories) { this.accessories = accessories; }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}