package com.example.roomiespot;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private EditText etFullName, etPhone, etBio;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etFullName = findViewById(R.id.et_full_name);
        etPhone = findViewById(R.id.et_phone);
        etBio = findViewById(R.id.et_bio);
        Button btnSave = findViewById(R.id.btn_save);

        loadUserData();
        btnSave.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etFullName.setText(documentSnapshot.getString("fullName"));
                        etPhone.setText(documentSnapshot.getString("phone"));
                        etBio.setText(documentSnapshot.getString("bio"));
                    }
                });
    }

    private void saveUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", etFullName.getText().toString().trim());
        updates.put("phone", etPhone.getText().toString().trim());
        updates.put("bio", etBio.getText().toString().trim());

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}