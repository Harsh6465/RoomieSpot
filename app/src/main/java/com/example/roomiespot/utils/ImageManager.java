package com.example.roomiespot.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageManager {
    private Context context;
    private static final int COMPRESSION_QUALITY = 80;

    public ImageManager(Context context) {
        this.context = context;
    }

    public String saveImage(Bitmap bitmap, String propertyId) throws IOException {
        File directory = new File(context.getFilesDir(), "property_images");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = "property_" + propertyId + "_" + System.currentTimeMillis() + ".jpg";
        File file = new File(directory, fileName);

        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, fos);
        fos.close();

        return file.getAbsolutePath();
    }

    public Bitmap loadImage(String path) {
        return BitmapFactory.decodeFile(path);
    }
}