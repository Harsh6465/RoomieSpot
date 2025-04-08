package com.example.roomiespot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.roomiespot.models.Property; // Add this import
import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {
    private List<Property> propertyList;

    public PropertyAdapter(List<Property> propertyList) {
        this.propertyList = propertyList;
    }

    @Override
    public PropertyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_property, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PropertyViewHolder holder, int position) {
        Property property = propertyList.get(position);
        holder.bind(property);
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    class PropertyViewHolder extends RecyclerView.ViewHolder {
        TextView title, price;

        PropertyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.property_title);
            price = itemView.findViewById(R.id.property_price);
        }

        void bind(Property property) {
            title.setText(property.getTitle());
            price.setText(String.valueOf(property.getPrice()));
        }
    }
}