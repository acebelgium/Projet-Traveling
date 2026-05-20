package com.example.traveling.share;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.traveling.databinding.ItemPhotoDetailBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class PhotoDetailsAdapter extends RecyclerView.Adapter<PhotoDetailsAdapter.ViewHolder> {
    private List<TravelPhoto> photos;
    private final FirestoreRepository repository = new FirestoreRepository();
    private final String currentUserId;

    public PhotoDetailsAdapter(List<TravelPhoto> photos) {
        this.photos = photos;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        this.currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPhotoDetailBinding binding = ItemPhotoDetailBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelPhoto photo = photos.get(position);
        holder.binding.tvLocation.setText(photo.getLocation());
        
        if (photo.getLatitude() != 0.0) {
            holder.binding.tvCoordinates.setText(String.format("%.4f, %.4f", photo.getLatitude(), photo.getLongitude()));
            holder.binding.tvCoordinates.setVisibility(View.VISIBLE);
        } else {
            holder.binding.tvCoordinates.setVisibility(View.GONE);
        }

        holder.binding.tvTitle.setText(photo.getTitle());
        holder.binding.tvDescription.setText(photo.getDescription());
        holder.binding.tvDate.setText(photo.getFormattedDate());
        
        Glide.with(holder.itemView.getContext())
                .load(photo.getImageUrl())
                .into(holder.binding.ivPhoto);

        // Charger les infos de l'auteur
        if (photo.getUserId() != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(photo.getUserId())
                    .get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            holder.binding.tvAuthorNameDetail.setText(doc.getString("name"));
                            String avatarUrl = doc.getString("profileImageUrl");
                            if (avatarUrl != null) {
                                Glide.with(holder.itemView.getContext()).load(avatarUrl).into(holder.binding.ivAuthorAvatar);
                            }
                        }
                    });

            holder.binding.llAuthorDetail.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), UserProfileActivity.class);
                intent.putExtra("userId", photo.getUserId());
                v.getContext().startActivity(intent);
            });
        }

        setupFollowButton(holder, photo);
    }

    private void setupFollowButton(ViewHolder holder, TravelPhoto photo) {
        String authorId = photo.getUserId();

        // Ne pas afficher "Suivre" pour les anonymes ou sur ses propres photos
        if (currentUserId == null || authorId == null || authorId.equals(currentUserId)) {
            holder.binding.btnFollow.setVisibility(View.GONE);
            return;
        }

        // Vérification explicite du mode anonyme
        if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {
            holder.binding.btnFollow.setVisibility(View.GONE);
            return;
        }

        holder.binding.btnFollow.setVisibility(View.VISIBLE);

        // Vérifier le statut de suivi
        repository.isFollowing(authorId, isFollowing -> {
            updateFollowButtonUI(holder, isFollowing);
            
            holder.binding.btnFollow.setOnClickListener(v -> {
                if (isFollowing) {
                    repository.unfollowUser(authorId, aVoid -> updateFollowButtonUI(holder, false));
                } else {
                    repository.followUser(authorId, aVoid -> updateFollowButtonUI(holder, true));
                }
                // Optionnel : On relance la vérification pour mettre à jour l'UI localement
                setupFollowButton(holder, photo);
            });
        });
    }

    private void updateFollowButtonUI(ViewHolder holder, boolean isFollowing) {
        if (isFollowing) {
            holder.binding.btnFollow.setText("SUIVI");
            holder.binding.btnFollow.setAlpha(0.6f);
        } else {
            holder.binding.btnFollow.setText("SUIVRE");
            holder.binding.btnFollow.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemPhotoDetailBinding binding;
        public ViewHolder(ItemPhotoDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
