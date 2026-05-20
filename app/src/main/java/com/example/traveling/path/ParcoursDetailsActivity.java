package com.example.traveling.path;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.traveling.databinding.ActivityParcoursDetailsBinding;
import java.util.Collections;

import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ParcoursDetailsActivity extends AppCompatActivity {
    private ActivityParcoursDetailsBinding binding;
    private Parcours parcours;
    private EtapeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityParcoursDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parcours = (Parcours) getIntent().getSerializableExtra("PARCOURS");
        
        boolean isPmr = getIntent().getBooleanExtra("PMR", false);
        boolean isSenior = getIntent().getBooleanExtra("SENIOR", false);

        if (parcours != null) {
            binding.toolbar.setTitle(parcours.getTitre());
            binding.tvDetailsTitre.setText(parcours.getTitre());
            
            // Chargement de l'image de couverture (première étape)
            if (!parcours.getEtapes().isEmpty()) {
                Glide.with(this)
                     .load(parcours.getEtapes().get(0).getImageUrl())
                     .centerCrop()
                     .into(binding.ivHeader);
            }
            
            String stats = parcours.getNbEtapes() + " étapes • " + 
                          parcours.getDureeTotaleMinutes() / 60 + "h " + (parcours.getDureeTotaleMinutes() % 60) + "m • " +
                          parcours.getBudgetTotal() + " €";
            binding.tvDetailsStats.setText(stats);

            // Affichage Eco-Santé
            binding.tvEcoCalories.setText(parcours.getCaloriesBrulees() + " kcal");
            binding.tvEcoCo2.setText(String.format("%.1f kg", parcours.getCo2EconomiseKG()));

            binding.rvEtapes.setLayoutManager(new LinearLayoutManager(this));
            adapter = new EtapeAdapter(parcours.getEtapes(), isPmr, isSenior);
            binding.rvEtapes.setAdapter(adapter);

            setupDragAndDrop();

            binding.btnOuvrirMap.setOnClickListener(v -> {
                Intent intent = new Intent(this, CarteActivity.class);
                intent.putExtra("PARCOURS", parcours);
                startActivity(intent);
            });

            binding.btnPublierFlux.setOnClickListener(v -> publierParcours());
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void publierParcours() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> post = new HashMap<>();
        post.put("userId", userId);
        post.put("title", "J'ai créé un itinéraire : " + parcours.getTitre());
        post.put("location", parcours.getTitre()); // On utilise le titre comme lieu simplifié
        post.put("description", "Itinéraire de " + parcours.getNbEtapes() + " étapes. Distance : " + String.format("%.1f", parcours.getDistanceTotaleKM()) + "km");
        post.put("imageUrl", parcours.getEtapes().get(0).getImageUrl());
        post.put("timestamp", System.currentTimeMillis());
        post.put("visibility", "public");

        db.collection("posts").add(post)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Parcours publié avec succès !", Toast.LENGTH_SHORT).show();
                binding.btnPublierFlux.setEnabled(false);
                binding.btnPublierFlux.setText("Publié ✅");
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Erreur lors de la publication", Toast.LENGTH_SHORT).show();
            });
    }

    private void setupDragAndDrop() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                
                adapter.onItemMove(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.rvEtapes);
    }
}
