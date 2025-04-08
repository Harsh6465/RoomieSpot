package com.example.roomiespot.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class LocalStorageManager {
    private static final String TAG = "LocalStorageManager";
    private static final String IMAGE_DIRECTORY = "RoomieSpot/Images";

    /**
     * Saves an image from a Uri to local storage
     * @param context Application context
     * @param imageUri Uri of the image to save
     * @return Path to the saved image or null if failed
     */
    public static String saveImageToLocal(Context context, Uri imageUri) {
        try {
            // Create directory if it doesn't exist
            File directory = new File(context.getFilesDir(), IMAGE_DIRECTORY);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate unique filename
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + timeStamp + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
            File outputFile = new File(directory, fileName);

            // Copy image data to file
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            inputStream.close();
            outputStream.close();
            
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Deletes an image from local storage
     * @param imagePath Path to the image to delete
     * @return true if deleted successfully, false otherwise
     */
    public static boolean deleteImageFromLocal(String imagePath) {
        File file = new File(imagePath);
        return file.exists() && file.delete();
    }
}