package com.example.roomiespot;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomiespot.adapters.ChatMessageAdapter;
import com.example.roomiespot.models.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    
    private RecyclerView recyclerViewMessages;
    private EditText etMessageInput;
    private ImageButton btnSendMessage;
    private TextView tvRecipientName;
    
    private List<ChatMessage> messageList;
    private ChatMessageAdapter messageAdapter;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private String currentUserId;
    private String recipientId;
    private String chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        
        // Get recipient details from intent
        recipientId = getIntent().getStringExtra("RECIPIENT_ID");
        String recipientName = getIntent().getStringExtra("RECIPIENT_NAME");
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(recipientName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Initialize views
        initializeViews();
        
        // Generate or fetch chat room ID
        generateChatRoomId();
        
        // Setup message recycler view
        setupMessageRecyclerView();
        
        // Load existing messages
        loadMessages();
        
        // Setup send message functionality
        setupMessageSending();
    }
    
    private void initializeViews() {
        recyclerViewMessages = findViewById(R.id.recycler_view_messages);
        etMessageInput = findViewById(R.id.et_message_input);
        btnSendMessage = findViewById(R.id.btn_send_message);
        tvRecipientName = findViewById(R.id.tv_receiver_name);
    }
    
    private void generateChatRoomId() {
        // Create a unique chat room ID based on participants
        String[] participants = new String[]{currentUserId, recipientId};
        java.util.Arrays.sort(participants);
        chatRoomId = participants[0] + "_" + participants[1];
    }
    
    private void setupMessageRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(this, messageList, currentUserId);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);
    }
    
    private void loadMessages() {
        db.collection("chat_rooms")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) {
                    android.util.Log.e(TAG, "Error loading messages", e);
                    return;
                }
                
                messageList.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    ChatMessage message = doc.toObject(ChatMessage.class);
                    messageList.add(message);
                }
                
                messageAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
            });
    }
    
    private void setupMessageSending() {
        // Enable/disable send button based on message input
        etMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSendMessage.setEnabled(s.toString().trim().length() > 0);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Send message on button click
        btnSendMessage.setOnClickListener(v -> {
            String messageText = etMessageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            }
        });
    }
    
    private void sendMessage(String messageText) {
        // Create message object
        ChatMessage message = new ChatMessage(
            currentUserId, 
            recipientId, 
            messageText, 
            ChatMessage.MessageType.TEXT
        );
        
        // Save to Firestore
        db.collection("chat_rooms")
            .document(chatRoomId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener(documentReference -> {
                // Clear input and scroll to bottom
                etMessageInput.setText("");
                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e(TAG, "Error sending message", e);
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
            });
        
        // Update last message in chat room metadata
        Map<String, Object> chatRoomData = new HashMap<>();
        chatRoomData.put("lastMessage", messageText);
        chatRoomData.put("lastMessageTimestamp", System.currentTimeMillis());
        
        db.collection("chat_rooms")
            .document(chatRoomId)
            .set(chatRoomData, com.google.firebase.firestore.SetOptions.merge());
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}