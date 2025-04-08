package com.example.roomiespot;

// Add these imports
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import com.google.firebase.firestore.FirebaseFirestore;
import android.animation.ObjectAnimator;
import androidx.cardview.widget.CardView;
import android.view.animation.AccelerateDecelerateInterpolator;

// Add this import at the top with other imports
import android.widget.ImageView;

public class LoginActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp;
    private FirebaseAuth mAuth;
    private CardView loginCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        progressBar = findViewById(R.id.progress_bar);
        loginCardView = findViewById(R.id.login_card_view);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvSignUp = findViewById(R.id.tv_sign_up);
        ImageView logoImageView = findViewById(R.id.logo_image);  // Keep only this one initialization

        // Apply animations
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Apply animations to views
        etEmail.startAnimation(slideUp);
        etPassword.startAnimation(slideUp);
        btnLogin.startAnimation(fadeIn);
        tvSignUp.startAnimation(fadeIn);

        // Load logo with Glide animation
        if (logoImageView != null) {
            Glide.with(this)
                .load(R.drawable.app_logo)
                .transition(DrawableTransitionOptions.withCrossFade(1000))
                .into(logoImageView);
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Setup click listeners
        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.d("LoginActivity", "Login successful");
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.e("LoginActivity", "Login failed: " + e.getMessage());
                    Toast.makeText(LoginActivity.this, 
                        "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void animateCard() {
        // Elevation animation
        ObjectAnimator elevation = ObjectAnimator.ofFloat(loginCardView, "cardElevation", 0f, 16f);
        elevation.setDuration(1000);
        elevation.setInterpolator(new AccelerateDecelerateInterpolator());
        elevation.start();

        // Corner radius animation
        ObjectAnimator cornerRadius = ObjectAnimator.ofFloat(loginCardView, "radius", 0f, 32f);
        cornerRadius.setDuration(1000);
        cornerRadius.setInterpolator(new AccelerateDecelerateInterpolator());
        cornerRadius.start();

        // Background color transition
        ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), 
            getResources().getColor(R.color.cardColorStart), 
            getResources().getColor(R.color.cardColorEnd));
        colorAnim.setDuration(1000);
        colorAnim.addUpdateListener(animator -> 
            loginCardView.setCardBackgroundColor((int) animator.getAnimatedValue()));
        colorAnim.start();
    }
}