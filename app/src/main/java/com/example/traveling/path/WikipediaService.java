package com.example.traveling.path;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WikipediaService {
    // Recherche textuelle globale - Augmentée à 50 résultats
    @GET("w/api.php?action=query&format=json&prop=coordinates|pageimages|extracts&generator=search&gsrlimit=50&exintro&explaintext&piprop=thumbnail&pithumbsize=800")
    Call<WikipediaResponse> searchMonuments(@Query("gsrsearch") String searchQuery);

    // Recherche par proximité géographique - Augmentée à 50 résultats
    @GET("w/api.php?action=query&format=json&prop=coordinates|pageimages|extracts&generator=geosearch&ggslimit=50&exintro&explaintext&piprop=thumbnail&pithumbsize=800")
    Call<WikipediaResponse> searchNearby(@Query("ggscoord") String latLon, @Query("ggsradius") int radius);

    // Pour trouver les coordonnées d'une ville par recherche floue (gsrsearch)
    @GET("w/api.php?action=query&format=json&prop=coordinates&generator=search&gsrlimit=1&gsrsearch=")
    Call<WikipediaResponse> getCityDetails(@Query("gsrsearch") String cityName);
}

class WikipediaResponse {
    public QueryData query;

    public static class QueryData {
        public Map<String, Page> pages;
    }

    public static class Page {
        public String title;
        public String extract;
        public List<Coordinate> coordinates;
        public Thumbnail thumbnail;

        public static class Coordinate {
            public double lat;
            public double lon;
        }

        public static class Thumbnail {
            public String source;
        }
    }
}
