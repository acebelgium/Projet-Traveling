package com.example.travelling;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.travelling.databinding.FragmentExploreBinding;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Données provenant du Repository
        UserStats stats = MockRepository.getUserStats();
        
        // Configuration initiale (en attendant l'enrichissement de l'UI)
        binding.cvProfile.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), NotificationSettingsActivity.class));
        });

        binding.cvHero.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), PhotoDetailsActivity.class));
        });
        
        // TODO: Initialiser les listes horizontales (Trending Journeys)
    }
}
