package com.example.traveling.path;

import com.example.traveling.databinding.FragmentTravelPathBinding;
import com.bumptech.glide.Glide;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.traveling.R;

public class TravelPathFragment extends Fragment {
    private FragmentTravelPathBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTravelPathBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Chargement des images pour les destinations populaires
        Glide.with(this).load("https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=400").into(binding.ivParis);
        Glide.with(this).load("https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?auto=format&fit=crop&w=400").into(binding.ivLondon);
        Glide.with(this).load("https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=400").into(binding.ivNy);

        binding.sbBudget.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                binding.tvBudgetVal.setText("Budget : " + progress + "€");
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });

        // Gestion manuelle des clics sur les cartes (plus fiable que les chips pour le debug)
        binding.cardParis.setOnClickListener(v -> binding.etVille.setText("Paris"));
        binding.cardLondon.setOnClickListener(v -> binding.etVille.setText("Londres"));
        binding.cardNy.setOnClickListener(v -> binding.etVille.setText("New York"));

        binding.btnMesFavoris.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), FavorisActivity.class));
        });

        binding.btnGenerer.setOnClickListener(v -> lancerSelectionLieux());
    }

    private void lancerSelectionLieux() {
        String ville = binding.etVille.getText().toString().trim();
        int budget = binding.sbBudget.getProgress();
        boolean meteo = binding.swMeteo.isChecked();

        if (ville.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez choisir une destination", Toast.LENGTH_SHORT).show();
            return;
        }

        java.util.ArrayList<String> categories = new java.util.ArrayList<>();
        if (binding.chipMusees.isChecked()) categories.add("Musées");
        if (binding.chipParcs.isChecked()) categories.add("Parcs");
        if (binding.chipMonuments.isChecked()) categories.add("Monuments");
        if (binding.chipResto.isChecked()) categories.add("Restauration");

        Intent intent = new Intent(getContext(), SelectionLieuxActivity.class);
        intent.putExtra("VILLE", ville);
        intent.putExtra("BUDGET", budget);
        intent.putExtra("METEO", meteo);
        intent.putExtra("CATEGORIES", categories);
        
        // On passe aussi les critères PMR/Sénior si besoin
        intent.putExtra("PMR", binding.chipPrm.isChecked());
        intent.putExtra("SENIOR", binding.chipSenior.isChecked());
        
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
