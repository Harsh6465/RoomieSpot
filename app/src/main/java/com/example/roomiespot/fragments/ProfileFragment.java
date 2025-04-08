package com.example.roomiespot.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.roomiespot.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView userName, userEmail, userPhone;
    private Button editProfileButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        loadUserProfile();
        setupListeners();
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profile_image);
        userName = view.findViewById(R.id.text_username);
        userEmail = view.findViewById(R.id.text_email);
        userPhone = view.findViewById(R.id.text_phone);
        editProfileButton = view.findViewById(R.id.button_edit_profile);
    }

    private void loadUserProfile() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");
                        String imageUrl = documentSnapshot.getString("profileImage");

                        userName.setText(name);
                        userEmail.setText(email);
                        userPhone.setText(phone);

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_person)
                                    .into(profileImage);
                        }
                    }
                });
    }

    private void setupListeners() {
        editProfileButton.setOnClickListener(v -> {
            // TODO: Implement edit profile functionality
        });
    }
}