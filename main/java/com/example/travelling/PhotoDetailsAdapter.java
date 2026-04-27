package com.example.travelling;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelling.databinding.ItemPhotoDetailBinding;
import java.util.List;

public class PhotoDetailsAdapter extends RecyclerView.Adapter<PhotoDetailsAdapter.ViewHolder> {
    private List<TravelPhoto> photos;

    public PhotoDetailsAdapter(List<TravelPhoto> photos) {
        this.photos = photos;
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
        holder.binding.tvTitle.setText(photo.getTitle());
        holder.binding.tvDescription.setText(photo.getDescription());
        
        Glide.with(holder.itemView.getContext())
                .load(photo.getImageUrl())
                .into(holder.binding.ivPhoto);
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
