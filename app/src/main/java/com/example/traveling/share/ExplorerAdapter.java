package com.example.traveling.share;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.traveling.databinding.ItemExplorerBinding;
import java.util.List;
import java.util.Map;

public class ExplorerAdapter extends RecyclerView.Adapter<ExplorerAdapter.ViewHolder> {
    private List<Map<String, Object>> users;
    private final FirestoreRepository repository = new FirestoreRepository();

    public ExplorerAdapter(List<Map<String, Object>> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExplorerBinding binding = ItemExplorerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> user = users.get(position);
        String name = (String) user.get("name");
        String uid = (String) user.get("uid");

        holder.binding.tvName.setText(name);

        String imageUrl = (String) user.get("profileImageUrl");
        if (imageUrl != null) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .into(holder.binding.ivProfile);
        } else {
            holder.binding.ivProfile.setImageResource(com.example.traveling.R.color.secondary_container);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
            intent.putExtra("userId", uid);
            v.getContext().startActivity(intent);
        });

        // Masquer le bouton suivre pour les anonymes
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.isAnonymous()) {
            holder.binding.btnFollow.setVisibility(android.view.View.GONE);
            return;
        }

        holder.binding.btnFollow.setVisibility(android.view.View.VISIBLE);
        
        repository.isFollowing(uid, isFollowing -> {
            holder.binding.btnFollow.setText(isFollowing ? "SUIVI" : "SUIVRE");
            holder.binding.btnFollow.setAlpha(isFollowing ? 0.6f : 1.0f);
        });

        holder.binding.btnFollow.setOnClickListener(v -> {
            repository.isFollowing(uid, isFollowing -> {
                if (isFollowing) {
                    repository.unfollowUser(uid, aVoid -> notifyItemChanged(position));
                } else {
                    repository.followUser(uid, aVoid -> notifyItemChanged(position));
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemExplorerBinding binding;
        public ViewHolder(ItemExplorerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
