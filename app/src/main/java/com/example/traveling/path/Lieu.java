package com.example.traveling.path;
import com.example.traveling.R;

import java.io.Serializable;

public class Lieu implements Serializable {
    private String nom;
    private String description;
    private String imageUrl;
    private String categorie;
    private double prix;
    private int tempsVisiteMinutes;
    private double latitude;
    private double longitude;
    private int heureOuverture;
    private int heureFermeture;

    public Lieu(String nom, String description, String imageUrl, String categorie,
                double prix, int tempsVisiteMinutes, double lat, double lng) {
        this(nom, description, imageUrl, categorie, prix, tempsVisiteMinutes, lat, lng, 9, 18);
    }

    public Lieu(String nom, String description, String imageUrl, String categorie,
                double prix, int tempsVisiteMinutes, double lat, double lng,
                int hOuv, int hFerm) {
        this.nom = nom;
        this.description = description;
        this.imageUrl = imageUrl;
        this.categorie = categorie;
        this.prix = prix;
        this.tempsVisiteMinutes = tempsVisiteMinutes;
        this.latitude = lat;
        this.longitude = lng;
        this.heureOuverture = hOuv;
        this.heureFermeture = hFerm;
    }

    // Getters
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public String getCategorie() { return categorie; }
    public double getPrix() { return prix; }
    public int getTempsVisiteMinutes() { return tempsVisiteMinutes; }
    public String getImageUrl() { return imageUrl; }
    public int getHeureOuverture() { return heureOuverture; }
    public int getHeureFermeture() { return heureFermeture; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
