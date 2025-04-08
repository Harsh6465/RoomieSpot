package com.example.roomiespot;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomiespot.adapters.ChatAdapter;
import com.example.roomiespot.models.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TextView tvNoChats;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    private List<Chat> chatList;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        currentUserId = mAuth.getCurrentUser().getUid();

        // Initialize views
        initViews();

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Messages");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup chat list
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Load chats
        loadChats();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler_view);
        tvNoChats = findViewById(R.id.tv_no_chats);
    }

    private void loadChats() {
        // Get all messages where current user is sender or receiver
        db.collection("messages")
                .whereEqualTo("senderId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String receiverId = document.getString("receiverId");
                        String propertyId = document.getString("propertyId");
                        long timestamp = document.getLong("timestamp");
                        String text = document.getString("text");
                        
                        // Check if chat already exists in the list
                        boolean chatExists = false;
                        for (Chat chat : chatList) {
                            if (chat.getUserId().equals(receiverId)) {
                                chatExists = true;
                                // Update timestamp if newer
                                if (timestamp > chat.getLastMessageTime()) {
                                    chat.setLastMessageTime(timestamp);
                                    chat.setLastMessage(text);
                                }
                                break;
                            }
                        }
                        
                        if (!chatExists) {
                            Chat chat = new Chat(receiverId, propertyId, timestamp);
                            chat.setLastMessage(text);
                            chatList.add(chat);
                        }
                    }
                    
                    loadReceivedMessages();
                });
    }

    private void loadReceivedMessages() {
        db.collection("messages")
                .whereEqualTo("receiverId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String senderId = document.getString("senderId");
                        String propertyId = document.getString("propertyId");
                        long timestamp = document.getLong("timestamp");
                        boolean read = document.getBoolean("read");
                        String text = document.getString("text");
                        
                        // Check if chat already exists in the list
                        boolean chatExists = false;
                        for (Chat chat : chatList) {
                            if (chat.getUserId().equals(senderId)) {
                                chatExists = true;
                                // Update timestamp if newer
                                if (timestamp > chat.getLastMessageTime()) {
                                    chat.setLastMessageTime(timestamp);
                                    chat.setLastMessage(text);
                                }
                                // Update unread status
                                if (!read) {
                                    chat.setHasUnreadMessages(true);
                                }
                                break;
                            }
                        }
                        
                        if (!chatExists) {
                            Chat chat = new Chat(senderId, propertyId, timestamp);
                            chat.setLastMessage(text);
                            chat.setHasUnreadMessages(!read);
                            chatList.add(chat);
                        }
                    }
                    
                    // Load user and property data for each chat
                    loadChatDetails();
                });
    }

    private void loadChatDetails() {
        if (chatList.isEmpty()) {
            tvNoChats.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }
        
        tvNoChats.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        
        for (Chat chat : chatList) {
            // Load user data
            db.collection("users").document(chat.getUserId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("name");
                            chat.setUserName(userName);
                            chatAdapter.notifyDataSetChanged();
                        }
                    });
            
            // Load property data if available
            if (chat.getPropertyId() != null && !chat.getPropertyId().isEmpty()) {
                db.collection("properties").document(chat.getPropertyId())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String propertyTitle = documentSnapshot.getString("title");
                                chat.setPropertyTitle(propertyTitle);
                                chatAdapter.notifyDataSetChanged();
                            }
                        });
            }
        }
    }
}