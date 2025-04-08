package com.example.roomiespot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2000; // 2 seconds
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        mAuth = FirebaseAuth.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (mAuth.getCurrentUser() != null) {
                // User is logged in, go to main activity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // User is not logged in, go to login activity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish(); // Close splash activity
        }, SPLASH_DURATION);
    }
}