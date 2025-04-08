package com.example.roomiespot.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ImageCompressor {

    public static byte[] compressImage(Context context, Uri imageUri, int maxWidth, int maxHeight, int quality) {
        try {
            InputStream input = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            input.close();

            // Calculate sample size
            int sampleSize = calculateSampleSize(options, maxWidth, maxHeight);
            
            // Decode bitmap with sample size
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            
            input = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
            input.close();

            // Compress bitmap
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int calculateSampleSize(BitmapFactory.Options options, int maxWidth, int maxHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int sampleSize = 1;

        if (height > maxHeight || width > maxWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / sampleSize) >= maxHeight && (halfWidth / sampleSize) >= maxWidth) {
                sampleSize *= 2;
            }
        }

        return sampleSize;
    }
}