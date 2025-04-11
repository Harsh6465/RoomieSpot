package com.example.roomiespot.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.roomiespot.R;

import java.io.File;
import java.util.List;

public class PropertyImageAdapter extends RecyclerView.Adapter<PropertyImageAdapter.ImageViewHolder> {
    private Context context;
    private List<String> imagePaths;
    private OnImageDeleteListener deleteListener;

    public interface OnImageDeleteListener {
        void onImageDelete(int position);
    }

    public PropertyImageAdapter(Context context, List<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
    }

    public void setOnImageDeleteListener(OnImageDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_property_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        
        try {
            // Check if path is a local file or a URL
            if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
                // Local content URI
                holder.imageView.setImageURI(Uri.parse(imagePath));
            } else if (new File(imagePath).exists()) {
                // Local file path
                holder.imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            } else {
                // Assume it's a network URL
                Glide.with(context)
                    .load(imagePath)
                    .placeholder(R.drawable.placeholder_property)
                    .error(R.drawable.placeholder_property)
                    .centerCrop()
                    .into(holder.imageView);
            }
        } catch (Exception e) {
            Log.e("PropertyImageAdapter", "Error loading image", e);
            holder.imageView.setImageResource(R.drawable.placeholder_property);
        }

        // Optional: Delete functionality (only if deleteButton exists in layout)
        if (holder.deleteButton != null) {
            holder.deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onImageDelete(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return imagePaths != null ? imagePaths.size() : 0;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView deleteButton;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.property_image);
            deleteButton = itemView.findViewById(R.id.btn_delete_image);
        }
    }

    // Method to update image list
    public void updateImages(List<String> newImagePaths) {
        this.imagePaths = newImagePaths;
        notifyDataSetChanged();
    }
}