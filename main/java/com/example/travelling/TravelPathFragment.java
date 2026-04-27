package com.example.travelling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.travelling.databinding.FragmentTravelPathBinding;

public class TravelPathFragment extends Fragment {

    private FragmentTravelPathBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTravelPathBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
