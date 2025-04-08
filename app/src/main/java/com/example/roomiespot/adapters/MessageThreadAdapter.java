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
import com.example.roomiespot.models.MessageThread;
import java.util.List;

public class MessageThreadAdapter extends RecyclerView.Adapter<MessageThreadAdapter.ViewHolder> {
    private Context context;
    private List<MessageThread> messageThreads;

    public MessageThreadAdapter(Context context, List<MessageThread> messageThreads) {
        this.context = context;
        this.messageThreads = messageThreads;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message_thread, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageThread thread = messageThreads.get(position);
        holder.tvLastMessage.setText(thread.getLastMessage());
        
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("threadId", thread.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return messageThreads.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
        }
    }
}