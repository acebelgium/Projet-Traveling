package com.example.traveling.path;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.traveling.databinding.ActivityResultatsBinding;
import java.util.List;

public class FavorisActivity extends AppCompatActivity {
    private ActivityResultatsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvResultatsTitre.setText("Mes Favoris");
        binding.tvResultatsSousTitre.setText("Retrouvez vos parcours sauvegardés");

        List<Parcours> favs = FavoritesManager.getFavorites(this);
        
        binding.rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSuggestions.setAdapter(new ParcoursAdapter(favs));

        binding.btnBack.setOnClickListener(v -> finish());
    }
}
