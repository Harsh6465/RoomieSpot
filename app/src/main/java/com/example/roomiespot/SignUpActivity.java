package com.example.roomiespot;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.roomiespot.models.User;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ArgbEvaluator;
import androidx.cardview.widget.CardView;
import android.view.animation.AccelerateDecelerateInterpolator;

public class SignUpActivity extends AppCompatActivity {
    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSignUp = findViewById(R.id.btn_sign_up);
        tvLogin = findViewById(R.id.tv_login);
        ImageView logoImageView = findViewById(R.id.logo_image);

        // Add card animation
        animateCard();

        // Apply animations
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Apply animations to views
        etFullName.startAnimation(slideUp);
        etEmail.startAnimation(slideUp);
        etPassword.startAnimation(slideUp);
        etConfirmPassword.startAnimation(slideUp);
        btnSignUp.startAnimation(fadeIn);
        tvLogin.startAnimation(fadeIn);

        // Load logo with Glide animation
        if (logoImageView != null) {
            Glide.with(this)
                .load(R.drawable.app_logo)  // Make sure you have this image in drawable
                .transition(DrawableTransitionOptions.withCrossFade(1000))
                .into(logoImageView);
        }

        // Setup click listeners
        btnSignUp.setOnClickListener(v -> signUp());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void signUp() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Save user details to Firestore
                    String userId = authResult.getUser().getUid();
                    db.collection("users").document(userId)
                            .set(new User(userId, fullName, email))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(SignUpActivity.this, "Sign up successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(SignUpActivity.this, 
                                "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(SignUpActivity.this, 
                    "Sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void animateCard() {
        CardView signupCardView = findViewById(R.id.signup_card_view);
        
        // Elevation animation
        ObjectAnimator elevation = ObjectAnimator.ofFloat(signupCardView, "cardElevation", 0f, 16f);
        elevation.setDuration(1000);
        elevation.setInterpolator(new AccelerateDecelerateInterpolator());
        elevation.start();
    
        // Corner radius animation
        ObjectAnimator cornerRadius = ObjectAnimator.ofFloat(signupCardView, "radius", 0f, 32f);
        cornerRadius.setDuration(1000);
        cornerRadius.setInterpolator(new AccelerateDecelerateInterpolator());
        cornerRadius.start();
    
        // Background color transition
        ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), 
            getResources().getColor(R.color.cardColorStart), 
            getResources().getColor(R.color.cardColorEnd));
        colorAnim.setDuration(1000);
        colorAnim.addUpdateListener(animator -> 
            signupCardView.setCardBackgroundColor((int) animator.getAnimatedValue()));
        colorAnim.start();
    }
}