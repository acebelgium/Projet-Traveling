package com.example.traveling.path;

import java.util.List;
import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RoutingService {
    // Profil walking pour un chemin piéton réel
    @GET("route/v1/walking/{coords}")
    Call<OsrmResponse> getRoute(
        @Path("coords") String coordinates,
        @Query("overview") String overview,
        @Query("geometries") String geometries
    );
}

class OsrmResponse {
    @SerializedName("routes")
    public List<Route> routes;

    public static class Route {
        @SerializedName("geometry")
        public Geometry geometry;
    }

    public static class Geometry {
        @SerializedName("coordinates")
        public List<List<Double>> coordinates; // [ [lon, lat], ... ]
    }
}
