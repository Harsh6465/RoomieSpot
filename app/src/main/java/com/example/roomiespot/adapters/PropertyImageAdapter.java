package com.example.roomiespot.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.roomiespot.R;

import java.util.List;

public class PropertyImageAdapter extends RecyclerView.Adapter<PropertyImageAdapter.ImageViewHolder> {
    private Context context;
    private List<Uri> imageUris;
    private OnImageDeleteListener deleteListener;

    public interface OnImageDeleteListener {
        void onImageDelete(int position);
    }

    public PropertyImageAdapter(Context context, List<Uri> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
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
        Uri imageUri = imageUris.get(position);
        Glide.with(context)
                .load(imageUri)
                .centerCrop()
                .into(holder.imageView);

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onImageDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
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
}