package com.example.roomiespot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.roomiespot.R;
import com.example.roomiespot.models.Review;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviewList;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        
        holder.tvUserName.setText(review.getUserName());
        holder.ratingBar.setRating(review.getRating());
        holder.tvComment.setText(review.getComment());
        
        // Format date
        if (review.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String date = sdf.format(review.getCreatedAt().toDate());
            holder.tvDate.setText(date);
        }
        
        // Load user photo
        if (review.getUserPhotoUrl() != null && !review.getUserPhotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(review.getUserPhotoUrl())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(holder.ivUserPhoto);
        } else {
            holder.ivUserPhoto.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserPhoto;
        TextView tvUserName, tvComment, tvDate;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserPhoto = itemView.findViewById(R.id.iv_user_photo);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvComment = itemView.findViewById(R.id.tv_comment);
            tvDate = itemView.findViewById(R.id.tv_date);
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }
    }
}