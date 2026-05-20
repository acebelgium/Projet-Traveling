package com.example.traveling.share;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.traveling.R;

import java.util.List;

public class SimilarPhotosAdapter extends RecyclerView.Adapter<SimilarPhotosAdapter.ViewHolder> {

    private final List<TravelPhoto> photos;
    private final OnPhotoClickListener listener;

    public interface OnPhotoClickListener {
        void onPhotoClick(TravelPhoto photo);
    }

    public SimilarPhotosAdapter(List<TravelPhoto> photos, OnPhotoClickListener listener) {
        this.photos = photos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_similar_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelPhoto photo = photos.get(position);
        Glide.with(holder.itemView.getContext())
                .load(photo.getImageUrl())
                .centerCrop()
                .into(holder.ivPhoto);

        holder.itemView.setOnClickListener(v -> listener.onPhotoClick(photo));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhotoSimilar);
        }
    }
}
