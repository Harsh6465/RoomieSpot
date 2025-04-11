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
        if (imageUri == null) {
            throw new IllegalArgumentException("Image URI cannot be null");
        }

        InputStream input = context.getContentResolver().openInputStream(imageUri);
        if (input == null) {
            throw new IOException("Unable to open input stream for image");
        }

        // Get original dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, options);
        input.close();

        // Reopen input stream
        input = context.getContentResolver().openInputStream(imageUri);
        
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, maxDimension, maxDimension);
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
        input.close();

        if (bitmap == null) {
            throw new IOException("Failed to decode bitmap");
        }

        // Rotate bitmap if needed
        Bitmap rotatedBitmap = rotateBitmapIfRequired(context, bitmap, imageUri);
        
        // Scale down if still too large
        if (rotatedBitmap.getWidth() > maxDimension || rotatedBitmap.getHeight() > maxDimension) {
            rotatedBitmap = Bitmap.createScaledBitmap(
                rotatedBitmap, 
                Math.min(rotatedBitmap.getWidth(), maxDimension),
                Math.min(rotatedBitmap.getHeight(), maxDimension), 
                true
            );
        }

        return rotatedBitmap;
    }

    private static int calculateInSampleSize(
        BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight 
                   && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static Bitmap rotateBitmapIfRequired(Context context, Bitmap bitmap, Uri imageUri) {
        try {
            ExifInterface exif = new ExifInterface(imageUri.getPath());
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