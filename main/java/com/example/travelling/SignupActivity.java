package com.example.travelling;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.travelling.databinding.ActivitySignupBinding;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    // private FirebaseAuth mAuth;
    // private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // mAuth = FirebaseAuth.getInstance();
        // db = FirebaseFirestore.getInstance();

        binding.btnSignup.setOnClickListener(v -> signupUser());
        binding.tvLogin.setOnClickListener(v -> finish());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void signupUser() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mock Signup
        Toast.makeText(this, "Compte simulé créé pour " + name, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(SignupActivity.this, MainActivity.class));
        finishAffinity();

        /*
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        saveUserToFirestore(userId, name, email);
                    } else {
                        Toast.makeText(SignupActivity.this, "Échec de l'inscription: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
        */
    }

    /*
    private void saveUserToFirestore(String userId, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignupActivity.this, "Erreur lors de l'enregistrement des données", Toast.LENGTH_SHORT).show();
                });
    }
    */
}
