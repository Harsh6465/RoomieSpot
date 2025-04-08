package com.example.roomiespot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomiespot.R;
import com.example.roomiespot.adapters.PropertyAdapter;
import com.example.roomiespot.models.Property;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PropertyAdapter propertyAdapter;
    private List<Property> propertyList;
    private FirebaseFirestore db;
    private AutoCompleteTextView locationDropdown;
    private ChipGroup propertyTypeChipGroup;
    private RangeSlider priceRangeSlider;
    private String selectedLocation = null;
    private String selectedPropertyType = null;
    private float minPrice = 0;
    private float maxPrice = 50000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view);
        locationDropdown = view.findViewById(R.id.location_dropdown);
        propertyTypeChipGroup = view.findViewById(R.id.property_type_chip_group);
        priceRangeSlider = view.findViewById(R.id.price_range_slider);
        
        // Setup RecyclerView
        propertyList = new ArrayList<>();
        propertyAdapter = new PropertyAdapter(getContext(), propertyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(propertyAdapter);
        
        // Setup location dropdown
        setupLocationDropdown();
        
        // Setup property type chips
        setupPropertyTypeChips();
        
        // Setup price range slider
        setupPriceRangeSlider();
        
        // Load properties
        loadProperties();
        
        return view;
    }
    
    private void setupLocationDropdown() {
        List<String> locations = Arrays.asList(
                "College Road", "Gangapur Road", "Satpur", "Panchavati", 
                "Ashok Stambh", "Nashik Road", "Dwarka", "All Locations"
        );
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(), R.layout.dropdown_item, locations);
        locationDropdown.setAdapter(adapter);
        
        locationDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String location = parent.getItemAtPosition(position).toString();
            if (location.equals("All Locations")) {
                selectedLocation = null;
            } else {
                selectedLocation = location;
            }
            loadProperties();
        });
    }
    
    private void setupPropertyTypeChips() {
        propertyTypeChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.size() > 0) {
                Chip chip = group.findViewById(checkedIds.get(0));
                selectedPropertyType = chip.getText().toString();
            } else {
                selectedPropertyType = null;
            }
            loadProperties();
        });
    }
    
    private void setupPriceRangeSlider() {
        priceRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            minPrice = values.get(0);
            maxPrice = values.get(1);
            loadProperties();
        });
    }
    
    private void loadProperties() {
        propertyList.clear();
        
        Query query = db.collection("properties")
                .whereEqualTo("status", "available");
        
        if (selectedLocation != null) {
            query = query.whereEqualTo("location", selectedLocation);
        }
        
        if (selectedPropertyType != null) {
            query = query.whereEqualTo("propertyType", selectedPropertyType);
        }
        
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Property property = document.toObject(Property.class);
                    property.setId(document.getId());
                    
                    // Filter by price
                    if (property.getPrice() >= minPrice && property.getPrice() <= maxPrice) {
                        propertyList.add(property);
                    }
                }
                propertyAdapter.notifyDataSetChanged();
                
                if (propertyList.isEmpty()) {
                    Toast.makeText(getContext(), "No properties found with the selected filters", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error loading properties: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}