package com.example.traveling.share;

import com.example.traveling.MainActivity;
import com.example.traveling.R;
import com.example.traveling.auth.LoginActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.traveling.databinding.ActivityPhotoDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoDetailsActivity extends AppCompatActivity {
    private ActivityPhotoDetailsBinding binding;
    private List<TravelPhoto> photosList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhotoDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        // Permettre d'accéder au profil de l'auteur
        binding.headerContainer.setOnClickListener(v -> {
            int currentPos = binding.viewPager.getCurrentItem();
            TravelPhoto photo = photosList.get(currentPos);
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("userId", photo.getUserId());
            startActivity(intent);
        });

        binding.btnBack.setOnClickListener(v -> finish());

        // Récupérer les données passées par l'Intent
        String imageUrl = getIntent().getStringExtra("image_url");
        String location = getIntent().getStringExtra("location");
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        long timestamp = getIntent().getLongExtra("timestamp", 0);
        String likes = getIntent().getStringExtra("likes");
        String id = getIntent().getStringExtra("id");
        String postUserId = getIntent().getStringExtra("user_id");
        String category = getIntent().getStringExtra("category");
        double latitude = getIntent().getDoubleExtra("latitude", 0.0);
        double longitude = getIntent().getDoubleExtra("longitude", 0.0);

        // Créer l'objet photo à partir des extras
        if (imageUrl != null) {
            photosList.add(new TravelPhoto(id != null ? id : "current", imageUrl, location, likes != null ? likes : "0", 
                title, description, postUserId != null ? postUserId : "mock_user", 
                category != null ? category : "Autre", timestamp, latitude, longitude));
        }
        
        PhotoDetailsAdapter adapter = new PhotoDetailsAdapter(photosList);
        binding.viewPager.setAdapter(adapter);

        binding.btnSimilar.setOnClickListener(v -> {
            int currentPos = binding.viewPager.getCurrentItem();
            TravelPhoto photo = photosList.get(currentPos);
            showSimilarPhotosBottomSheet(photo.getCategory(), photo.getId());
        });

        // Vérifier si le post actuel est déjà liké par l'utilisateur
        checkIfLiked(photosList.get(0).getId());

        // Mettre à jour la vérification lors du défilement
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                TravelPhoto photo = photosList.get(position);
                checkIfLiked(photo.getId());
                binding.tvLikes.setText(photo.getLikes());
            }
        });

        // Actions sur les boutons du footer
        setupFooterActions();
    }

    private void showLoginDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Compte requis")
                .setMessage("Vous devez être connecté avec un compte réel pour aimer ce voyage.")
                .setPositiveButton("Se connecter", (dialog, which) -> {
                    auth.signOut();
                    finishAffinity();
                    startActivity(new Intent(this, LoginActivity.class));
                })
                .setNegativeButton("Plus tard", null)
                .show();
    }

    private void checkIfLiked(String postId) {
        if (postId == null || postId.equals("current") || auth.getCurrentUser() == null) {
            updateLikeUI(false);
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String likeId = userId + "_" + postId;

        db.collection("likes").document(likeId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                updateLikeUI(true);
            } else {
                updateLikeUI(false);
            }
        });
    }

    private void updateLikeUI(boolean isLiked) {
        if (isLiked) {
            binding.ivLike.setImageResource(android.R.drawable.btn_star_big_on);
            binding.ivLike.setTag(true);
        } else {
            binding.ivLike.setImageResource(android.R.drawable.btn_star_big_off);
            binding.ivLike.setTag(false);
        }
    }

    private void setupFooterActions() {
        // Vérification de la propriété au chargement initial
        checkPostOwnership(photosList.get(0));

        binding.llLike.setOnClickListener(v -> {
            // ... (logique existante)
            com.google.firebase.auth.FirebaseUser user = auth.getCurrentUser();
            
            if (user == null || user.isAnonymous()) {
                // ... (boîte de dialogue de connexion existante)
                showLoginDialog();
                return;
            }

            int currentPos = binding.viewPager.getCurrentItem();
            TravelPhoto photo = photosList.get(currentPos);
            String postId = photo.getId();
            
            if (postId != null && !postId.equals("current") && postId.length() > 5) {
                boolean isAlreadyLiked = binding.ivLike.getTag() != null && (boolean) binding.ivLike.getTag();
                String userId = user.getUid();
                String likeId = userId + "_" + postId;

                if (!isAlreadyLiked) {
                    // Ajouter le Like via Transaction
                    new FirestoreRepository().toggleLike(postId, true, aVoid -> {
                        int newLikesCount = Integer.parseInt(photo.getLikes()) + 1;
                        photo.setLikes(String.valueOf(newLikesCount));
                        binding.tvLikes.setText(String.valueOf(newLikesCount));
                        updateLikeUI(true);
                    });
                } else {
                    // Retirer le Like via Transaction
                    new FirestoreRepository().toggleLike(postId, false, aVoid -> {
                        int newLikesCount = Math.max(0, Integer.parseInt(photo.getLikes()) - 1);
                        photo.setLikes(String.valueOf(newLikesCount));
                        binding.tvLikes.setText(String.valueOf(newLikesCount));
                        updateLikeUI(false);
                    });
                }
            }
        });

        binding.llComments.setOnClickListener(v -> {
            int currentPos = binding.viewPager.getCurrentItem();
            TravelPhoto photo = photosList.get(currentPos);
            showCommentsBottomSheet(photo.getId());
        });

        binding.llReport.setOnClickListener(v -> {
            int currentPos = binding.viewPager.getCurrentItem();
            TravelPhoto photo = photosList.get(currentPos);

            new AlertDialog.Builder(this)
                    .setTitle("Signaler ce contenu")
                    .setMessage("Voulez-vous signaler ce voyage pour contenu inapproprié ?")
                    .setPositiveButton("Signaler", (dialog, which) -> {
                        saveReportToFirestore(photo);
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });

        binding.llMaps.setOnClickListener(v -> {
            int currentPos = binding.viewPager.getCurrentItem();
            TravelPhoto photo = photosList.get(currentPos);
            
            Uri gmmIntentUri;
            if (photo.getLatitude() != 0.0 && photo.getLongitude() != 0.0) {
                // geo:lat,lng?q=lat,lng(Label) -> Affiche un marqueur à la position exacte
                String query = photo.getLatitude() + "," + photo.getLongitude() + "(" + Uri.encode(photo.getLocation()) + ")";
                gmmIntentUri = Uri.parse("geo:" + photo.getLatitude() + "," + photo.getLongitude() + "?q=" + query);
            } else {
                // Repli sur le nom du lieu si pas de coordonnées
                gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(photo.getLocation()));
            }
            
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            // On ne force pas le package Maps au cas où l'utilisateur préfère Waze ou autre, 
            // mais on garde la logique de fallback si besoin.
            startActivity(mapIntent);
        });

        binding.btnGoThere.setOnClickListener(v -> {
            int currentPos = binding.viewPager.getCurrentItem();
            TravelPhoto photo = photosList.get(currentPos);
            
            Toast.makeText(this, "Destination ajoutée à votre itinéraire !", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("navigate_to", "path");
            intent.putExtra("target_location", photo.getLocation());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // Réinitialiser l'icône like et mettre à jour le compteur quand on change de photo
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                TravelPhoto photo = photosList.get(position);
                checkIfLiked(photo.getId());
                binding.tvLikes.setText(photo.getLikes());
                checkPostOwnership(photo);
            }
        });

        // Bouton de suppression
        binding.llDelete.setOnClickListener(v -> {
            int currentPos = binding.viewPager.getCurrentItem();
            TravelPhoto photo = photosList.get(currentPos);
            
            new AlertDialog.Builder(this)
                    .setTitle("Supprimer le voyage")
                    .setMessage("Êtes-vous sûr de vouloir supprimer ce souvenir de façon permanente ?")
                    .setPositiveButton("Supprimer", (dialog, which) -> deletePost(photo))
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    private void checkPostOwnership(TravelPhoto photo) {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (currentUserId != null && currentUserId.equals(photo.getUserId())) {
            binding.llDelete.setVisibility(View.VISIBLE);
        } else {
            binding.llDelete.setVisibility(View.GONE);
        }
    }

    private void deletePost(TravelPhoto photo) {
        if (photo.getId() == null || photo.getId().equals("current") || photo.getId().length() < 5) {
            Toast.makeText(this, "Impossible de supprimer une démo", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("posts").document(photo.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Voyage supprimé", Toast.LENGTH_SHORT).show();
                    finish(); // Fermer l'activité car le post n'existe plus
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show());
    }

    private void saveReportToFirestore(TravelPhoto photo) {
        if (photo.getId() == null || photo.getId().equals("current")) return;

        Map<String, Object> report = new HashMap<>();
        report.put("postId", photo.getId());
        report.put("reporterId", auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous");
        report.put("timestamp", System.currentTimeMillis());
        report.put("status", "pending");

        db.collection("reports").add(report)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Signalement enregistré. Merci pour votre vigilance.", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de l'envoi du signalement", Toast.LENGTH_SHORT).show();
                });
    }

    private void showCommentsBottomSheet(String postId) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_comments, null);
        bottomSheetDialog.setContentView(view);

        androidx.recyclerview.widget.RecyclerView rvComments = view.findViewById(R.id.rvComments);
        android.widget.EditText etComment = view.findViewById(R.id.etComment);
        android.widget.ImageButton btnSend = view.findViewById(R.id.btnSendComment);

        List<Comment> commentList = new ArrayList<>();
        CommentAdapter adapter = new CommentAdapter(commentList);
        rvComments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        rvComments.setAdapter(adapter);

        // Charger les commentaires
        new FirestoreRepository().getCommentsQuery(postId).addSnapshotListener((value, error) -> {
            if (value != null) {
                commentList.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                    commentList.add(doc.toObject(Comment.class));
                }
                adapter.notifyDataSetChanged();
                rvComments.scrollToPosition(commentList.size() - 1);
            }
        });

        btnSend.setOnClickListener(v -> {
            String text = etComment.getText().toString().trim();
            if (text.isEmpty()) return;

            if (auth.getCurrentUser() == null || auth.getCurrentUser().isAnonymous()) {
                showLoginDialog();
                return;
            }

            new FirestoreRepository().addComment(postId, text, aVoid -> {
                etComment.setText("");
                // Le SnapshotListener s'occupe de mettre à jour la liste
            });
        });

        bottomSheetDialog.show();
    }

    private void showSimilarPhotosBottomSheet(String category, String currentId) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_similar, null);
        bottomSheetDialog.setContentView(view);

        androidx.recyclerview.widget.RecyclerView rvSimilar = view.findViewById(R.id.rvSimilar);
        List<TravelPhoto> similarList = new ArrayList<>();
        SimilarPhotosAdapter adapter = new SimilarPhotosAdapter(similarList, photo -> {
            // Ouvrir ce nouveau voyage
            Intent intent = new Intent(this, PhotoDetailsActivity.class);
            intent.putExtra("image_url", photo.getImageUrl());
            intent.putExtra("location", photo.getLocation());
            intent.putExtra("title", photo.getTitle());
            intent.putExtra("description", photo.getDescription());
            intent.putExtra("timestamp", photo.getTimestamp());
            intent.putExtra("likes", photo.getLikes());
            intent.putExtra("id", photo.getId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            bottomSheetDialog.dismiss();
        });

        rvSimilar.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        rvSimilar.setAdapter(adapter);

        // Requête Firestore pour trouver des photos de la même catégorie (uniquement publiques)
        new FirestoreRepository().getCategoryQuery(category).get().addOnSuccessListener(queryDocumentSnapshots -> {
            similarList.clear();
            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (doc.getId().equals(currentId)) continue;
                
                similarList.add(new TravelPhoto(
                        doc.getId(),
                        doc.getString("imageUrl"),
                        doc.getString("location"),
                        doc.getString("likes"),
                        doc.getString("title"),
                        doc.getString("description"),
                        doc.getString("userId"),
                        doc.getString("category"),
                        doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0
                ));
            }
            adapter.notifyDataSetChanged();
        });

        bottomSheetDialog.show();
    }
}
