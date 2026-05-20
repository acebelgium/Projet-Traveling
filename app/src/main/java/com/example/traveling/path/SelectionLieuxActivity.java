package com.example.traveling.path;
import com.example.traveling.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SelectionLieuxActivity extends AppCompatActivity {

    private RecyclerView rvLieux;
    private Button btnConfirmer;
    private ProgressBar pbLoading;
    private LieuAdapter adapter;
    private List<Lieu> listeDeBase = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_lieux);

        rvLieux = findViewById(R.id.rv_lieux);
        btnConfirmer = findViewById(R.id.btn_confirmer);
        pbLoading = findViewById(R.id.pb_loading);

        String ville = getIntent().getStringExtra("VILLE");
        if (ville == null) ville = "Paris";

        adapter = new LieuAdapter(listeDeBase);
        rvLieux.setLayoutManager(new LinearLayoutManager(this));
        rvLieux.setAdapter(adapter);

        chargerLieux(ville);

        btnConfirmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Lieu> selection = adapter.getLieuxSelectionnes();
                Intent intent = new Intent(SelectionLieuxActivity.this, ResultatsActivity.class);
                intent.putExtra("VILLE", getIntent().getStringExtra("VILLE"));
                intent.putExtra("BUDGET", getIntent().getIntExtra("BUDGET", 100));
                intent.putExtra("EFFORT", getIntent().getIntExtra("EFFORT", 1));
                intent.putExtra("METEO", getIntent().getBooleanExtra("METEO", false));
                intent.putExtra("PMR", getIntent().getBooleanExtra("PMR", false));
                intent.putExtra("SENIOR", getIntent().getBooleanExtra("SENIOR", false));
                intent.putExtra("CATEGORIES", getIntent().getStringArrayListExtra("CATEGORIES"));
                intent.putExtra("FAVORIS", new ArrayList<>(selection));
                startActivity(intent);
            }
        });
    }

    private void chargerLieux(String ville) {
        pbLoading.setVisibility(View.VISIBLE);
        ArrayList<String> selectedCats = getIntent().getStringArrayListExtra("CATEGORIES");

        LieuRepository.fetchLieux(ville, new LieuRepository.Callback() {
            @Override
            public void onResult(List<Lieu> lieux) {
                pbLoading.setVisibility(View.GONE);
                listeDeBase.clear();
                
                if (selectedCats == null || selectedCats.isEmpty()) {
                    listeDeBase.addAll(lieux);
                } else {
                    for (Lieu l : lieux) {
                        if (selectedCats.contains(l.getCategorie())) {
                            listeDeBase.add(l);
                        }
                    }
                }
                
                adapter.notifyDataSetChanged();
                
                if (listeDeBase.isEmpty()) {
                    Toast.makeText(SelectionLieuxActivity.this, "Aucun lieu correspondant trouvé.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCityNotFound() {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(SelectionLieuxActivity.this, "La ville '" + ville + "' n'existe pas ou est introuvable.", Toast.LENGTH_LONG).show();
                // On peut proposer de revenir en arrière
            }

            @Override
            public void onError(Throwable t) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(SelectionLieuxActivity.this, "Erreur de connexion internet", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
