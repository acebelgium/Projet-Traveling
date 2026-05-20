package com.example.traveling.path;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.traveling.R;
import java.util.Collections;
import java.util.List;

public class EtapeAdapter extends RecyclerView.Adapter<EtapeAdapter.ViewHolder> {
    private List<Lieu> etapes;
    private boolean isPmr;
    private boolean isSenior;

    public EtapeAdapter(List<Lieu> etapes, boolean isPmr, boolean isSenior) {
        this.etapes = etapes;
        this.isPmr = isPmr;
        this.isSenior = isSenior;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_etape_planning, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lieu lieu = etapes.get(position);
        
        // Calcul dynamique des horaires basé sur le profil
        double vitesseKmh = (isPmr || isSenior) ? 3.0 : 5.0;
        int tempsDepuisDebut = 0;
        for (int i = 0; i < position; i++) {
            Lieu prev = etapes.get(i);
            Lieu next = etapes.get(i+1);
            double dist = calculerDistanceKM(prev.getLatitude(), prev.getLongitude(), next.getLatitude(), next.getLongitude());
            int minutesTrajet = (int) (dist * (60.0 / vitesseKmh));
            if (minutesTrajet < 5) minutesTrajet = 10;
            
            tempsDepuisDebut += etapes.get(i).getTempsVisiteMinutes() + minutesTrajet;
        }
        
        int totalMinutes = 540 + tempsDepuisDebut; // 09h00
        int h = totalMinutes / 60;
        int m = totalMinutes % 60;
        
        holder.tvHeure.setText(String.format("%02dh%02d", h, m));
        holder.tvNom.setText(lieu.getNom());
        holder.tvDuree.setText(lieu.getTempsVisiteMinutes() + " min de visite");
        
        // Alerte accessibilité PMR
        String desc = lieu.getDescription().toLowerCase();
        boolean hasPmrAlert = isPmr && (desc.contains("escalier") || desc.contains("marche") || desc.contains("pente") || desc.contains("difficile"));
        
        // Alerte Horaires (Fermeture)
        boolean isClosed = (totalMinutes + lieu.getTempsVisiteMinutes()) > (lieu.getHeureFermeture() * 60);
        
        if (hasPmrAlert || isClosed) {
            holder.tvPmrAlert.setVisibility(View.VISIBLE);
            String msg = "";
            if (hasPmrAlert) msg += "⚠ Accès difficile (PMR) ";
            if (isClosed) msg += "🕒 Fermé à cette heure";
            holder.tvPmrAlert.setText(msg);
        } else {
            holder.tvPmrAlert.setVisibility(View.GONE);
        }

        // Style spécial Restauration
        if ("Restauration".equals(lieu.getCategorie())) {
            holder.tvNom.setTextColor(holder.itemView.getContext().getColor(R.color.tertiary));
        } else {
            holder.tvNom.setTextColor(holder.itemView.getContext().getColor(R.color.on_surface));
        }

        Glide.with(holder.itemView.getContext()).load(lieu.getImageUrl()).centerCrop().into(holder.ivLieu);
    }

    private double calculerDistanceKM(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) 
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        return dist * 60 * 1.853159616;
    }

    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(etapes, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        notifyItemRangeChanged(Math.min(fromPosition, toPosition), etapes.size());
    }

    @Override
    public int getItemCount() { return etapes.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeure, tvNom, tvDuree, tvPmrAlert;
        ImageView ivLieu;

        public ViewHolder(View itemView) {
            super(itemView);
            tvHeure = itemView.findViewById(R.id.tv_heure_etape);
            tvNom = itemView.findViewById(R.id.tv_nom_lieu_etape);
            tvDuree = itemView.findViewById(R.id.tv_duree_etape);
            ivLieu = itemView.findViewById(R.id.iv_lieu_etape);
            tvPmrAlert = itemView.findViewById(R.id.tv_pmr_alert);
        }
    }
}
