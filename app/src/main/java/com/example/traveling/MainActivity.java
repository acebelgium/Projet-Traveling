package com.example.traveling;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.traveling.databinding.ActivityMainBinding;
import com.example.traveling.path.TravelPathFragment;
import com.example.traveling.ExploreFragment;
import com.example.traveling.share.FeedFragment;
import com.example.traveling.share.PublishFragment;
import com.example.traveling.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private long lastCheckTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NotificationHelper.createNotificationChannel(this);
        lastCheckTime = System.currentTimeMillis();
        setupNotificationListener(currentUser.getUid());

        // Default fragment or from Intent
        handleNavigation(getIntent());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_explore) {
                replaceFragment(new ExploreFragment());
            } else if (id == R.id.nav_feed) {
                replaceFragment(new FeedFragment());
            } else if (id == R.id.nav_publish) {
                replaceFragment(new PublishFragment());
            } else if (id == R.id.nav_path) {
                replaceFragment(new TravelPathFragment());
            }
            return true;
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNavigation(intent);
    }

    private void handleNavigation(Intent intent) {
        if (intent != null && "path".equals(intent.getStringExtra("navigate_to"))) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_path);
            
            TravelPathFragment fragment = new TravelPathFragment();
            if (intent.hasExtra("target_location")) {
                Bundle args = new Bundle();
                args.putString("target_location", intent.getStringExtra("target_location"));
                fragment.setArguments(args);
            }
            replaceFragment(fragment);
        } else {
            replaceFragment(new ExploreFragment());
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void setupNotificationListener(String userId) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("posts")
                .whereGreaterThan("timestamp", lastCheckTime)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null || value.isEmpty()) return;

                    for (com.google.firebase.firestore.DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            String title = dc.getDocument().getString("title");
                            String location = dc.getDocument().getString("location");
                            String visibility = dc.getDocument().getString("visibility");
                            String postAuthorId = dc.getDocument().getString("userId");

                            // Ne pas se notifier soi-même
                            if (userId.equals(postAuthorId)) continue;

                            // Vérifier si c'est public ou un groupe
                            if ("public".equals(visibility)) {
                                NotificationHelper.showNotification(this, "Nouveau voyage public", 
                                        title + " à " + location);
                            } else {
                                NotificationHelper.showNotification(this, "Nouveau post de groupe", 
                                        "Un nouveau voyage a été partagé dans l'un de vos groupes.");
                            }
                        }
                    }
                    // Mettre à jour pour ne pas notifier plusieurs fois le même post
                    lastCheckTime = System.currentTimeMillis();
                });
    }
}
