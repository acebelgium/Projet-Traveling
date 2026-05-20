package com.example.traveling;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.traveling.auth.LoginActivity;
import com.example.traveling.databinding.ActivityNotificationSettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class NotificationSettingsActivity extends AppCompatActivity {
    private ActivityNotificationSettingsBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private boolean isInitialLoad = true;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Les notifications sont désactivées au niveau système", Toast.LENGTH_LONG).show();
                    binding.swFollowedUsers.setChecked(false);
                    binding.swGroupAlerts.setChecked(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnLogout.setOnClickListener(v -> logout());

        binding.contentLayout.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        loadSettings();
    }

    private void loadSettings() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.isAnonymous()) {
            binding.progressBar.setVisibility(View.GONE);
            binding.contentLayout.setVisibility(View.VISIBLE);
            disableSwitchesForAnonymous();
            return;
        }

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean followedUsers = documentSnapshot.getBoolean("notifFollowedUsers");
                        Boolean groupAlerts = documentSnapshot.getBoolean("notifGroupAlerts");

                        isInitialLoad = true;
                        if (followedUsers != null) binding.swFollowedUsers.setChecked(followedUsers);
                        if (groupAlerts != null) binding.swGroupAlerts.setChecked(groupAlerts);
                        isInitialLoad = false;
                    }
                    
                    binding.progressBar.setVisibility(View.GONE);
                    binding.contentLayout.setVisibility(View.VISIBLE);
                    setupListeners();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.contentLayout.setVisibility(View.VISIBLE);
                    setupListeners();
                });
    }

    private void setupListeners() {
        binding.swFollowedUsers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInitialLoad) {
                if (isChecked) checkNotificationPermission();
                saveSetting("notifFollowedUsers", isChecked);
            }
        });
        binding.swGroupAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInitialLoad) {
                if (isChecked) checkNotificationPermission();
                saveSetting("notifGroupAlerts", isChecked);
            }
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void saveSetting(String key, boolean value) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.isAnonymous()) return;

        Map<String, Object> update = new HashMap<>();
        update.put(key, value);

        db.collection("users").document(user.getUid())
                .set(update, SetOptions.merge());
    }

    private void disableSwitchesForAnonymous() {
        binding.swFollowedUsers.setEnabled(false);
        binding.swGroupAlerts.setEnabled(false);
        // On change le texte du bouton logout pour les anonymes
        // binding.btnLogout.setVisibility(View.GONE); // Optionnel
    }

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
