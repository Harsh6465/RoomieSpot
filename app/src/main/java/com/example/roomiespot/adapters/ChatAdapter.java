package com.example.roomiespot.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomiespot.ChatActivity;
import com.example.roomiespot.R;
import com.example.roomiespot.models.Chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context context;
    private List<Chat> chatList;

    public ChatAdapter(Context context, List<Chat> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        
        // Set user name
        holder.tvUserName.setText(chat.getUserName() != null ? chat.getUserName() : "User");
        
        // Set property title if available
        if (chat.getPropertyTitle() != null && !chat.getPropertyTitle().isEmpty()) {
            holder.tvPropertyTitle.setText("Re: " + chat.getPropertyTitle());
            holder.tvPropertyTitle.setVisibility(View.VISIBLE);
        } else {
            holder.tvPropertyTitle.setVisibility(View.GONE);
        }
        
        // Set last message
        holder.tvLastMessage.setText(chat.getLastMessage());
        
        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        String time = sdf.format(new Date(chat.getLastMessageTime()));
        holder.tvTime.setText(time);
        
        // Set unread indicator
        if (chat.isHasUnreadMessages()) {
            holder.tvUnreadIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.tvUnreadIndicator.setVisibility(View.GONE);
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("RECEIVER_ID", chat.getUserId());
            intent.putExtra("RECEIVER_NAME", chat.getUserName());
            if (chat.getPropertyId() != null && !chat.getPropertyId().isEmpty()) {
                intent.putExtra("PROPERTY_ID", chat.getPropertyId());
            }
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        TextView tvPropertyTitle;
        TextView tvLastMessage;
        TextView tvTime;
        TextView tvUnreadIndicator;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvPropertyTitle = itemView.findViewById(R.id.tv_property_title);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvUnreadIndicator = itemView.findViewById(R.id.tv_unread_indicator);
        }
    }
}