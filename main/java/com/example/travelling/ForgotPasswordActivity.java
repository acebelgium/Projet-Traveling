package com.example.travelling;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.travelling.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSendLink.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                Toast.makeText(this, "Lien de réinitialisation envoyé à " + email, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Veuillez entrer votre email", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
