package com.example.roomiespot.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    // Add quality constant at the top of the class
    private static final int COMPRESSION_QUALITY = 80; // 80% quality for JPEG compression

    // Get compressed bitmap from Uri
    public static Bitmap getCompressedBitmap(Context context, Uri imageUri, int maxDimension) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(imageUri);
        
        // Get original dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, options);
        input.close();
        
        // Calculate sample size
        options.inSampleSize = calculateSampleSize(options, maxDimension);
        
        // Decode bitmap with sample size
        options.inJustDecodeBounds = false;
        input = context.getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
        input.close();
        
        // Fix orientation if needed
        input = context.getContentResolver().openInputStream(imageUri);
        bitmap = fixOrientation(bitmap, input);
        input.close();
        
        return bitmap;
    }
    
    // Calculate sample size for downsampling
    private static int calculateSampleSize(BitmapFactory.Options options, int maxDimension) {
        int height = options.outHeight;
        int width = options.outWidth;
        int sampleSize = 1;
        
        if (height > maxDimension || width > maxDimension) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / sampleSize) >= maxDimension || (halfWidth / sampleSize) >= maxDimension) {
                sampleSize *= 2;
            }
        }
        
        return sampleSize;
    }
    
    // Fix image orientation based on EXIF data
    private static Bitmap fixOrientation(Bitmap bitmap, InputStream input) {
        try {
            ExifInterface exif = new ExifInterface(input);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap;
            }
            
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            return bitmap;
        }
    }
    
    // Update the bitmapToByteArray method
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, stream);
        return stream.toByteArray();
    }
}