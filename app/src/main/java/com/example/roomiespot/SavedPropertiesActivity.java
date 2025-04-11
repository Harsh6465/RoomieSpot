package com.example.roomiespot;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import com.example.roomiespot.adapters.PropertyAdapter;
import com.example.roomiespot.models.Property;
import com.example.roomiespot.PropertyDetailActivity;

public class SavedPropertiesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PropertyAdapter propertyAdapter;
    private List<Property> propertyList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_properties);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Saved Properties");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupViews();
        loadSavedProperties();
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        propertyList = new ArrayList<>();
        propertyAdapter = new PropertyAdapter(this, propertyList, new PropertyAdapter.OnPropertyClickListener() {
            @Override
            public void onPropertyClick(Property property) {
                // Open Property Detail Activity
                Intent intent = new Intent(SavedPropertiesActivity.this, PropertyDetailActivity.class);
                
                // Pass property details via intent
                intent.putExtra("PROPERTY_ID", property.getId());
                intent.putExtra("PROPERTY_TITLE", property.getTitle());
                intent.putExtra("PROPERTY_DESCRIPTION", property.getDescription());
                intent.putExtra("PROPERTY_PRICE", property.getPrice());
                intent.putExtra("PROPERTY_LOCATION", property.getLocation());
                intent.putExtra("PROPERTY_TYPE", property.getPropertyType());
                
                // Pass landlord contact details
                intent.putExtra("LANDLORD_PHONE", property.getLandlordPhoneNumber());
                intent.putExtra("LANDLORD_EMAIL", property.getLandlordEmail());
                
                // Pass image URLs if available
                if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
                    intent.putStringArrayListExtra("PROPERTY_IMAGES", new ArrayList<>(property.getImageUrls()));
                }

                startActivity(intent);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(propertyAdapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadSavedProperties);
    }

    private void loadSavedProperties() {
        String userId = mAuth.getCurrentUser().getUid();
        swipeRefreshLayout.setRefreshing(true);

        db.collection("users").document(userId)
                .collection("savedProperties")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    propertyList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String propertyId = document.getString("propertyId");
                        loadPropertyDetails(propertyId);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> swipeRefreshLayout.setRefreshing(false));
    }

    private void loadPropertyDetails(String propertyId) {
        db.collection("properties").document(propertyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Property property = documentSnapshot.toObject(Property.class);
                        property.setId(documentSnapshot.getId());
                        propertyList.add(property);
                        propertyAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}