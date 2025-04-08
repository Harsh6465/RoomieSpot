package com.example.roomiespot;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomiespot.adapters.MessageAdapter;
import com.example.roomiespot.models.Message;
import com.example.roomiespot.models.Property;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvReceiverName;
    private TextView tvPropertyTitle;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageButton btnSend;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;
    private String receiverId;
    private String receiverName;
    private String propertyId;
    private Property property;

    private List<Message> messageList;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        currentUserId = mAuth.getCurrentUser().getUid();

        // Get data from intent
        receiverId = getIntent().getStringExtra("RECEIVER_ID");
        receiverName = getIntent().getStringExtra("RECEIVER_NAME");
        propertyId = getIntent().getStringExtra("PROPERTY_ID");

        if (receiverId == null || receiverName == null) {
            Toast.makeText(this, "Chat information not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Set receiver name
        tvReceiverName.setText(receiverName);

        // Setup message list
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        // Load property data if available
        if (propertyId != null) {
            loadPropertyData();
        } else {
            tvPropertyTitle.setVisibility(View.GONE);
        }

        // Load messages
        loadMessages();

        // Setup send button
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvReceiverName = findViewById(R.id.tv_receiver_name);
        tvPropertyTitle = findViewById(R.id.tv_property_title);
        recyclerView = findViewById(R.id.recycler_view);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
    }

    private void loadPropertyData() {
        db.collection("properties").document(propertyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        property = documentSnapshot.toObject(Property.class);
                        property.setId(documentSnapshot.getId());
                        tvPropertyTitle.setText("Re: " + property.getTitle());
                        tvPropertyTitle.setVisibility(View.VISIBLE);
                    } else {
                        tvPropertyTitle.setVisibility(View.GONE);
                    }
                });
    }

    private void loadMessages() {
        db.collection("messages")
                .whereEqualTo("senderId", currentUserId)
                .whereEqualTo("receiverId", receiverId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Message message = dc.getDocument().toObject(Message.class);
                                message.setId(dc.getDocument().getId());
                                messageList.add(message);
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                        if (messageList.size() > 0) {
                            recyclerView.smoothScrollToPosition(messageList.size() - 1);
                        }
                    }
                });

        db.collection("messages")
                .whereEqualTo("senderId", receiverId)
                .whereEqualTo("receiverId", currentUserId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Message message = dc.getDocument().toObject(Message.class);
                                message.setId(dc.getDocument().getId());
                                
                                // Mark message as read
                                if (!message.isRead()) {
                                    message.setRead(true);
                                    db.collection("messages").document(message.getId())
                                            .update("read", true);
                                }
                                
                                messageList.add(message);
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                        if (messageList.size() > 0) {
                            recyclerView.smoothScrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        
        if (text.isEmpty()) {
            return;
        }
        
        Message message = new Message(currentUserId, receiverId, text, propertyId);
        
        db.collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    etMessage.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error sending message: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}