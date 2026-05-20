package com.example.traveling.share;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.traveling.R;
import com.example.traveling.databinding.ActivityUserProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private ActivityUserProfileBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirestoreRepository repository = new FirestoreRepository();
    private String targetUserId;
    private List<TravelPhoto> userTrips = new ArrayList<>();
    private FeedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        targetUserId = getIntent().getStringExtra("userId");
        if (targetUserId == null) {
            finish();
            return;
        }

        setupRecyclerView();
        loadUserInfo();
        loadUserTrips();

        binding.btnBackProfile.setOnClickListener(v -> finish());
        
        setupFollowButton();
    }

    private void setupRecyclerView() {
        adapter = new FeedAdapter(userTrips); // On réutilise FeedAdapter pour afficher les voyages
        binding.rvUserTrips.setLayoutManager(new GridLayoutManager(this, 1)); // Liste simple pour commencer
        binding.rvUserTrips.setAdapter(adapter);
    }

    private void loadUserInfo() {
        db.collection("users").document(targetUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                binding.tvUserNameLarge.setText(doc.getString("name"));
                String imageUrl = doc.getString("profileImageUrl");
                if (imageUrl != null) {
                    Glide.with(this).load(imageUrl).into(binding.ivProfileLarge);
                }
            }
        });
    }

    private void loadUserTrips() {
        // Simplification pour éviter l'erreur FAILED_PRECONDITION (index manquant)
        db.collection("posts")
                .whereEqualTo("userId", targetUserId)
                .whereEqualTo("visibility", "public")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userTrips.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        userTrips.add(new TravelPhoto(
                                doc.getId(),
                                doc.getString("imageUrl"),
                                doc.getString("location"),
                                doc.getString("likes"),
                                doc.getString("title"),
                                doc.getString("description"),
                                doc.getString("userId"),
                                doc.getString("category"),
                                doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0,
                                doc.getDouble("latitude") != null ? doc.getDouble("latitude") : 0.0,
                                doc.getDouble("longitude") != null ? doc.getDouble("longitude") : 0.0
                        ));
                    }
                    
                    // Tri manuel par date décroissante
                    java.util.Collections.sort(userTrips, (p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
                    
                    adapter.notifyDataSetChanged();
                    binding.tvTripCount.setText(userTrips.size() + " VOYAGES PUBLICS");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors du chargement des voyages", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupFollowButton() {
        com.google.firebase.auth.FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.isAnonymous() || currentUser.getUid().equals(targetUserId)) {
            binding.btnFollowProfile.setVisibility(View.GONE);
            return;
        }

        repository.isFollowing(targetUserId, isFollowing -> {
            updateFollowUI(isFollowing);
            binding.btnFollowProfile.setOnClickListener(v -> {
                if (isFollowing) {
                    repository.unfollowUser(targetUserId, aVoid -> updateFollowUI(false));
                } else {
                    repository.followUser(targetUserId, aVoid -> updateFollowUI(true));
                }
                setupFollowButton(); // Rafraîchir l'état
            });
        });
    }

    private void updateFollowUI(boolean isFollowing) {
        binding.btnFollowProfile.setText(isFollowing ? "SUIVI" : "SUIVRE");
        binding.btnFollowProfile.setAlpha(isFollowing ? 0.6f : 1.0f);
    }
}
