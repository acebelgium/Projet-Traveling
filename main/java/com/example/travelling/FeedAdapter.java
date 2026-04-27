package com.example.travelling;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelling.databinding.ItemFeedBinding;

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
        holder.binding.tvLocation.setText(photo.getLocation());
        holder.binding.tvLikes.setText(photo.getLikes());

        Glide.with(holder.itemView.getContext())
                .load(photo.getImageUrl())
                .centerCrop()
                .into(holder.binding.ivPhoto);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PhotoDetailsActivity.class);
            intent.putExtra("image_url", photo.getImageUrl());
            intent.putExtra("location", photo.getLocation());
            intent.putExtra("title", photo.getTitle());
            intent.putExtra("description", photo.getDescription());
            v.getContext().startActivity(intent);
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
