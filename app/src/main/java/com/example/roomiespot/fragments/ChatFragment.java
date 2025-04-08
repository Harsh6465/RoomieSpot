package com.example.roomiespot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomiespot.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        recyclerView = view.findViewById(R.id.recycler_chats);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialize chat list
        loadChats();
    }
    
    private void loadChats() {
        // Here you would load chat conversations from Firebase
        // For now, this is a placeholder for the implementation
        
        // Example implementation:
        // Query query = db.collection("chats")
        //     .whereArrayContains("participants", auth.getCurrentUser().getUid());
        // 
        // query.addSnapshotListener((value, error) -> {
        //     if (error != null) {
        //         // Handle error
        //         return;
        //     }
        //     
        //     List<ChatConversation> conversations = new ArrayList<>();
        //     for (DocumentSnapshot doc : value.getDocuments()) {
        //         // Parse chat data and add to list
        //     }
        //     
        //     // Update adapter with conversations
        // });
    }
}