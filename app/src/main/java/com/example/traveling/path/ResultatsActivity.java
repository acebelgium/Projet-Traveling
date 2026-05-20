package com.example.traveling.path;
import com.example.traveling.R;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.traveling.databinding.ActivityResultatsBinding;
import java.util.ArrayList;
import java.util.List;

public class ResultatsActivity extends AppCompatActivity {
    private ActivityResultatsBinding binding;
    private ParcoursAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int budgetUser = getIntent().getIntExtra("BUDGET", 100);
        int effortUser = getIntent().getIntExtra("EFFORT", 1);
        boolean meteoUser = getIntent().getBooleanExtra("METEO", false);
        String villeUser = getIntent().getStringExtra("VILLE");
        
        // Nouveaux flags accessibilité
        boolean isPmr = getIntent().getBooleanExtra("PMR", false);
        boolean isSenior = getIntent().getBooleanExtra("SENIOR", false);
        
        List<Lieu> favoris = (List<Lieu>) getIntent().getSerializableExtra("FAVORIS");

        binding.tvResultatsSousTitre.setText("Itinéraires pour " + (villeUser != null ? villeUser : "votre voyage"));
        binding.btnBack.setOnClickListener(v -> finish());

        if (favoris != null && !favoris.isEmpty()) {
            genererEtAfficher(favoris, favoris, budgetUser, effortUser, meteoUser, isPmr, isSenior);
        } else {
            LieuRepository.fetchLieux(villeUser, new LieuRepository.Callback() {
                @Override
                public void onResult(List<Lieu> lieux) {
                    genererEtAfficher(lieux, new ArrayList<>(), budgetUser, effortUser, meteoUser, isPmr, isSenior);
                }
                @Override public void onCityNotFound() { finish(); }
                @Override public void onError(Throwable t) { finish(); }
            });
        }
    }

    private void genererEtAfficher(List<Lieu> sources, List<Lieu> favoris, int budget, int effort, boolean meteo, boolean isPmr, boolean isSenior) {
        List<Parcours> resultats = GenerateurParcours.genererOptions(sources, favoris, budget, effort, meteo);

        // AJOUT DES RECOMMANDATIONS EN LIGNE (MOCK SHARE)
        resultats.addAll(getRecommandationsEnLigne());

        binding.rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        // On passe les flags à l'adapter pour le calcul du temps dans la liste
        adapter = new ParcoursAdapter(resultats, isPmr, isSenior);
        binding.rvSuggestions.setAdapter(adapter);
    }

    private List<Parcours> getRecommandationsEnLigne() {
        List<Parcours> recos = new ArrayList<>();
        
        // Simulation de parcours postés par la communauté
        List<Lieu> lilleLieux = new ArrayList<>();
        lilleLieux.add(new Lieu("Grand Place", "Le coeur de Lille", "", "Culture", 0.0, 30, 50.63, 3.06));
        lilleLieux.add(new Lieu("Vieille Bourse", "Architecture flamande", "", "Culture", 0.0, 45, 50.63, 3.06));
        
        Parcours p1 = new Parcours("Best of Lille (par @Alice)", lilleLieux, "Facile");
        p1.setTitre("Populaire : Lille en 3h");
        
        recos.add(p1);
        return recos;
    }
}
