package com.example.traveling.share;
import com.example.traveling.R;
import com.example.traveling.path.Lieu;

import java.util.ArrayList;
import java.util.List;

public class MockRepository {

    public static List<Lieu> getLieuxByCity(String city) {
        List<Lieu> filtered = new ArrayList<>();
        String cityName = city.toLowerCase().trim();

        if (cityName.contains("paris")) {
            filtered.add(new Lieu("Tour Eiffel", "L'icône mondiale de la France.", getDynamicImageUrl("eiffel tower"), "Culture", 25.0, 120, 48.8584, 2.2945, 9, 23));
            filtered.add(new Lieu("Musée du Louvre", "Le plus grand musée d'art du monde.", getDynamicImageUrl("louvre"), "Culture", 17.0, 180, 48.8606, 2.3376, 9, 18));
            filtered.add(new Lieu("Arc de Triomphe", "Monument historique aux Champs-Élysées.", getDynamicImageUrl("arc de triomphe"), "Culture", 13.0, 45, 48.8738, 2.2950, 10, 22));
        } else {
            filtered.add(new Lieu("Vieille Ville de " + city, "Le cœur historique.", getDynamicImageUrl(city + " old town"), "Culture", 0.0, 120, 0, 0, 9, 19));
            filtered.add(new Lieu("Grand Parc de " + city, "Espace de verdure.", getDynamicImageUrl(city + " park"), "Nature", 0.0, 90, 0, 0, 7, 21));
        }
        return filtered;
    }

    private static String getDynamicImageUrl(String query) {
        return "https://loremflickr.com/800/600/" + query.toLowerCase().replaceAll(" ", "") + ",landmark,travel/all";
    }

    // Données pour le flux social (si nécessaire)
    public static List<TravelPhoto> getFeedPhotos() {
        return new ArrayList<>(); // Sera remplacé par Firestore dans cette version
    }
}
