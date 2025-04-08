package com.example.roomiespot;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class NextActivity extends AppCompatActivity {

    private Button imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        // Initialize views
        imageButton = findViewById(R.id.image_button);

        // Setup click listener
        imageButton.setOnClickListener(v -> 
            Toast.makeText(NextActivity.this, "Button clicked", Toast.LENGTH_SHORT).show()
        );
    }
}