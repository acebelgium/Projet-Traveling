package com.example.travelling;

public class Journey {
    private String id;
    private String title;
    private String location;
    private String duration;
    private String imageUrl;
    private String category; // e.g., "EUROPE", "ASIE"

    public Journey(String id, String title, String location, String duration, String imageUrl, String category) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.duration = duration;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public String getDuration() { return duration; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
}
