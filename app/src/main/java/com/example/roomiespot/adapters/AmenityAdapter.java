package com.example.roomiespot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomiespot.R;

import java.util.List;

public class AmenityAdapter extends RecyclerView.Adapter<AmenityAdapter.AmenityViewHolder> {

    private Context context;
    private List<String> amenities;

    public AmenityAdapter(Context context, List<String> amenities) {
        this.context = context;
        this.amenities = amenities;
    }

    @NonNull
    @Override
    public AmenityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_amenity, parent, false);
        return new AmenityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AmenityViewHolder holder, int position) {
        String amenity = amenities.get(position);
        holder.tvAmenity.setText(amenity);
    }

    @Override
    public int getItemCount() {
        return amenities.size();
    }

    public static class AmenityViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmenity;

        public AmenityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmenity = itemView.findViewById(R.id.tv_amenity);
        }
    }
}