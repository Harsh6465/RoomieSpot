package com.example.roomiespot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddPropertyActivity extends AppCompatActivity {
    private EditText etTitle, etDescription, etPrice, etLocation;
    private Button btnAddImages, btnSubmit;
    private ImageView ivPreview;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private String propertyType;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        propertyType = getIntent().getStringExtra("property_type");
        initializeViews();
        setupToolbar();
        setupListeners();
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        etLocation = findViewById(R.id.et_location);
        btnAddImages = findViewById(R.id.btn_add_images);
        btnSubmit = findViewById(R.id.btn_submit);
        ivPreview = findViewById(R.id.iv_preview);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add " + 
            (propertyType.equals("room") ? "Room" : "Apartment"));
    }

    private void setupListeners() {
        btnAddImages.setOnClickListener(v -> pickImages());
        btnSubmit.setOnClickListener(v -> submitProperty());
    }

    private void pickImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), 1);
    }

    private void submitProperty() {
        // Implementation for submitting property
        // This will handle both local storage and Firebase upload
        // based on your previous requirements
    }
}