package com.example.traveling.path;

import android.util.Log;
import com.example.traveling.share.MockRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class LieuRepository {
    private static final String TAG = "LieuRepository";
    private static WikipediaService wikiService;

    private static WikipediaService getWikiService() {
        if (wikiService == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .header("User-Agent", "TravellingApp/1.0 (contact@travelling.app)")
                                .header("Accept", "application/json; charset=utf-8")
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://fr.wikipedia.org/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            wikiService = retrofit.create(WikipediaService.class);
        }
        return wikiService;
    }

    public interface Callback {
        void onResult(List<Lieu> lieux);
        void onCityNotFound();
        void onError(Throwable t);
    }

    public static void fetchLieux(String city, Callback callback) {
        final String cleanCity = city.trim();
        Log.d(TAG, "Démarrage recherche ville: " + cleanCity);

        getWikiService().getCityDetails(cleanCity).enqueue(new retrofit2.Callback<WikipediaResponse>() {
            @Override
            public void onResponse(Call<WikipediaResponse> call, Response<WikipediaResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().query != null && response.body().query.pages != null) {
                    
                    WikipediaResponse.Page page = response.body().query.pages.values().iterator().next();
                    
                    if (page.coordinates != null && !page.coordinates.isEmpty()) {
                        double lat = page.coordinates.get(0).lat;
                        double lon = page.coordinates.get(0).lon;
                        lancerRechercheHybride(lat, lon, page.title, callback);
                    } else {
                        callback.onCityNotFound();
                    }
                } else {
                    callback.onCityNotFound();
                }
            }
            @Override public void onFailure(Call<WikipediaResponse> call, Throwable t) { callback.onError(t); }
        });
    }

    private static void lancerRechercheHybride(double cityLat, double cityLon, String cityName, Callback callback) {
        final List<Lieu> resultatsFinaux = new ArrayList<>();
        final Set<String> titresVus = new HashSet<>();
        final int[] requetesTerminees = {0};

        // Recherche A : Par proximité (Géo-search)
        String coords = cityLat + "|" + cityLon;
        getWikiService().searchNearby(coords, 20000).enqueue(new retrofit2.Callback<WikipediaResponse>() {
            @Override
            public void onResponse(Call<WikipediaResponse> call, Response<WikipediaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fusionnerResultats(response.body(), resultatsFinaux, titresVus, cityName, cityLat, cityLon);
                }
                verifierFin();
            }
            @Override public void onFailure(Call<WikipediaResponse> call, Throwable t) { verifierFin(); }

            private void verifierFin() {
                requetesTerminees[0]++;
                if (requetesTerminees[0] == 2) callback.onResult(resultatsFinaux);
            }
        });

        // Recherche B : Par mots-clés
        String queryText = "monument historique " + cityName;
        getWikiService().searchMonuments(queryText).enqueue(new retrofit2.Callback<WikipediaResponse>() {
            @Override
            public void onResponse(Call<WikipediaResponse> call, Response<WikipediaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fusionnerResultats(response.body(), resultatsFinaux, titresVus, cityName, cityLat, cityLon);
                }
                verifierFin();
            }
            @Override public void onFailure(Call<WikipediaResponse> call, Throwable t) { verifierFin(); }

            private void verifierFin() {
                requetesTerminees[0]++;
                if (requetesTerminees[0] == 2) callback.onResult(resultatsFinaux);
            }
        });
    }

    private static void fusionnerResultats(WikipediaResponse response, List<Lieu> destination, Set<String> titresVus, 
                                           String cityName, double cityLat, double cityLon) {
        if (response.query == null || response.query.pages == null) return;

        for (WikipediaResponse.Page page : response.query.pages.values()) {
            if (titresVus.contains(page.title)) continue;

            String extract = (page.extract != null) ? page.extract.toLowerCase() : "";
            String titleLower = page.title.toLowerCase();
            
            // 1. FILTRES DE TYPE (Évènements, administratifs, etc.)
            if (titleLower.contains("massacre") || titleLower.contains("attentat") || 
                titleLower.contains("manifestation") || titleLower.contains("siège de") ||
                titleLower.contains("bataille de") || titleLower.contains("festival")) continue;

            if (extract.contains("commune") || extract.contains("ville de") || 
                extract.contains("village") || extract.contains("département")) continue;

            if (page.title.equalsIgnoreCase(cityName)) continue;

            if (page.coordinates != null && !page.coordinates.isEmpty()) {
                double lat = page.coordinates.get(0).lat;
                double lon = page.coordinates.get(0).lon;

                // 2. FILTRE GÉOGRAPHIQUE CRITIQUE (Rayon de 30km max autour du centre)
                // Évite que "Paris" ramène le Taj Mahal à 6000km !
                double distance = calculerDistanceKM(cityLat, cityLon, lat, lon);
                if (distance > 30.0) {
                    Log.d(TAG, "Lieu exclu par distance: " + page.title + " (" + distance + " km)");
                    continue; 
                }
                
                String img = (page.thumbnail != null) ? page.thumbnail.source : 
                            "https://images.unsplash.com/photo-1480714378408-67cf0d13bc1b?auto=format&fit=crop&w=800";

                String cat = detecterCategorie(page.title, extract);

                destination.add(new Lieu(
                        page.title,
                        (page.extract != null && page.extract.length() > 200) ? page.extract.substring(0, 200) + "..." : page.extract,
                        img,
                        cat,
                        0.0,
                        90,
                        lat,
                        lon,
                        9,
                        18
                ));
                titresVus.add(page.title);
            }
        }
    }

    private static String detecterCategorie(String titre, String extract) {
        String full = (titre + " " + extract).toLowerCase();
        if (full.contains("musée")) return "Musées";
        if (full.contains("parc") || full.contains("jardin") || full.contains("square")) return "Parcs";
        if (full.contains("église") || full.contains("cathédrale") || full.contains("basilique") || full.contains("mosquée") || full.contains("synagogue")) return "Religieux";
        if (full.contains("château") || full.contains("palais") || full.contains("tour")) return "Monuments";
        return "Culture";
    }

    private static double calculerDistanceKM(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) 
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        return dist * 60 * 1.853159616; // Conversion en kilomètres
    }
}
