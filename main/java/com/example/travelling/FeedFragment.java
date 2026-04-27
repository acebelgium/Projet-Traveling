package com.example.travelling;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.travelling.databinding.FragmentFeedBinding;

import java.util.List;

public class FeedFragment extends Fragment {

    private FragmentFeedBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Données provenant du Repository
        List<TravelPhoto> photos = MockRepository.getFeedPhotos();

        FeedAdapter adapter = new FeedAdapter(photos);
        binding.rvFeed.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        binding.rvFeed.setAdapter(adapter);

        binding.cvSearch.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SearchActivity.class));
        });

        binding.etSearchMock.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SearchActivity.class));
        });
    }
}
