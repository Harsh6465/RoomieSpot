package com.example.roomiespot.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

public class StorageManager {
    
    private static final int MAX_IMAGE_SIZE = 500; // Max dimension in pixels
    private static final int COMPRESSION_QUALITY = 70; // JPEG compression quality (0-100)
    
    // Delete property images
    public static void deletePropertyImages(List<String> imageUrls) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        
        for (String imageUrl : imageUrls) {
            // Get reference from URL and delete
            StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
            imageRef.delete().addOnSuccessListener(aVoid -> {
                // Image deleted successfully
            }).addOnFailureListener(e -> {
                // Failed to delete image
            });
        }
    }
    
    // Upload a compressed image and get download URL
    public static Task<Uri> uploadCompressedImage(Context context, Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String imageName = UUID.randomUUID().toString();
        StorageReference imageRef = storage.getReference().child("property_images/" + imageName);
        
        try {
            // Compress the image
            Bitmap bitmap = ImageUtils.getCompressedBitmap(context, imageUri, MAX_IMAGE_SIZE);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, baos);
            byte[] data = baos.toByteArray();
            
            // Upload compressed image
            UploadTask uploadTask = imageRef.putBytes(data);
            return uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    throw task.getException();
                }
                return imageRef.getDownloadUrl();
            });
        } catch (Exception e) {
            Toast.makeText(context, "Image compression failed", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    
    // Get storage usage statistics
    public static void getStorageUsage(StorageUsageCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference rootRef = storage.getReference();
        
        rootRef.listAll().addOnSuccessListener(listResult -> {
            long[] totalSize = {0};
            int[] fileCount = {0};
            int[] completedChecks = {0};
            
            if (listResult.getItems().size() == 0) {
                callback.onUsageCalculated(0, 0);
                return;
            }
            
            for (StorageReference item : listResult.getItems()) {
                item.getMetadata().addOnSuccessListener(metadata -> {
                    totalSize[0] += metadata.getSizeBytes();
                    fileCount[0]++;
                    completedChecks[0]++;
                    
                    if (completedChecks[0] == listResult.getItems().size()) {
                        callback.onUsageCalculated(totalSize[0], fileCount[0]);
                    }
                }).addOnFailureListener(e -> {
                    completedChecks[0]++;
                    if (completedChecks[0] == listResult.getItems().size()) {
                        callback.onUsageCalculated(totalSize[0], fileCount[0]);
                    }
                });
            }
        }).addOnFailureListener(e -> {
            callback.onError(e.getMessage());
        });
    }
    
    // Interface for storage usage callback
    public interface StorageUsageCallback {
        void onUsageCalculated(long totalBytes, int fileCount);
        void onError(String errorMessage);
    }
}