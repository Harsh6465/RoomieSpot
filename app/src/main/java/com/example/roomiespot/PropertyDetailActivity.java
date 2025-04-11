package com.example.roomiespot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomiespot.adapters.PropertyImageAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PropertyDetailActivity extends AppCompatActivity {
    private static final String TAG = "PropertyDetailActivity";
    private static final String FAVORITES_COLLECTION = "user_favorites";

    // UI Components
    private TextView tvTitle, tvPrice, tvAddress, tvDescription, tvLandlordContact;
    private RecyclerView recyclerViewImages;
    private PropertyImageAdapter imageAdapter;
    private FloatingActionButton fabFavorite;
    private Button btnContactLandlord;

    // Firebase Components
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Property Details
    private String propertyId;
    private String landlordId;
    private String landlordPhoneNumber;
    private String landlordEmail;
    private boolean isFavorited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_detail);

        // Initialize Firebase
        initializeFirebase();

        // Initialize Views
        initializeViews();

        // Get Property Details from Intent
        propertyId = getIntent().getStringExtra("PROPERTY_ID");

        // Setup RecyclerView
        setupRecyclerView();

        // Fetch Property Details
        fetchPropertyDetails();

        // Setup Favorite Button
        setupFavoriteButton();

        // Setup Contact Landlord Button
        setupContactLandlordButton();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvAddress = findViewById(R.id.tv_address);
        tvDescription = findViewById(R.id.tv_description);
        tvLandlordContact = findViewById(R.id.tv_landlord_contact);
        recyclerViewImages = findViewById(R.id.recycler_view_images);
        btnContactLandlord = findViewById(R.id.btn_contact_landlord);
        fabFavorite = findViewById(R.id.fab_favorite);
    }

    private void setupRecyclerView() {
        recyclerViewImages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
    }

    private void fetchPropertyDetails() {
        db.collection("properties").document(propertyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updatePropertyUI(documentSnapshot);
                        loadPropertyImages(documentSnapshot);
                        checkFavoriteStatus();
                    } else {
                        handlePropertyNotFound();
                    }
                })
                .addOnFailureListener(this::handleFirestoreError);
    }

    private void updatePropertyUI(DocumentSnapshot documentSnapshot) {
        String title = documentSnapshot.getString("title");
        double price = documentSnapshot.getDouble("price");
        String address = documentSnapshot.getString("location");
        String description = documentSnapshot.getString("description");

        // Landlord details
        landlordPhoneNumber = documentSnapshot.getString("landlordPhoneNumber");
        landlordEmail = documentSnapshot.getString("landlordEmail");

        // Set UI elements
        tvTitle.setText(title);
        tvPrice.setText(String.format("$%.2f", price));
        tvAddress.setText(address);
        tvDescription.setText(description);

        // Set landlord contact
        String landlordContact = (landlordPhoneNumber != null ? landlordPhoneNumber : "Not Available");
        tvLandlordContact.setText(landlordContact);
    }

    private void loadPropertyImages(DocumentSnapshot documentSnapshot) {
        List<String> localImagePaths = (List<String>) documentSnapshot.get("localImagePaths");
        List<String> remoteImageUrls = (List<String>) documentSnapshot.get("imageUrls");

        List<String> combinedImagePaths = new ArrayList<>();
        if (localImagePaths != null) {
            combinedImagePaths.addAll(localImagePaths);
        }
        if (remoteImageUrls != null) {
            combinedImagePaths.addAll(remoteImageUrls);
        }

        loadLocalImages(combinedImagePaths);
    }

    private void loadLocalImages(List<String> imagePaths) {
        if (imagePaths == null || imagePaths.isEmpty()) {
            recyclerViewImages.setVisibility(View.GONE);
            return;
        }

        List<String> validImagePaths = imagePaths.stream()
                .filter(this::isValidImagePath)
                .collect(Collectors.toList());

        if (validImagePaths.isEmpty()) {
            recyclerViewImages.setVisibility(View.GONE);
            return;
        }

        recyclerViewImages.setVisibility(View.VISIBLE);
        imageAdapter = new PropertyImageAdapter(this, validImagePaths);
        recyclerViewImages.setAdapter(imageAdapter);
    }

    private boolean isValidImagePath(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }

        // Check if it's a local file path
        if (imagePath.startsWith("/") || imagePath.startsWith("file://")) {
            File file = new File(imagePath.replace("file://", ""));
            return file.exists() && file.length() > 0;
        }

        // Check if it's a valid URL
        return imagePath.startsWith("http://") || imagePath.startsWith("https://");
    }

    private void setupFavoriteButton() {
        fabFavorite.setOnClickListener(v -> {
            // Prevent multiple clicks
            fabFavorite.setEnabled(false);

            // Check user authentication
            if (currentUser == null) {
                showLoginPrompt();
                fabFavorite.setEnabled(true);
                return;
            }

            // Toggle favorite status
            toggleFavoriteStatus();
        });
    }

    private void toggleFavoriteStatus() {
        String userId = currentUser.getUid();
        DocumentReference favoriteRef = db.collection(FAVORITES_COLLECTION)
                .document(userId)
                .collection("properties")
                .document(propertyId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(favoriteRef);

            if (snapshot.exists()) {
                // Remove from favorites
                transaction.delete(favoriteRef);
                return false;
            } else {
                // Add to favorites
                Map<String, Object> favoriteData = new HashMap<>();
                favoriteData.put("propertyId", propertyId);
                favoriteData.put("addedAt", FieldValue.serverTimestamp());
                favoriteData.put("title", tvTitle.getText().toString());

                transaction.set(favoriteRef, favoriteData);
                return true;
            }
        }).addOnSuccessListener(isFavoriteAdded -> {
            // Update UI
            isFavorited = isFavoriteAdded;
            updateFavoriteIcon();

            // Show appropriate toast
            String message = isFavoriteAdded
                    ? "Added to favorites"
                    : "Removed from favorites";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            fabFavorite.setEnabled(true);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Favorite toggle failed", e);
            handleFirestoreError(e);
            fabFavorite.setEnabled(true);
        });
    }

    private void checkFavoriteStatus() {
        if (currentUser == null) {
            updateFavoriteIcon();
            return;
        }

        db.collection(FAVORITES_COLLECTION)
                .document(currentUser.getUid())
                .collection("properties")
                .document(propertyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isFavorited = documentSnapshot.exists();
                    updateFavoriteIcon();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking favorite status", e);
                    isFavorited = false;
                    updateFavoriteIcon();
                    handleFirestoreError(e);
                });
    }

    private void updateFavoriteIcon() {
        runOnUiThread(() -> {
            int iconResId = isFavorited
                    ? R.drawable.ic_favorite_filled
                    : R.drawable.ic_favorite;
            fabFavorite.setImageResource(iconResId);
        });
    }

    private void setupContactLandlordButton() {
        btnContactLandlord.setOnClickListener(v -> {
            // Check if landlord contact information exists
            if (landlordPhoneNumber != null && !landlordPhoneNumber.isEmpty()) {
                // Show dialog with contact options
                showContactOptionsDialog();
            } else if (landlordEmail != null && !landlordEmail.isEmpty()) {
                // If phone is not available, offer email option
                sendEmailToLandlord();
            } else {
                // No contact information available
                Toast.makeText(this, "No contact information available for this landlord", Toast.LENGTH_LONG).show();
                btnContactLandlord.setEnabled(false);
            }
        });
    }

    private void showContactOptionsDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Contact Landlord")
                .setItems(new String[]{"Call", "Send SMS", "Send Email"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Call
                            dialLandlordNumber();
                            break;
                        case 1: // SMS
                            sendSmsToLandlord();
                            break;
                        case 2: // Email
                            sendEmailToLandlord();
                            break;
                    }
                })
                .show();
    }

    private void dialLandlordNumber() {
        try {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + landlordPhoneNumber));
            startActivity(dialIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error launching dialer", e);
            Toast.makeText(this, "Could not open phone dialer", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSmsToLandlord() {
        try {
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + landlordPhoneNumber));
            startActivity(smsIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error launching SMS", e);
            Toast.makeText(this, "Could not open SMS app", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmailToLandlord() {
        if (landlordEmail == null || landlordEmail.isEmpty()) {
            Toast.makeText(this, "No email available for this landlord", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + landlordEmail));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry about Property: " + tvTitle.getText().toString());
            startActivity(emailIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error launching email", e);
            Toast.makeText(this, "Could not open email app", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoginPrompt() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Login Required")
                .setMessage("Please log in to add properties to favorites")
                .setPositiveButton("Login", (dialog, which) -> {
                    // Navigate to login activity
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    startActivity(loginIntent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handlePropertyNotFound() {
        Toast.makeText(this, "Property not found", Toast.LENGTH_LONG).show();
        finish(); // Close the activity
    }

    private void handleFirestoreError(Exception e) {
        if (e instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;

            switch (firestoreException.getCode()) {
                case PERMISSION_DENIED:
                    showPermissionDeniedDialog();
                    break;
                case UNAUTHENTICATED:
                    promptUserLogin();
                    break;
                default:
                    showGeneralErrorMessage(e);
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Access Denied")
                .setMessage("You don't have permission to perform this action.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void promptUserLogin() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Login Required")
                .setMessage("Please log in to continue")
                .setPositiveButton("Login", (dialog, which) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showGeneralErrorMessage(Exception e) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage("An error occurred: " + e.getMessage())
                .setPositiveButton("OK", null)
                .show();
    }
}