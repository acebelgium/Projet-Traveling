package com.example.travelling;

public class TravelPhoto {
    private String id;
    private String imageUrl;
    private String location;
    private String likes;
    private String title;
    private String description;

    public TravelPhoto(String id, String imageUrl, String location, String likes, String title, String description) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.location = location;
        this.likes = likes;
        this.title = title;
        this.description = description;
    }

    public String getImageUrl() { return imageUrl; }
    public String getLocation() { return location; }
    public String getLikes() { return likes; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
