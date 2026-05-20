package com.example.traveling.share;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TravelPhoto {
    private String id;
    private String imageUrl;
    private String location;
    private String likes;
    private String title;
    private String description;
    private String userId;
    private String category;
    private long timestamp;
    private double latitude;
    private double longitude;

    public TravelPhoto(String id, String imageUrl, String location, String likes, String title, String description, String userId, String category, long timestamp) {
        this(id, imageUrl, location, likes, title, description, userId, category, timestamp, 0.0, 0.0);
    }

    public TravelPhoto(String id, String imageUrl, String location, String likes, String title, String description, String userId, String category, long timestamp, double latitude, double longitude) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.location = location;
        this.likes = likes;
        this.title = title;
        this.description = description;
        this.userId = userId;
        this.category = category;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public String getLocation() { return location; }
    public String getLikes() { return likes; }
    public void setLikes(String likes) { this.likes = likes; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getUserId() { return userId; }
    public String getCategory() { return category; }
    public long getTimestamp() { return timestamp; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    /**
     * Retourne la date formatée (ex: "17 Mai 2026")
     */
    public String getFormattedDate() {
        if (timestamp == 0) return "Date inconnue";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE);
        return sdf.format(new Date(timestamp));
    }
}
