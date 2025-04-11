package com.example.roomiespot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.roomiespot.R;
import com.example.roomiespot.models.Property;

import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {
    private Context context;
    private List<Property> properties;
    private OnPropertyClickListener clickListener;

    // Interface for click events
    public interface OnPropertyClickListener {
        void onPropertyClick(Property property);
    }

    // Constructor with click listener
    public PropertyAdapter(Context context, List<Property> properties, OnPropertyClickListener clickListener) {
        this.context = context;
        this.properties = properties;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_property, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        Property property = properties.get(position);

        // Set property details
        holder.titleTextView.setText(property.getTitle());
        holder.priceTextView.setText(String.format("$%.2f", property.getPrice()));
        holder.locationTextView.setText(property.getLocation());

        // Load property image
        if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
            Glide.with(context)
                .load(property.getImageUrls().get(0))
                .into(holder.propertyImageView);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPropertyClick(property);
            }
        });
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    // ViewHolder class
    public static class PropertyViewHolder extends RecyclerView.ViewHolder {
        ImageView propertyImageView;
        TextView titleTextView;
        TextView priceTextView;
        TextView locationTextView;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            propertyImageView = itemView.findViewById(R.id.property_image);
            titleTextView = itemView.findViewById(R.id.property_title);
            priceTextView = itemView.findViewById(R.id.property_price);
            locationTextView = itemView.findViewById(R.id.property_location);
        }
    }
}