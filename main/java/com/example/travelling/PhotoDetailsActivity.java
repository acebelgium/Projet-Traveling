package com.example.travelling;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.travelling.databinding.ActivityPhotoDetailsBinding;
import java.util.ArrayList;
import java.util.List;

public class PhotoDetailsActivity extends AppCompatActivity {
    private ActivityPhotoDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhotoDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        // Récupérer les données passées par l'Intent
        String imageUrl = getIntent().getStringExtra("image_url");
        String location = getIntent().getStringExtra("location");
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");

        // Créer l'objet photo à partir des extras
        List<TravelPhoto> photos = new ArrayList<>();
        if (imageUrl != null) {
            photos.add(new TravelPhoto("current", imageUrl, location, "", title, description));
        }
        
        // Ajouter le reste pour le carrousel
        photos.addAll(MockRepository.getFeedPhotos());
        
        PhotoDetailsAdapter adapter = new PhotoDetailsAdapter(photos);
        binding.viewPager.setAdapter(adapter);
    }
}
