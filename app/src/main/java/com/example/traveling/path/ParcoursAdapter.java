package com.example.traveling.path;
import com.example.traveling.R;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ParcoursAdapter extends RecyclerView.Adapter<ParcoursAdapter.ViewHolder> {
    private List<Parcours> listeParcours;
    private boolean isPmr;
    private boolean isSenior;

    public ParcoursAdapter(List<Parcours> listeParcours) {
        this(listeParcours, false, false);
    }

    public ParcoursAdapter(List<Parcours> listeParcours, boolean isPmr, boolean isSenior) {
        this.listeParcours = listeParcours;
        this.isPmr = isPmr;
        this.isSenior = isSenior;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parcours, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Parcours p = listeParcours.get(position);
        holder.tvTitre.setText(p.getTitre());
        holder.tvBudget.setText(p.getBudgetTotal() + "€");
        holder.tvEtapes.setText(String.valueOf(p.getNbEtapes()));
        holder.tvEffort.setText(p.getNiveauEffort());

        // Calcul du temps adapté au profil
        double vitesse = (isPmr || isSenior) ? 3.0 : 5.0;
        int dureeTotale = 0;
        for (int i = 0; i < p.getEtapes().size(); i++) {
            dureeTotale += p.getEtapes().get(i).getTempsVisiteMinutes();
            if (i > 0) {
                Lieu l1 = p.getEtapes().get(i-1);
                Lieu l2 = p.getEtapes().get(i);
                double d = calculerDistanceKM(l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude());
                dureeTotale += (int)(d * (60.0 / vitesse));
            }
        }

        int h = dureeTotale / 60;
        int m = dureeTotale % 60;
        holder.tvDuree.setText(h + "h " + m + "m");

        // État initial du Like (persistant)
        updateLikeIcon(holder.btnLike, FavoritesManager.isFavorite(holder.itemView.getContext(), p.getTitre()));

        holder.btnLike.setOnClickListener(v -> {
            if (FavoritesManager.isFavorite(holder.itemView.getContext(), p.getTitre())) {
                FavoritesManager.removeParcours(holder.itemView.getContext(), p.getTitre());
            } else {
                FavoritesManager.saveParcours(holder.itemView.getContext(), p);
            }
            updateLikeIcon(holder.btnLike, FavoritesManager.isFavorite(holder.itemView.getContext(), p.getTitre()));
        });

        holder.btnExportPdf.setOnClickListener(v -> {
            PdfExporter.exportParcours(holder.itemView.getContext(), p);
        });

        holder.btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ParcoursDetailsActivity.class);
            intent.putExtra("PARCOURS", p);
            intent.putExtra("PMR", isPmr);
            intent.putExtra("SENIOR", isSenior);
            holder.itemView.getContext().startActivity(intent);
        });

        // Couleurs des badges
        if (p.getNiveauEffort().equals("Facile")) {
            holder.tvEffort.setBackgroundTintList(holder.itemView.getContext().getColorStateList(R.color.tertiary));
        } else if (p.getNiveauEffort().equals("Moyen")) {
            holder.tvEffort.setBackgroundTintList(holder.itemView.getContext().getColorStateList(R.color.secondary));
        } else {
            holder.tvEffort.setBackgroundTintList(holder.itemView.getContext().getColorStateList(R.color.primary));
        }
    }

    private double calculerDistanceKM(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) 
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        return dist * 60 * 1.853159616;
    }

    private void updateLikeIcon(ImageButton btn, boolean isFav) {
        if (isFav) {
            btn.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            btn.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    @Override
    public int getItemCount() { return listeParcours.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitre, tvBudget, tvEtapes, tvDuree, tvEffort;
        Button btnDetails, btnExportPdf;
        ImageButton btnLike;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitre = itemView.findViewById(R.id.tv_titre_parcours);
            tvBudget = itemView.findViewById(R.id.tv_res_budget);
            tvEtapes = itemView.findViewById(R.id.tv_res_etapes);
            tvDuree = itemView.findViewById(R.id.tv_res_duree);
            tvEffort = itemView.findViewById(R.id.tv_badge_effort);
            btnDetails = itemView.findViewById(R.id.btn_voir_details);
            btnExportPdf = itemView.findViewById(R.id.btn_export_pdf);
            btnLike = itemView.findViewById(R.id.btn_like_parcours);
        }
    }
}
