package com.example.traveling.path;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.traveling.databinding.ActivityCarteBinding;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CarteActivity extends AppCompatActivity {
    private ActivityCarteBinding binding;
    private Parcours parcours;
    private Polyline roadOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Configuration.getInstance().setUserAgentValue("TravellingApp/1.0");
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        
        binding = ActivityCarteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parcours = (Parcours) getIntent().getSerializableExtra("PARCOURS");

        setupMap();
        if (parcours != null) {
            displayParcours();
            fetchRealRoute();
        }

        binding.toolbarMap.setNavigationOnClickListener(v -> finish());
    }

    private void setupMap() {
        binding.map.setTileSource(TileSourceFactory.MAPNIK);
        binding.map.setMultiTouchControls(true);
        binding.map.getZoomController().setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER);
    }

    private void displayParcours() {
        List<Lieu> etapes = parcours.getEtapes();
        if (etapes == null || etapes.isEmpty()) return;

        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;

        for (int i = 0; i < etapes.size(); i++) {
            Lieu l = etapes.get(i);
            GeoPoint gp = new GeoPoint(l.getLatitude(), l.getLongitude());

            minLat = Math.min(minLat, gp.getLatitude());
            maxLat = Math.max(maxLat, gp.getLatitude());
            minLon = Math.min(minLon, gp.getLongitude());
            maxLon = Math.max(maxLon, gp.getLongitude());

            Marker marker = new Marker(binding.map);
            marker.setPosition(gp);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle((i + 1) + ". " + l.getNom());
            binding.map.getOverlays().add(marker);
        }

        if (etapes.size() > 1) {
            final BoundingBox bbox = new BoundingBox(maxLat + 0.005, maxLon + 0.005, minLat - 0.005, minLon - 0.005);
            binding.map.post(() -> binding.map.zoomToBoundingBox(bbox, true, 100));
        } else if (!etapes.isEmpty()) {
            binding.map.getController().setZoom(15.0);
            binding.map.getController().setCenter(new GeoPoint(etapes.get(0).getLatitude(), etapes.get(0).getLongitude()));
        }
    }

    private void fetchRealRoute() {
        List<Lieu> etapes = parcours.getEtapes();
        if (etapes.size() < 2) return;

        StringBuilder coordsBuilder = new StringBuilder();
        for (int i = 0; i < etapes.size(); i++) {
            Lieu l = etapes.get(i);
            // OSRM attend [longitude,latitude]
            coordsBuilder.append(l.getLongitude()).append(",").append(l.getLatitude());
            if (i < etapes.size() - 1) coordsBuilder.append(";");
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://router.project-osrm.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RoutingService service = retrofit.create(RoutingService.class);
        service.getRoute(coordsBuilder.toString(), "full", "geojson").enqueue(new Callback<OsrmResponse>() {
            @Override
            public void onResponse(Call<OsrmResponse> call, Response<OsrmResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().routes.isEmpty()) {
                    drawRealRoute(response.body().routes.get(0).geometry.coordinates);
                } else {
                    drawFallbackStraightLines();
                }
            }

            @Override
            public void onFailure(Call<OsrmResponse> call, Throwable t) {
                drawFallbackStraightLines();
            }
        });
    }

    private void drawRealRoute(List<List<Double>> coords) {
        if (roadOverlay != null) binding.map.getOverlays().remove(roadOverlay);
        
        roadOverlay = new Polyline();
        List<GeoPoint> points = new ArrayList<>();
        for (List<Double> coord : coords) {
            // OSRM renvoie [lon, lat]
            points.add(new GeoPoint(coord.get(1), coord.get(0)));
        }
        
        roadOverlay.setPoints(points);
        roadOverlay.getOutlinePaint().setColor(0xFF2196F3);
        roadOverlay.getOutlinePaint().setStrokeWidth(12.0f);
        binding.map.getOverlays().add(roadOverlay);
        binding.map.invalidate();
    }

    private void drawFallbackStraightLines() {
        // En cas d'échec de l'API routing, on trace des lignes directes pour ne pas laisser la carte vide
        Polyline fallback = new Polyline();
        List<GeoPoint> points = new ArrayList<>();
        for (Lieu l : parcours.getEtapes()) {
            points.add(new GeoPoint(l.getLatitude(), l.getLongitude()));
        }
        fallback.setPoints(points);
        fallback.getOutlinePaint().setColor(0x882196F3);
        fallback.getOutlinePaint().setStrokeWidth(8.0f);
        binding.map.getOverlays().add(fallback);
        binding.map.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.map.onPause();
    }
}
