package com.example.travelling;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.travelling.databinding.ActivityNotificationSettingsBinding;

public class NotificationSettingsActivity extends AppCompatActivity {
    private ActivityNotificationSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
    }
}
