package com.example.traveling.share;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.traveling.R;
import com.example.traveling.databinding.FragmentFeedBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private FragmentFeedBinding binding;
    private FeedAdapter adapter;
    private FirestoreRepository repository;
    private final List<TravelPhoto> photoList = new ArrayList<>();
    private ListenerRegistration firestoreListener;

    private final ActivityResultLauncher<Intent> voiceSearchLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    java.util.ArrayList<String> matches = result.getData().getStringArrayListExtra(
                            android.speech.RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        binding.etSearch.setText(matches.get(0));
                    }
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        repository = new FirestoreRepository();
        
        setupRecyclerView();
        
        binding.fabMap.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MapActivity.class);
            startActivity(intent);
        });

        binding.btnVoiceSearch.setOnClickListener(v -> startVoiceRecognition());

        setupSearch();
        setupFilters();
        listenToFirestore(repository.getPostsQuery());

        return binding.getRoot();
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault());
        intent.putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Dites le lieu ou le titre recherché...");
        
        try {
            voiceSearchLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "La reconnaissance vocale n'est pas disponible sur cet appareil", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        adapter = new FeedAdapter(photoList);
        binding.rvFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFeed.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            private java.util.Timer timer = new java.util.Timer();
            private final long DELAY = 500; // millisecondes

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (timer != null) timer.cancel();
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                timer = new java.util.Timer();
                timer.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (query.isEmpty()) {
                                    listenToFirestore(repository.getPostsQuery());
                                } else {
                                    listenToFirestore(repository.getSearchQuery(query));
                                }
                            });
                        }
                    }
                }, DELAY);
            }
        });
    }

    private void setupFilters() {
        binding.cgFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                listenToFirestore(repository.getPostsQuery());
                return;
            }

            int id = checkedIds.get(0);
            if (id == R.id.chipFilterAll) {
                listenToFirestore(repository.getPostsQuery());
            } else if (id == R.id.chipFilterNature) {
                listenToFirestore(repository.getCategoryQuery("Nature"));
            } else if (id == R.id.chipFilterMuseum) {
                listenToFirestore(repository.getCategoryQuery("Musées"));
            } else if (id == R.id.chipFilterCity) {
                listenToFirestore(repository.getCategoryQuery("Ville"));
            }
        });
    }

    private void listenToFirestore(Query query) {
        if (firestoreListener != null) {
            firestoreListener.remove();
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        
        firestoreListener = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getContext(), "Erreur de chargement : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }

                if (value != null) {
                    photoList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String id = doc.getId();
                        String imageUrl = doc.getString("imageUrl");
                        String location = doc.getString("location");
                        String likes = doc.getString("likes");
                        String title = doc.getString("title");
                        String description = doc.getString("description");
                        String userId = doc.getString("userId");
                        String category = doc.getString("category");
                        long timestamp = doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0;
                        double latitude = doc.getDouble("latitude") != null ? doc.getDouble("latitude") : 0.0;
                        double longitude = doc.getDouble("longitude") != null ? doc.getDouble("longitude") : 0.0;

                        photoList.add(new TravelPhoto(id, imageUrl, location, likes != null ? likes : "0", 
                                title, description, userId, category, timestamp, latitude, longitude));
                    }
                    
                    // Tri manuel par date décroissante
                    java.util.Collections.sort(photoList, (p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
                    
                    adapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
        binding = null;
    }
}
