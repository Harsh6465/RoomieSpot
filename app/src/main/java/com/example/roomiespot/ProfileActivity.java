package com.example.roomiespot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {
    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private TextInputEditText etName, etEmail, etPhone;
    private ImageView profileImage;
    private Uri selectedImageUri;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Find views using correct IDs
        profileImage = findViewById(R.id.iv_profile_pic);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        TextInputEditText etBio = findViewById(R.id.et_bio);
        Switch switchLandlord = findViewById(R.id.switch_landlord);
        Button btnSaveProfile = findViewById(R.id.btn_save_profile);
        Button btnChangePassword = findViewById(R.id.btn_change_password);
        Button btnLogout = findViewById(R.id.btn_logout);

        // Setup image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        selectedImageUri = data.getData();
                        uploadProfileImage();
                    }
                }
            }
        );

        // Setup click listeners
        btnSaveProfile.setOnClickListener(v -> {
            // Handle save profile action
            Toast.makeText(this, "Saving profile...", Toast.LENGTH_SHORT).show();
        });

        // Implement logout functionality
        btnLogout.setOnClickListener(v -> {
            try {
                mAuth.signOut();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                
                // Redirect to login screen
                Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish();
            } catch (Exception e) {
                Log.e("ProfileActivity", "Logout error", e);
                Toast.makeText(this, "Error logging out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Add click listener to profile image to open media picker
        profileImage.setOnClickListener(v -> openImagePicker());

        // Load user data
        loadUserProfile();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Check image size
            long imageSize = getImageSize(selectedImageUri);
            if (imageSize > MAX_IMAGE_SIZE) {
                Toast.makeText(this, "Image too large. Max size is 5MB", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a reference to the profile image in Firebase Storage
            StorageReference profileImageRef = storageReference
                .child("profile_images")
                .child(currentUser.getUid() + ".jpg");

            // Upload image to Firebase Storage
            profileImageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Save URL to Firestore
                        String profileImageUrl = uri.toString();
                        db.collection("users").document(currentUser.getUid())
                            .update("profileImageUrl", profileImageUrl)
                            .addOnSuccessListener(aVoid -> {
                                // Update UI
                                Glide.with(this)
                                    .load(profileImageUrl)
                                    .circleCrop()
                                    .into(profileImage);
                                
                                Toast.makeText(this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } catch (Exception e) {
            Log.e("ProfileActivity", "Image upload error", e);
            Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
        }
    }

    private long getImageSize(Uri uri) {
        try {
            android.content.ContentResolver resolver = getContentResolver();
            android.database.Cursor cursor = resolver.query(uri, null, null, null, null);
            
            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                cursor.moveToFirst();
                long size = cursor.getLong(sizeIndex);
                cursor.close();
                return size;
            }
        } catch (Exception e) {
            Log.e("ProfileActivity", "Error getting image size", e);
        }
        return 0;
    }

    private void loadUserProfile() {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if (document.exists()) {
                            String fullName = document.getString("fullName");
                            String email = document.getString("email");
                            String phoneNumber = document.getString("phoneNumber");
                            String profileImageUrl = document.getString("profileImageUrl");

                            // Set text using correct EditText views
                            if (etName != null && fullName != null) {
                                etName.setText(fullName);
                            }
                            if (etEmail != null && email != null) {
                                etEmail.setText(email);
                            }
                            if (etPhone != null && phoneNumber != null) {
                                etPhone.setText(phoneNumber);
                            }

                            if (profileImageUrl != null && !profileImageUrl.isEmpty() && profileImage != null) {
                                Glide.with(ProfileActivity.this)
                                    .load(profileImageUrl)
                                    .circleCrop()
                                    .into(profileImage);
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "User profile not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
        } catch (Exception e) {
            Toast.makeText(this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}