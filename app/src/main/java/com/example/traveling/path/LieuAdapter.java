package com.example.traveling.path;
import com.example.traveling.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class LieuAdapter extends RecyclerView.Adapter<LieuAdapter.LieuViewHolder> {
    private List<Lieu> listeLieux;
    private List<Lieu> lieuxSelectionnes = new ArrayList<>();

    public LieuAdapter(List<Lieu> listeLieux) {
        this.listeLieux = listeLieux;
    }

    @NonNull
    @Override
    public LieuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lieux, parent, false);
        return new LieuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LieuViewHolder holder, int position) {
        Lieu lieu = listeLieux.get(position);
        holder.tvNom.setText(lieu.getNom());

        Glide.with(holder.itemView.getContext())
                .load(lieu.getImageUrl())
                .centerCrop()
                .into(holder.ivLieu);

        // Reset listener to null to avoid side effects during recycling
        holder.cbSelection.setOnCheckedChangeListener(null);
        holder.cbSelection.setChecked(lieuxSelectionnes.contains(lieu));

        holder.cbSelection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!lieuxSelectionnes.contains(lieu)) lieuxSelectionnes.add(lieu);
            } else {
                lieuxSelectionnes.remove(lieu);
            }
        });
    }

    @Override
    public int getItemCount() { return listeLieux.size(); }

    public List<Lieu> getLieuxSelectionnes() { return lieuxSelectionnes; }

    class LieuViewHolder extends RecyclerView.ViewHolder {
        TextView tvNom;
        ImageView ivLieu;
        CheckBox cbSelection;

        public LieuViewHolder(View view) {
            super(view);
            tvNom = view.findViewById(R.id.tv_nom_lieu);
            ivLieu = view.findViewById(R.id.iv_lieu);
            cbSelection = view.findViewById(R.id.cb_selection);
        }
    }
}
