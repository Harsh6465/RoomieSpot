package com.example.roomiespot;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import com.example.roomiespot.adapters.PropertyAdapter;
import com.example.roomiespot.models.Property;

public class MainActivity extends AppCompatActivity 
        implements NavigationView.OnNavigationItemSelectedListener, 
        BottomNavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView;
    private PropertyAdapter propertyAdapter;
    private List<Property> propertyList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;
    
    // Add FAB declarations here
    // Remove these FAB declarations
    /*
    private ExtendedFloatingActionButton fabAddRoom, fabAddApartment;
    private FloatingActionButton fabMain;
    private boolean isFabMenuOpen = false;
    private Animation fabOpenAnimation, fabCloseAnimation;
    private Animation fabRotateForward, fabRotateBackward;
    */

    // Add this single FAB declaration
    private FloatingActionButton fabAddProperty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup toolbar (keep only this one initialization)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("RoomieSpot");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Views
        setupViews();
        
        // Setup Navigation
        setupNavigation();

        // Setup RecyclerView
        setupRecyclerView();  // Call the method here instead

        // Add this line before loadProperties()
        // Replace setupFAB() with this
        fabAddProperty = findViewById(R.id.fab_add_property);
        fabAddProperty.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPropertyActivity.class);
            startActivity(intent);
        });

        // Load properties
        loadProperties();
    }

    // Remove these methods if they exist
    /*
    private void toggleFabMenu() {
        // Remove this entire method
    }
    
    private void setupFAB() {
        // Remove this entire method
    }
    */

    private void setupRecyclerView() {
        propertyList = new ArrayList<>();
        propertyAdapter = new PropertyAdapter(this, propertyList);
        recyclerView.setAdapter(propertyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerView = findViewById(R.id.recycler_view_properties);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        toolbar = findViewById(R.id.toolbar);  // Add this line
        setSupportActionBar(toolbar);          // Add this line

        swipeRefreshLayout.setOnRefreshListener(this::loadProperties);
    }

    private void setupNavigation() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    private void loadProperties() {
        swipeRefreshLayout.setRefreshing(true);
        db.collection("properties")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                propertyList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Property property = document.toObject(Property.class);
                    property.setId(document.getId());
                    propertyList.add(property);
                }
                propertyAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            })
            .addOnFailureListener(e -> {
                swipeRefreshLayout.setRefreshing(false);
                // Show error message
            });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_my_properties) {
            startActivity(new Intent(this, MyPropertiesActivity.class));
        } else if (id == R.id.nav_saved) {
            startActivity(new Intent(this, SavedPropertiesActivity.class));
        } else if (id == R.id.nav_messages) {
            startActivity(new Intent(this, MessagesActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(this, HelpActivity.class));
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Remove the entire toggleFabMenu() method

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}