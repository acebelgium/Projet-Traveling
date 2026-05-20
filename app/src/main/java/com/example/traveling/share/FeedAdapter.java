package com.example.traveling.share;
import com.example.traveling.R;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.traveling.databinding.ItemFeedBinding;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private List<TravelPhoto> photos;

    public FeedAdapter(List<TravelPhoto> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFeedBinding binding = ItemFeedBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelPhoto photo = photos.get(position);
        
        holder.binding.tvTitle.setText(photo.getTitle());
        holder.binding.tvLocation.setText(photo.getLocation());
        
        // Charger le nom de l'auteur
        if (photo.getUserId() != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(photo.getUserId())
                    .get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            holder.binding.tvAuthorName.setText(doc.getString("name"));
                        }
                    });
            
            holder.binding.llAuthor.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
                intent.putExtra("userId", photo.getUserId());
                v.getContext().startActivity(intent);
            });
        }

        if (photo.getLatitude() != 0.0) {
            holder.binding.tvCoordinates.setText(String.format("%.4f, %.4f", photo.getLatitude(), photo.getLongitude()));
            holder.binding.tvCoordinates.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.binding.tvCoordinates.setVisibility(android.view.View.GONE);
        }

        holder.binding.tvLikes.setText(photo.getLikes());
        holder.binding.tvDate.setText(photo.getFormattedDate());

        Glide.with(holder.itemView.getContext())
                .load(photo.getImageUrl())
                .centerCrop()
                .placeholder(R.color.surface_container)
                .into(holder.binding.ivPhoto);

        // Animation simple du bouton Like
        holder.binding.btnLike.setOnClickListener(v -> {
            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (user == null || user.isAnonymous()) {
                Toast.makeText(v.getContext(), "Connectez-vous pour aimer ce voyage", Toast.LENGTH_SHORT).show();
                return;
            }
            
            holder.binding.btnLike.setImageResource(android.R.drawable.btn_star_big_on);
            // La logique complète de toggle pourrait être ajoutée ici aussi
        });

        holder.itemView.setOnClickListener(v -> {
            android.app.Activity activity = (android.app.Activity) v.getContext();
            Intent intent = new Intent(activity, PhotoDetailsActivity.class);
            intent.putExtra("image_url", photo.getImageUrl());
            intent.putExtra("location", photo.getLocation());
            intent.putExtra("title", photo.getTitle());
            intent.putExtra("description", photo.getDescription());
            intent.putExtra("date", photo.getFormattedDate());
            intent.putExtra("timestamp", photo.getTimestamp());
            intent.putExtra("likes", photo.getLikes());
            intent.putExtra("id", photo.getId());
            intent.putExtra("user_id", photo.getUserId());
            intent.putExtra("category", photo.getCategory());
            intent.putExtra("latitude", photo.getLatitude());
            intent.putExtra("longitude", photo.getLongitude());

            androidx.core.app.ActivityOptionsCompat options = androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity, holder.binding.ivPhoto, "hero_image"
            );
            activity.startActivity(intent, options.toBundle());
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemFeedBinding binding;
        public ViewHolder(ItemFeedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
