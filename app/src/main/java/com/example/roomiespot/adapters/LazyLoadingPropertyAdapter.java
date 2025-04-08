package com.example.roomiespot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.roomiespot.R;
import com.example.roomiespot.models.Property;
import java.util.List;

public class LazyLoadingPropertyAdapter extends RecyclerView.Adapter<LazyLoadingPropertyAdapter.PropertyViewHolder> {
    private Context context;
    private List<Property> properties;

    public LazyLoadingPropertyAdapter(Context context, List<Property> properties) {
        this.context = context;
        this.properties = properties;
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
        
        // Load image using Glide with disk caching
        if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
            Glide.with(context)
                .load(property.getImageUrls().get(0))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    static class PropertyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        PropertyViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.property_image);
        }
    }
}