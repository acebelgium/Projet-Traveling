package com.example.traveling;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.traveling.databinding.FragmentExploreBinding;
import com.example.traveling.share.ExplorerAdapter;
import com.example.traveling.share.FirestoreRepository;
import com.example.traveling.share.GroupAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private final FirestoreRepository repository = new FirestoreRepository();
    private final List<Map<String, Object>> explorersList = new ArrayList<>();
    private final List<Map<String, Object>> groupsList = new ArrayList<>();
    private ExplorerAdapter explorerAdapter;
    private GroupAdapter groupAdapter;
    private FirebaseAuth.AuthStateListener authListener;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    uploadProfileImage(result.getData().getData());
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialisation immédiate de l'UI selon l'utilisateur actuel
        updateUI(FirebaseAuth.getInstance().getCurrentUser());

        // Écouteur de changement d'état (pour réagir si l'utilisateur se connecte/déconnecte)
        authListener = firebaseAuth -> {
            if (binding != null) {
                updateUI(firebaseAuth.getCurrentUser());
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(authListener);

        setupExplorers();
        setupGroups();
        
        binding.cvProfile.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null || user.isAnonymous()) {
                Intent intent = new Intent(getActivity(), NotificationSettingsActivity.class);
                startActivity(intent);
                return;
            }

            // Menu de choix
            String[] options = {"Changer de photo", "Paramètres de notifications"};
            new AlertDialog.Builder(getContext())
                    .setTitle("Mon Profil")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            pickImageLauncher.launch(intent);
                        } else {
                            Intent intent = new Intent(getActivity(), NotificationSettingsActivity.class);
                            startActivity(intent);
                        }
                    })
                    .show();
        });
    }

    private void uploadProfileImage(android.net.Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Toast.makeText(getContext(), "Mise à jour de votre photo...", Toast.LENGTH_SHORT).show();
        String path = "profiles/" + user.getUid() + ".jpg";
        FirebaseStorage.getInstance().getReference(path).putFile(uri)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                            .update("profileImageUrl", downloadUri.toString())
                            .addOnSuccessListener(aVoid -> {
                                if (binding != null) {
                                    Glide.with(this).load(downloadUri).into(binding.ivProfile);
                                }
                                Toast.makeText(getContext(), "Photo de profil mise à jour !", Toast.LENGTH_SHORT).show();
                            });
                }))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur lors de l'upload", Toast.LENGTH_SHORT).show());
    }

    private void updateUI(FirebaseUser user) {
        if (binding == null) return;

        if (user != null && !user.isAnonymous()) {
            // Utilisateur CONNECTÉ RÉEL : On montre les fonctionnalités sociales
            binding.btnCreateGroup.setVisibility(View.VISIBLE);
            binding.btnCreateGroup.setOnClickListener(v -> showCreateGroupDialog());
            setupUserGreeting(user);
        } else {
            // Utilisateur ANONYME ou DÉCONNECTÉ : On cache les groupes
            binding.btnCreateGroup.setVisibility(View.GONE);
            binding.btnCreateGroup.setOnClickListener(null);
            binding.tvGreeting.setText("BIENVENUE, EXPLORATEUR");
        }
    }

    private void setupExplorers() {
        explorerAdapter = new ExplorerAdapter(explorersList);
        binding.rvExplorers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvExplorers.setAdapter(explorerAdapter);

        repository.getAllUsersQuery().get().addOnSuccessListener(queryDocumentSnapshots -> {
            explorersList.clear();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (currentUser != null && doc.getId().equals(currentUser.getUid())) continue;
                
                Map<String, Object> userData = doc.getData();
                userData.put("uid", doc.getId());
                explorersList.add(userData);
            }
            explorerAdapter.notifyDataSetChanged();
        });
    }

    private void setupGroups() {
        groupAdapter = new GroupAdapter(groupsList);
        binding.rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvGroups.setAdapter(groupAdapter);
        loadGroups();
    }

    private void loadGroups() {
        repository.getGroupsQuery().get().addOnSuccessListener(queryDocumentSnapshots -> {
            groupsList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Map<String, Object> group = doc.getData();
                group.put("id", doc.getId());
                groupsList.add(group);
            }
            groupAdapter.notifyDataSetChanged();
        });
    }

    private void setupUserGreeting(FirebaseUser user) {
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (binding != null && documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String imageUrl = documentSnapshot.getString("profileImageUrl");
                        
                        if (name != null) {
                            binding.tvGreeting.setText("RAVI DE VOUS REVOIR, " + name.toUpperCase());
                        }
                        if (imageUrl != null) {
                            Glide.with(this).load(imageUrl).into(binding.ivProfile);
                        }
                    }
                });
    }

    private void showCreateGroupDialog() {
        if (getActivity() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Créer un nouveau groupe");
        
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(64, 32, 64, 32);

        final EditText etName = new EditText(getActivity());
        etName.setHint("Nom du groupe");
        layout.addView(etName);

        final EditText etDesc = new EditText(getActivity());
        etDesc.setHint("Description (optionnel)");
        etDesc.setPadding(0, 32, 0, 0);
        layout.addView(etDesc);

        builder.setView(layout);

        builder.setPositiveButton("Créer", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            
            if (name.isEmpty()) {
                Toast.makeText(getActivity(), "Le nom est obligatoire", Toast.LENGTH_SHORT).show();
                return;
            }

            repository.createGroup(name, desc, 
                aVoid -> {
                    Toast.makeText(getActivity(), "Groupe créé !", Toast.LENGTH_SHORT).show();
                    loadGroups();
                },
                e -> Toast.makeText(getActivity(), "Erreur lors de la création", Toast.LENGTH_LONG).show()
            );
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (authListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
        }
        binding = null;
    }
}
