package com.example.roomiespot;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.roomiespot.utils.ImageUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

public class AddPropertyActivity extends AppCompatActivity {
    private static final String TAG = "AddPropertyActivity";
    private EditText etTitle, etDescription, etPrice, etLocation, etLandlordName, etLandlordPhone, etLandlordEmail;
    private Button btnAddImages, btnSubmit;
    private ImageView ivPreview;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private String propertyType;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;

    private ActivityResultLauncher<String> storagePermissionLauncher;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        // Add null check and default value for propertyType
        propertyType = getIntent().getStringExtra("property_type");
        if (propertyType == null) {
            propertyType = "room"; // Default to room if not specified
        }

        initializeViews();
        setupToolbar();
        initializeLaunchers();
        setupListeners();
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        etLocation = findViewById(R.id.et_location);
        etLandlordName = findViewById(R.id.et_landlord_name);
        etLandlordPhone = findViewById(R.id.et_landlord_phone);
        etLandlordEmail = findViewById(R.id.et_landlord_email);
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
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            
            // Use a ternary operator to handle the title
            String title = "Add " + (propertyType.equals("room") ? "Room" : "Apartment");
            getSupportActionBar().setTitle(title);
        }
    }

    private void initializeLaunchers() {
        // Storage Permission Launcher
        storagePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), 
            isGranted -> {
                if (isGranted) {
                    // Permission granted, open image picker
                    openImagePicker();
                } else {
                    // Permission denied
                    showPermissionDeniedDialog();
                }
            }
        );

        // Image Picker Launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    handleImageSelection(result.getData());
                }
            }
        );
    }

    private void setupListeners() {
        btnAddImages.setOnClickListener(v -> checkAndRequestStoragePermission());
        btnSubmit.setOnClickListener(v -> submitProperty());
    }

    private void checkAndRequestStoragePermission() {
        // Check Android version for different permission handling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33) and above: Use new media permissions
            if (ContextCompat.checkSelfPermission(
                this, 
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED) {
                // Request media permissions for Android 13+
                requestMediaPermissions();
            } else {
                // Permission already granted
                openImagePicker();
            }
        } else {
            // For older Android versions, use traditional storage permissions
            if (ContextCompat.checkSelfPermission(
                this, 
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
                // Request traditional storage permissions
                requestLegacyStoragePermissions();
            } else {
                // Permission already granted
                openImagePicker();
            }
        }
    }

    private void requestMediaPermissions() {
        // Request media permissions for Android 13+
        ActivityCompat.requestPermissions(this, 
            new String[]{
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            }, 
            PERMISSION_REQUEST_CODE);
    }

    private void requestLegacyStoragePermissions() {
        // Request legacy storage permissions for older Android versions
        ActivityCompat.requestPermissions(this, 
            new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 
            PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Permissions granted, open image picker
                openImagePicker();
            } else {
                // Show detailed permission denied dialog
                new MaterialAlertDialogBuilder(this)
                    .setTitle("Media Access Permission")
                    .setMessage("This app needs media access to select property images. Please grant media permission in app settings.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        // Open app-specific settings
                        Intent intent = new Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null)
                        );
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> 
                        Toast.makeText(this, 
                            "Media permission is required to select images", 
                            Toast.LENGTH_SHORT).show())
                    .setCancelable(false)
                    .show();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Media Access Permission")
            .setMessage("This app needs media access to select property images. Please grant media permission in app settings.")
            .setPositiveButton("Open Settings", (dialog, which) -> {
                // Open app-specific settings
                Intent intent = new Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", getPackageName(), null)
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", (dialog, which) -> 
                Toast.makeText(this, 
                    "Media permission is required to select images", 
                    Toast.LENGTH_SHORT).show())
            .setCancelable(false)
            .show();
    }

    private void openImagePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, 
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(
                Intent.createChooser(intent, "Select Property Images")
            );
        } catch (Exception e) {
            Log.e("AddPropertyActivity", "Error opening image picker", e);
            Toast.makeText(this, "Unable to open image picker", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImageSelection(Intent data) {
        // Clear previous selections
        imageUris.clear();

        if (data == null) {
            Log.e(TAG, "Image selection intent is null");
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Handle multiple image selection
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                Log.d(TAG, "Multiple images selected: " + count);
                
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    if (isValidImageUri(imageUri)) {
                        imageUris.add(imageUri);
                        Log.d(TAG, "Added image URI: " + imageUri);
                    } else {
                        Log.w(TAG, "Invalid image URI: " + imageUri);
                    }
                }
            } 
            // Handle single image selection
            else if (data.getData() != null) {
                Uri imageUri = data.getData();
                if (isValidImageUri(imageUri)) {
                    imageUris.add(imageUri);
                    Log.d(TAG, "Single image added: " + imageUri);
                } else {
                    Log.w(TAG, "Invalid single image URI: " + imageUri);
                }
            }

            // Update UI with selected images
            if (!imageUris.isEmpty()) {
                // Show first image in preview
                ivPreview.setImageURI(imageUris.get(0));
                
                // Show number of selected images
                Toast.makeText(this, 
                    imageUris.size() + " image(s) selected", 
                    Toast.LENGTH_SHORT).show();
            } else {
                Log.w(TAG, "No valid images selected");
                Toast.makeText(this, "No valid images selected", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling image selection", e);
            Toast.makeText(this, "Error selecting images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to validate image URI
    private boolean isValidImageUri(Uri uri) {
        if (uri == null) return false;

        try {
            // Check if we can open an input stream from the URI
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);
            
            if (inputStream == null) {
                Log.w(TAG, "Unable to open input stream for URI: " + uri);
                return false;
            }

            // Check file size (optional, limit to 10MB)
            long fileSize = inputStream.available();
            inputStream.close();

            if (fileSize > 10 * 1024 * 1024) {  // 10MB limit
                Log.w(TAG, "Image too large: " + fileSize + " bytes");
                return false;
            }

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error validating image URI", e);
            return false;
        }
    }

    private List<String> saveLocalPropertyImages() {
        List<String> localImagePaths = new ArrayList<>();
        
        for (Uri imageUri : imageUris) {
            try {
                // Compress image before saving
                Bitmap compressedBitmap = ImageUtils.getCompressedBitmap(this, imageUri, 1024);
                
                // Save compressed bitmap to local storage
                String localImagePath = saveCompressedBitmapToLocal(compressedBitmap);
                
                if (localImagePath != null) {
                    localImagePaths.add(localImagePath);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error saving local image", e);
                Toast.makeText(this, "Failed to save image locally", Toast.LENGTH_SHORT).show();
            }
        }
        
        return localImagePaths;
    }

    private String saveCompressedBitmapToLocal(Bitmap bitmap) {
        try {
            // Create directory for local images
            java.io.File directory = new java.io.File(getFilesDir(), "property_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Generate unique filename
            java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault());
            String timeStamp = simpleDateFormat.format(new java.util.Date());
            String fileName = "PROP_" + timeStamp + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
            java.io.File outputFile = new java.io.File(directory, fileName);
            
            // Save bitmap to file
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(outputFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            }
            
            return outputFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error saving compressed bitmap", e);
            return null;
        }
    }

    private void submitProperty() {
        // Validate input fields
        if (!validateInputs()) {
            return;
        }

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Property");
        progressDialog.setMessage("Please wait while we upload your property...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Save local images first
        List<String> localImagePaths = saveLocalPropertyImages();

        // Upload images to Firebase Storage (optional)
        uploadPropertyImages(new OnImagesUploadedCallback() {
            @Override
            public void onImagesUploaded(List<String> imageUrls) {
                // Create property document
                Map<String, Object> propertyData = new HashMap<>();
                propertyData.put("title", etTitle.getText().toString().trim());
                propertyData.put("description", etDescription.getText().toString().trim());
                propertyData.put("price", Double.parseDouble(etPrice.getText().toString().trim()));
                propertyData.put("location", etLocation.getText().toString().trim());
                propertyData.put("propertyType", propertyType);
                propertyData.put("userId", mAuth.getCurrentUser().getUid());

                // Landlord details
                propertyData.put("landlordName", etLandlordName.getText().toString().trim());
                propertyData.put("landlordPhoneNumber", etLandlordPhone.getText().toString().trim());
                propertyData.put("landlordEmail", etLandlordEmail.getText().toString().trim());

                // Store both local and remote image paths
                propertyData.put("localImagePaths", localImagePaths);
                propertyData.put("imageUrls", imageUrls);

                // Add to Firestore
                db.collection("properties")
                    .add(propertyData)
                    .addOnSuccessListener(documentReference -> {
                        progressDialog.dismiss();
                        Toast.makeText(AddPropertyActivity.this, "Property added successfully", Toast.LENGTH_SHORT).show();
                        
                        // Broadcast property added event
                        Intent broadcastIntent = new Intent("com.example.roomiespot.PROPERTY_ADDED");
                        sendBroadcast(broadcastIntent);
                        
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Log.e(TAG, "Error adding property", e);
                        Toast.makeText(AddPropertyActivity.this, 
                            "Failed to add property: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
            }

            @Override
            public void onUploadFailed(String error) {
                progressDialog.dismiss();
                Toast.makeText(AddPropertyActivity.this, 
                    "Image upload failed: " + error, 
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate title
        if (etTitle.getText().toString().trim().isEmpty()) {
            etTitle.setError("Title is required");
            isValid = false;
        }

        // Validate price
        try {
            double price = Double.parseDouble(etPrice.getText().toString().trim());
            if (price <= 0) {
                etPrice.setError("Price must be greater than 0");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price");
            isValid = false;
        }

        // Validate location
        if (etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setError("Location is required");
            isValid = false;
        }

        // Validate images
        if (imageUris.isEmpty()) {
            Toast.makeText(this, "Please add at least one property image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void uploadPropertyImages(OnImagesUploadedCallback callback) {
        // Validate input fields
        if (!validateInputs()) {
            Log.e(TAG, "Input validation failed");
            showDetailedErrorToast("Input validation failed. Please check all fields.");
            callback.onUploadFailed("Input validation failed");
            return;
        }

        // Check if images exist
        if (imageUris == null || imageUris.isEmpty()) {
            Log.e(TAG, "No images selected for upload");
            showDetailedErrorToast("No images selected. Please add at least one image.");
            callback.onUploadFailed("No images selected");
            return;
        }

        // Check storage and network connectivity
        // Removed network connectivity check

        // Limit number of images
        if (imageUris.size() > 5) {
            showDetailedErrorToast("Maximum 5 images allowed. Please remove some images.");
            callback.onUploadFailed("Maximum 5 images allowed");
            return;
        }

        List<String> uploadedImageUrls = new ArrayList<>();
        AtomicInteger uploadCounter = new AtomicInteger(0);
        AtomicBoolean uploadFailed = new AtomicBoolean(false);

        // Create a progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Images");
        progressDialog.setMessage("Please wait while images are being uploaded...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        for (Uri imageUri : imageUris) {
            try {
                // Comprehensive image processing
                Bitmap compressedBitmap = null;
                byte[] compressedImageBytes = null;

                try {
                    compressedBitmap = ImageUtils.getCompressedBitmap(this, imageUri, 1024);
                    compressedImageBytes = ImageUtils.bitmapToByteArray(compressedBitmap);
                    
                    // Log compression details
                    Log.d(TAG, "Image Compression:");
                    Log.d(TAG, "  Compressed Bitmap Width: " + compressedBitmap.getWidth());
                    Log.d(TAG, "  Compressed Bitmap Height: " + compressedBitmap.getHeight());
                    Log.d(TAG, "  Compressed Bytes Length: " + compressedImageBytes.length);
                } catch (IOException e) {
                    Log.e(TAG, "Image compression failed", e);
                    callback.onUploadFailed("Image compression error: " + e.getMessage());
                    progressDialog.dismiss();
                    return;
                }

                // Generate unique storage reference
                String filename = "property_image_" + System.currentTimeMillis() + "_" + 
                                  UUID.randomUUID().toString().substring(0, 8) + ".jpg";
                StorageReference imageRef = storage.getReference()
                    .child("property_images")
                    .child(mAuth.getCurrentUser().getUid())
                    .child(filename);

                // Upload compressed image bytes
                imageRef.putBytes(compressedImageBytes)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            synchronized (uploadedImageUrls) {
                                uploadedImageUrls.add(uri.toString());
                                Log.d(TAG, "Image uploaded successfully: " + uri);
                            }
                            
                            // Check if all images are uploaded
                            if (uploadCounter.incrementAndGet() == imageUris.size() && !uploadFailed.get()) {
                                progressDialog.dismiss();
                                callback.onImagesUploaded(uploadedImageUrls);
                            }
                        }).addOnFailureListener(e -> {
                            handleUploadFailure(e, uploadFailed, callback);
                            progressDialog.dismiss();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Image upload failed", e);
                        handleUploadFailure(e, uploadFailed, callback);
                        progressDialog.dismiss();
                    });

            } catch (Exception e) {
                Log.e(TAG, "Unexpected error during image upload", e);
                callback.onUploadFailed("Unexpected error: " + e.getMessage());
                progressDialog.dismiss();
                return;
            }
        }
    }

    private void handleUploadFailure(Exception e, AtomicBoolean uploadFailed, OnImagesUploadedCallback callback) {
        if (uploadFailed.compareAndSet(false, true)) {
            Log.e(TAG, "Image upload failed", e);
            callback.onUploadFailed("Upload failed: " + e.getMessage());
        }
    }

    // Helper method to get real file path from URI
    private String getRealPathFromURI(Uri contentUri) {
        // Multiple strategies to get the real path
        String[] pathStrategies = {
            contentUri.getPath(),
            getFilePathFromContentResolver(contentUri),
            getFilePathFromMediaStore(contentUri)
        };

        for (String path : pathStrategies) {
            if (path != null && !path.isEmpty()) {
                java.io.File file = new java.io.File(path);
                if (file.exists() && file.canRead()) {
                    return path;
                }
            }
        }

        // Fallback
        Log.e(TAG, "Could not resolve real path for URI: " + contentUri);
        return contentUri.toString();
    }

    // Get file path using ContentResolver
    private String getFilePathFromContentResolver(Uri uri) {
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            android.database.Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
            
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(column_index);
                cursor.close();
                return path;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting path from ContentResolver", e);
        }
        return null;
    }

    // Get file path from MediaStore
    private String getFilePathFromMediaStore(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        android.database.Cursor cursor = managedQuery(uri, projection, null, null, null);
        
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return null;
    }

    // New method to show detailed error toast
    private void showDetailedErrorToast(String message) {
        Toast.makeText(this, "Upload Error: " + message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Upload Error: " + message);
    }

    // Callback interface for image upload
    private interface OnImagesUploadedCallback {
        void onImagesUploaded(List<String> imageUrls);
        void onUploadFailed(String error);
    }
}