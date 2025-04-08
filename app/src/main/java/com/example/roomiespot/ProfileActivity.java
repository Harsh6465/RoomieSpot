package com.example.roomiespot;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {
    private ImageView profileImage;
    private TextView tvFullName, tvEmail;
    private Button btnEditProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views with correct IDs
        profileImage = findViewById(R.id.iv_profile_pic);
        TextInputEditText etName = findViewById(R.id.et_name);
        TextInputEditText etEmail = findViewById(R.id.et_email);
        TextInputEditText etPhone = findViewById(R.id.et_phone);
        TextInputEditText etBio = findViewById(R.id.et_bio);
        Switch switchLandlord = findViewById(R.id.switch_landlord);
        Button btnSaveProfile = findViewById(R.id.btn_save_profile);
        Button btnChangePassword = findViewById(R.id.btn_change_password);
        Button btnLogout = findViewById(R.id.btn_logout);

        // Load user data
        loadUserProfile();

        // Setup click listeners
        btnSaveProfile.setOnClickListener(v -> {
            // Handle save profile action
            Toast.makeText(this, "Saving profile...", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    String fullName = document.getString("fullName");
                    String email = document.getString("email");
                    String profileImageUrl = document.getString("profileImageUrl");

                    tvFullName.setText(fullName);
                    tvEmail.setText(email);

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .circleCrop()
                            .into(profileImage);
                    }
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}