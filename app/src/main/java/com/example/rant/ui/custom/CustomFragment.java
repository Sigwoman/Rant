package com.example.rant.ui.custom;

import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

import com.example.rant.databinding.FragmentCustomBinding;

public class CustomFragment extends Fragment {

    private CustomViewModel customViewModel;
    private FragmentCustomBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        customViewModel = new ViewModelProvider(this).get(CustomViewModel.class);

        binding = FragmentCustomBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textCustom;
        customViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}