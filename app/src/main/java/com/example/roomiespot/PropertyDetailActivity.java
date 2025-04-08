package com.example.roomiespot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomiespot.adapters.AmenityAdapter;
import com.example.roomiespot.adapters.ImageSliderAdapter;
import com.example.roomiespot.models.Property;
import com.example.roomiespot.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PropertyDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerViewImages;
    private RecyclerView recyclerViewAmenities;
    private TextView tvTitle, tvPrice, tvAddress, tvDescription;
    private TextView tvBedrooms, tvBathrooms, tvSquareFeet;
    private TextView tvPetsAllowed, tvFurnished;
    private Button btnContact;
    private FloatingActionButton fabFavorite;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String propertyId;
    private Property property;
    private User propertyOwner;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_detail);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            // User not logged in, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Get property ID from intent
        propertyId = getIntent().getStringExtra("PROPERTY_ID");
        if (propertyId == null) {
            Toast.makeText(this, "Property not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Load property data
        loadPropertyData();

        // Check if property is in favorites
        checkFavoriteStatus();

        // Setup click listeners
        fabFavorite.setOnClickListener(v -> toggleFavorite());
        btnContact.setOnClickListener(v -> contactOwner());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewImages = findViewById(R.id.recycler_view_images);
        recyclerViewAmenities = findViewById(R.id.recycler_view_amenities);
        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvAddress = findViewById(R.id.tv_address);
        tvDescription = findViewById(R.id.tv_description);
        tvBedrooms = findViewById(R.id.tv_bedrooms);
        tvBathrooms = findViewById(R.id.tv_bathrooms);
        tvSquareFeet = findViewById(R.id.tv_square_feet);
        tvPetsAllowed = findViewById(R.id.tv_pets_allowed);
        tvFurnished = findViewById(R.id.tv_furnished);
        btnContact = findViewById(R.id.btn_contact);
        fabFavorite = findViewById(R.id.fab_favorite);
    }

    private void loadPropertyData() {
        db.collection("properties").document(propertyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        property = documentSnapshot.toObject(Property.class);
                        property.setId(documentSnapshot.getId());
                        
                        // Load property owner data
                        loadOwnerData(property.getOwnerId());
                        
                        // Update UI with property data
                        updateUI();
                    } else {
                        Toast.makeText(PropertyDetailActivity.this, "Property not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PropertyDetailActivity.this, "Error loading property: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadOwnerData(String ownerId) {
        db.collection("users").document(ownerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        propertyOwner = documentSnapshot.toObject(User.class);
                        propertyOwner.setUserId(documentSnapshot.getId());
                    }
                });
    }

    private void updateUI() {
        // Set property title
        tvTitle.setText(property.getTitle());
        
        // Set property price
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        tvPrice.setText(format.format(property.getPrice()) + "/month");
        
        // Set property address
        tvAddress.setText(property.getFullAddress());
        
        // Set property description
        tvDescription.setText(property.getDescription());
        
        // Set property details
        tvBedrooms.setText(String.valueOf(property.getBedrooms()));
        tvBathrooms.setText(String.valueOf(property.getBathrooms()));
        tvSquareFeet.setText(String.valueOf((int) property.getSquareFeet()));
        
        // Set pets allowed and furnished status
        tvPetsAllowed.setText(property.isPetsAllowed() ? "Yes" : "No");
        tvFurnished.setText(property.isFurnished() ? "Yes" : "No");
        
        // Setup image slider
        if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
            setupImageSlider();
        }
        
        // Setup amenities
        if (property.getAmenities() != null && !property.getAmenities().isEmpty()) {
            AmenityAdapter amenityAdapter = new AmenityAdapter(this, property.getAmenities());
            recyclerViewAmenities.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recyclerViewAmenities.setAdapter(amenityAdapter);
        }
        
        // Hide contact button if current user is the owner
        if (mAuth.getCurrentUser().getUid().equals(property.getOwnerId())) {
            btnContact.setVisibility(View.GONE);
        }
    }

    private void checkFavoriteStatus() {
        String userId = mAuth.getCurrentUser().getUid();
        
        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("propertyId", propertyId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    isFavorite = !queryDocumentSnapshots.isEmpty();
                    updateFavoriteIcon();
                });
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            fabFavorite.setImageResource(R.drawable.ic_favorite);
        }
    }

    private void toggleFavorite() {
        String userId = mAuth.getCurrentUser().getUid();
        
        if (isFavorite) {
            // Remove from favorites
            db.collection("favorites")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("propertyId", propertyId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // Fix: Get document reference from QueryDocumentSnapshot
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete();
                        }
                        isFavorite = false;
                        updateFavoriteIcon();
                        Toast.makeText(PropertyDetailActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Add to favorites
            Map<String, Object> favorite = new HashMap<>();
            favorite.put("userId", userId);
            favorite.put("propertyId", propertyId);
            favorite.put("timestamp", System.currentTimeMillis());
            
            db.collection("favorites")
                    .add(favorite)
                    .addOnSuccessListener(documentReference -> {
                        isFavorite = true;
                        updateFavoriteIcon();
                        Toast.makeText(PropertyDetailActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PropertyDetailActivity.this, "Error adding to favorites: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void contactOwner() {
        if (propertyOwner == null) {
            Toast.makeText(this, "Owner information not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Start chat activity with the property owner
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("RECEIVER_ID", propertyOwner.getUserId());
        intent.putExtra("RECEIVER_NAME", propertyOwner.getFullName());  // Changed from getName() to getFullName()
        intent.putExtra("PROPERTY_ID", propertyId);
        startActivity(intent);
    }

    private void setupImageSlider() {
        // Remove duplicate findViewById since we already have recyclerViewImages as a class field
        recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        
        List<String> imageUrls = property.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            ImageSliderAdapter imageAdapter = new ImageSliderAdapter(this, imageUrls);
            recyclerViewImages.setAdapter(imageAdapter);
            
            PagerSnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(recyclerViewImages);
        }
    }
}