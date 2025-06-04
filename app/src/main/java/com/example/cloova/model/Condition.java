package com.example.cloova.model;
import com.google.gson.annotations.SerializedName;
public class Condition {
    @SerializedName("text")
    public String text;
    @SerializedName("icon")
    public String iconUrlPath;
    @SerializedName("code")
    public int code;
}