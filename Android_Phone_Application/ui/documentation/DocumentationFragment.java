package com.example.blue2_1.ui.documentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.blue2_1.databinding.FragmentDocumentationBinding;

public class DocumentationFragment extends Fragment {

    private DocumentationViewModel documentationViewModel;
    private FragmentDocumentationBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        documentationViewModel =
                new ViewModelProvider(this).get(DocumentationViewModel.class);

        binding = FragmentDocumentationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDocumentation;
        documentationViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
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