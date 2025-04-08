package com.example.roomiespot;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roomiespot.utils.StorageManager;

public class StorageMonitorActivity extends AppCompatActivity {

    private TextView tvStorageUsed;
    private TextView tvFileCount;
    private ProgressBar progressBar;
    
    // Free tier limit in bytes (5GB)
    private static final long FREE_TIER_LIMIT = 5L * 1024 * 1024 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_monitor);
        
        tvStorageUsed = findViewById(R.id.tv_storage_used);
        tvFileCount = findViewById(R.id.tv_file_count);
        progressBar = findViewById(R.id.progress_bar);
        
        loadStorageStats();
    }
    
    private void loadStorageStats() {
        progressBar.setVisibility(View.VISIBLE);
        
        StorageManager.getStorageUsage(new StorageManager.StorageUsageCallback() {
            @Override
            public void onUsageCalculated(long totalBytes, int fileCount) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    // Display storage used
                    String usedStorage = formatSize(totalBytes);
                    String totalStorage = formatSize(FREE_TIER_LIMIT);
                    double percentUsed = ((double) totalBytes / FREE_TIER_LIMIT) * 100;
                    
                    tvStorageUsed.setText(String.format("Storage Used: %s / %s (%.1f%%)", 
                            usedStorage, totalStorage, percentUsed));
                    tvFileCount.setText(String.format("Total Files: %d", fileCount));
                });
            }
            
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvStorageUsed.setText("Error: " + errorMessage);
                });
            }
        });
    }
    
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}