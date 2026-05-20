package com.example.traveling.share;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import com.example.traveling.R;
import com.example.traveling.databinding.ActivityMapBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private ActivityMapBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configuration indispensable pour OSM (User Agent)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configuration de la carte
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getController().setZoom(5.0);
        
        // Point central par défaut (Europe/Monde)
        GeoPoint startPoint = new GeoPoint(46.0, 2.0);
        binding.mapView.getController().setCenter(startPoint);

        binding.btnBack.setOnClickListener(v -> finish());
        
        loadPins();
    }

    private void loadPins() {
        FirebaseFirestore.getInstance().collection("posts")
                .whereEqualTo("visibility", "public")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        
                        if (lat != null && lng != null && lat != 0.0) {
                            GeoPoint point = new GeoPoint(lat, lng);
                            Marker marker = new Marker(binding.mapView);
                            marker.setPosition(point);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            marker.setTitle(doc.getString("title"));
                            marker.setSnippet(doc.getString("location"));

                            // Créer l'objet photo pour le clic
                            TravelPhoto photo = new TravelPhoto(
                                    doc.getId(),
                                    doc.getString("imageUrl"),
                                    doc.getString("location"),
                                    doc.getString("likes") != null ? doc.getString("likes") : "0",
                                    doc.getString("title"),
                                    doc.getString("description"),
                                    doc.getString("userId"),
                                    doc.getString("category"),
                                    doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0,
                                    lat,
                                    lng
                            );

                            marker.setOnMarkerClickListener((m, mapView) -> {
                                Intent intent = new Intent(this, PhotoDetailsActivity.class);
                                intent.putExtra("image_url", photo.getImageUrl());
                                intent.putExtra("location", photo.getLocation());
                                intent.putExtra("title", photo.getTitle());
                                intent.putExtra("description", photo.getDescription());
                                intent.putExtra("timestamp", photo.getTimestamp());
                                intent.putExtra("likes", photo.getLikes());
                                intent.putExtra("id", photo.getId());
                                intent.putExtra("latitude", photo.getLatitude());
                                intent.putExtra("longitude", photo.getLongitude());
                                startActivity(intent);
                                return true;
                            });

                            binding.mapView.getOverlays().add(marker);
                        }
                    }
                    binding.mapView.invalidate(); // Rafraîchir la carte
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }
}
