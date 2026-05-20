package com.example.traveling.share;

import com.example.traveling.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.traveling.databinding.FragmentPublishBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PublishFragment extends Fragment {

    private FragmentPublishBinding binding;
    private Uri imageUri;
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    private List<String> groupNames = new ArrayList<>();
    private List<String> groupIds = new ArrayList<>();
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean fineLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    getLastLocation();
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.ivSelectedImage.setImageURI(imageUri);
                    binding.ivSelectedImage.setVisibility(View.VISIBLE);
                    binding.llUploadPlaceholder.setVisibility(View.GONE);
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPublishBinding.inflate(inflater, container, false);
        
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.isAnonymous()) {
            return showAnonymousView(inflater, container);
        }

        setupVisibilitySpinner();
        
        // Rendre le rectangle cliquable
        View.OnClickListener selectImageListener = v -> openGallery();
        binding.cvImageSelect.setOnClickListener(selectImageListener);
        binding.llUploadPlaceholder.setOnClickListener(selectImageListener);
        
        binding.btnPublish.setOnClickListener(v -> startPublishing());

        // Demander la position dès l'ouverture
        locationPermissionLauncher.launch(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        });

        return binding.getRoot();
    }

    private View showAnonymousView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(com.example.traveling.R.layout.fragment_explore, container, false); // On réutilise un layout de base ou on crée une vue dynamique
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(64, 64, 64, 64);
        layout.setBackgroundColor(getResources().getColor(com.example.traveling.R.color.background));
        
        android.widget.TextView tv = new android.widget.TextView(getContext());
        tv.setText("Rejoignez la communauté !\n\nConnectez-vous pour partager vos propres aventures.");
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setTextSize(18);
        tv.setTextColor(getResources().getColor(com.example.traveling.R.color.on_background));
        layout.addView(tv);

        android.widget.Button btn = new android.widget.Button(getContext());
        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 48, 0, 0);
        btn.setLayoutParams(lp);
        btn.setText("SE CONNECTER");
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.traveling.auth.LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        layout.addView(btn);

        return layout;
    }

    private void setupVisibilitySpinner() {
        groupNames.clear();
        groupIds.clear();
        groupNames.add("🌍 Public (Tout le monde)");
        groupIds.add("public");

        // Initialiser l'adapter immédiatement avec "Public"
        if (getContext() != null && binding != null) {
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                    getContext(), android.R.layout.simple_spinner_item, groupNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerVisibility.setAdapter(adapter);
        }

        // Charger les groupes et mettre à jour l'adapter
        new FirestoreRepository().getMyGroups(groups -> {
            if (groups != null && !groups.isEmpty()) {
                for (Map<String, String> group : groups) {
                    groupNames.add("👥 Groupe : " + group.get("name"));
                    groupIds.add(group.get("id"));
                }
                if (getContext() != null && binding != null && binding.spinnerVisibility.getAdapter() != null) {
                    ((android.widget.ArrayAdapter)binding.spinnerVisibility.getAdapter()).notifyDataSetChanged();
                }
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void startPublishing() {
        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();
        
        // Récupérer la catégorie
        String category = "Autre";
        int checkedId = binding.cgCategory.getCheckedChipId();
        if (checkedId == R.id.chipNature) category = "Nature";
        else if (checkedId == R.id.chipMuseum) category = "Musées";
        else if (checkedId == R.id.chipCity) category = "Ville";

        if (imageUri == null) {
            Toast.makeText(getContext(), "Veuillez sélectionner une photo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty() || location.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez remplir le titre et le lieu", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnPublish.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        uploadImage(imageUri, title, description, location, category);
    }

    private void uploadImage(Uri uri, String title, String description, String location, String category) {
        String fileName = "posts/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child(fileName);

        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    savePostToFirestore(downloadUri.toString(), title, description, location, category);
                }))
                .addOnFailureListener(e -> {
                    binding.btnPublish.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Échec de l'upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void getLastLocation() {
        android.location.LocationManager locationManager = (android.location.LocationManager) getActivity().getSystemService(android.content.Context.LOCATION_SERVICE);
        try {
            android.location.Location loc = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
            if (loc == null) {
                loc = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
            }

            if (loc != null) {
                currentLat = loc.getLatitude();
                currentLng = loc.getLongitude();
                if (binding != null) {
                    binding.tvGpsStatus.setText("GPS : " + currentLat + ", " + currentLng);
                    binding.tvGpsStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                }
            } else {
                locationManager.requestSingleUpdate(android.location.LocationManager.NETWORK_PROVIDER, new android.location.LocationListener() {
                    @Override public void onLocationChanged(@NonNull android.location.Location location) {
                        currentLat = location.getLatitude();
                        currentLng = location.getLongitude();
                        if (binding != null) {
                            binding.tvGpsStatus.setText("GPS : " + currentLat + ", " + currentLng);
                            binding.tvGpsStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        }
                    }
                    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override public void onProviderEnabled(@NonNull String provider) {}
                    @Override public void onProviderDisabled(@NonNull String provider) {}
                }, null);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void savePostToFirestore(String url, String title, String description, String location, String category) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";
        String visibility = "public";
        if (binding.spinnerVisibility.getSelectedItemPosition() >= 0 && binding.spinnerVisibility.getSelectedItemPosition() < groupIds.size()) {
            visibility = groupIds.get(binding.spinnerVisibility.getSelectedItemPosition());
        }
        
        Map<String, Object> post = new HashMap<>();
        post.put("imageUrl", url);
        post.put("title", title);
        post.put("description", description);
        post.put("location", location);
        post.put("category", category);
        post.put("userId", userId);
        post.put("timestamp", System.currentTimeMillis());
        post.put("likes", "0");
        post.put("latitude", currentLat);
        post.put("longitude", currentLng);
        post.put("visibility", visibility);

        db.collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Votre voyage a été publié !", Toast.LENGTH_LONG).show();
                    resetForm();
                })
                .addOnFailureListener(e -> {
                    binding.btnPublish.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show();
                });
    }

    private void resetForm() {
        binding.btnPublish.setEnabled(true);
        binding.progressBar.setVisibility(View.GONE);
        binding.etTitle.setText("");
        binding.etDescription.setText("");
        binding.etLocation.setText("");
        binding.ivSelectedImage.setVisibility(View.GONE);
        binding.llUploadPlaceholder.setVisibility(View.VISIBLE);
        imageUri = null;
    }
}
