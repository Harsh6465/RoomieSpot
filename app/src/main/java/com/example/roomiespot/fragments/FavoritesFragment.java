package com.example.roomiespot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomiespot.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    public static FavoritesFragment newInstance() {
        return new FavoritesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        recyclerView = view.findViewById(R.id.recycler_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        loadFavorites();
    }

    private void loadFavorites() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    // TODO: Implement favorite properties loading
                });
    }
}