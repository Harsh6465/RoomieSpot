package com.example.roomiespot;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import com.example.roomiespot.adapters.MessageThreadAdapter;
import com.example.roomiespot.models.MessageThread;

public class MessagesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageThreadAdapter messageAdapter;
    private List<MessageThread> messageThreads;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Messages");

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        setupRecyclerView();
        
        // Load message threads
        loadMessageThreads();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view_messages);
        messageThreads = new ArrayList<>();
        messageAdapter = new MessageThreadAdapter(this, messageThreads);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
    }

    private void loadMessageThreads() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("message_threads")
            .whereArrayContains("participants", currentUserId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                messageThreads.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    MessageThread thread = document.toObject(MessageThread.class);
                    thread.setId(document.getId());
                    messageThreads.add(thread);
                }
                messageAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                // Handle error
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}