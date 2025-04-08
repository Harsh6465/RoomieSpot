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

public class FavouriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public FavouriteFragment() {
        // Required empty public constructor
    }

    public static FavouriteFragment newInstance() {
        return new FavouriteFragment();
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
        return inflater.inflate(R.layout.fragment_favourite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        recyclerView = view.findViewById(R.id.recycler_favourites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        loadFavourites();
    }

    private void loadFavourites() {
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