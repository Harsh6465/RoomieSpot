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

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.ViewHolder> {
    private Context context;
    private List<Property> properties;

    public PropertyAdapter(Context context, List<Property> properties) {
        this.context = context;
        this.properties = properties;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_property, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = properties.get(position);  // Changed propertyList to properties
        
        holder.tvTitle.setText(property.getTitle());
        holder.tvPrice.setText(String.format("$%.2f", property.getPrice()));
        holder.tvLocation.setText(property.getLocation());

        // Load the first image if available
        if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
            Glide.with(context)
                .load(property.getImageUrls().get(0))
                .into(holder.propertyImage);
        }
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView propertyImage;
        TextView tvTitle, tvPrice, tvLocation;

        public ViewHolder(View itemView) {
            super(itemView);
            propertyImage = itemView.findViewById(R.id.property_image);
            tvTitle = itemView.findViewById(R.id.property_title);
            tvPrice = itemView.findViewById(R.id.property_price);
            tvLocation = itemView.findViewById(R.id.property_location);
        }
    }
}