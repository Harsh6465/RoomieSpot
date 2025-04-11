package com.example.roomiespot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
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
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
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
    private FloatingActionButton fabAddProperty;
    private BroadcastReceiver propertyAddedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
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
        setupRecyclerView();

        // Setup FAB with comprehensive error handling
        setupFAB();

        // Register broadcast receiver for property updates
        registerPropertyAddedReceiver();

        // Load properties
        loadProperties();
    }

    private void registerPropertyAddedReceiver() {
        propertyAddedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Reload properties when a new property is added
                loadProperties();
            }
        };
        
        IntentFilter filter = new IntentFilter("com.example.roomiespot.PROPERTY_ADDED");
        registerReceiver(propertyAddedReceiver, filter);
    }

    private void setupFAB() {
        try {
            fabAddProperty = findViewById(R.id.fab_add_property);
            
            if (fabAddProperty == null) {
                Log.e("MainActivity", "FAB not found in layout");
                return;
            }
            
            // Check if user is authenticated before allowing property addition
            fabAddProperty.setOnClickListener(v -> {
                try {
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        // User is authenticated, proceed to add property
                        Intent intent = new Intent(MainActivity.this, AddPropertyActivity.class);
                        intent.putExtra("property_type", "room"); // Default property type
                        startActivity(intent);
                    } else {
                        // Redirect to login if not authenticated
                        Toast.makeText(this, "Please log in to add a property", Toast.LENGTH_SHORT).show();
                        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(loginIntent);
                        finish();
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Error in FAB click listener", e);
                    Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Error setting up FAB", e);
            Toast.makeText(this, "Could not set up property addition button", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        propertyList = new ArrayList<>();
        propertyAdapter = new PropertyAdapter(this, propertyList, new PropertyAdapter.OnPropertyClickListener() {
            @Override
            public void onPropertyClick(Property property) {
                // Open Property Detail Activity
                Intent intent = new Intent(MainActivity.this, PropertyDetailActivity.class);
                
                // Pass property details via intent
                intent.putExtra("PROPERTY_ID", property.getId());
                intent.putExtra("PROPERTY_TITLE", property.getTitle());
                intent.putExtra("PROPERTY_DESCRIPTION", property.getDescription());
                intent.putExtra("PROPERTY_PRICE", property.getPrice());
                intent.putExtra("PROPERTY_LOCATION", property.getLocation());
                intent.putExtra("PROPERTY_TYPE", property.getPropertyType());
                
                // Pass landlord contact details
                intent.putExtra("LANDLORD_PHONE", property.getLandlordPhoneNumber());
                intent.putExtra("LANDLORD_EMAIL", property.getLandlordEmail());
                
                // Pass image URLs if available
                if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
                    intent.putStringArrayListExtra("PROPERTY_IMAGES", new ArrayList<>(property.getImageUrls()));
                }

                startActivity(intent);
            }
        });
        recyclerView.setAdapter(propertyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout == null) {
            Log.e(TAG, "DrawerLayout is null in setupViews()");
        }

        navigationView = findViewById(R.id.nav_view);
        if (navigationView == null) {
            Log.e(TAG, "NavigationView is null in setupViews()");
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) {
            Log.e(TAG, "BottomNavigationView is null in setupViews()");
        }

        recyclerView = findViewById(R.id.recycler_view_properties);
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView is null in setupViews()");
        }

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        if (swipeRefreshLayout == null) {
            Log.e(TAG, "SwipeRefreshLayout is null in setupViews()");
        }

        swipeRefreshLayout.setOnRefreshListener(this::loadProperties);
    }

    private void setupNavigation() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Ensure navigation view is properly set up
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        } else {
            Log.e(TAG, "NavigationView is null in setupNavigation()");
        }
        
        // Add a separate listener for bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Already in home, do nothing or refresh
                loadProperties();
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, MessagesActivity.class));
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(this, AddPropertyActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            
            return false;
        });
    }

    private void loadProperties() {
        Log.d(TAG, "Starting loadProperties() method");
        swipeRefreshLayout.setRefreshing(true);
        
        // Query properties for the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No current user found. Cannot load properties.");
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "Please log in to view properties", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = currentUser.getUid();
        Log.d(TAG, "Current User ID: " + currentUserId);
        
        // Debug: Print user authentication details
        Log.d(TAG, "User Email: " + currentUser.getEmail());
        Log.d(TAG, "User Display Name: " + currentUser.getDisplayName());
        
        // First, try querying by userId
        db.collection("properties")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Firestore query successful");
                Log.d(TAG, "Documents found by userId: " + queryDocumentSnapshots.size());
                
                propertyList.clear();
                
                // If no properties found by userId, try ownerId
                if (queryDocumentSnapshots.isEmpty()) {
                    Log.w(TAG, "No properties found with userId. Attempting ownerId query.");
                    
                    db.collection("properties")
                        .whereEqualTo("ownerId", currentUserId)
                        .get()
                        .addOnSuccessListener(ownerQuerySnapshots -> {
                            Log.d(TAG, "Documents found by ownerId: " + ownerQuerySnapshots.size());
                            
                            for (QueryDocumentSnapshot document : ownerQuerySnapshots) {
                                Log.d(TAG, "Processing document: " + document.getId());
                                Log.d(TAG, "Document data: " + document.getData());
                                
                                Property property = document.toObject(Property.class);
                                property.setId(document.getId());
                                
                                // Ensure userId is set
                                if (property.getUserId() == null) {
                                    property.setUserId(currentUserId);
                                    Log.d(TAG, "Set userId for property: " + property.getId());
                                }
                                
                                propertyList.add(property);
                            }
                            
                            propertyAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                            
                            if (propertyList.isEmpty()) {
                                Log.w(TAG, "No properties found for user");
                                Toast.makeText(this, "No properties found. Add your first property!", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d(TAG, "Total properties loaded: " + propertyList.size());
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error querying properties by ownerId", e);
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(this, "Failed to load properties: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                } else {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d(TAG, "Processing document: " + document.getId());
                        Log.d(TAG, "Document data: " + document.getData());
                        
                        Property property = document.toObject(Property.class);
                        property.setId(document.getId());
                        propertyList.add(property);
                    }
                    
                    propertyAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                    
                    Log.d(TAG, "Total properties loaded: " + propertyList.size());
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error querying properties by userId", e);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, "Failed to load properties: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already in home, do nothing or refresh
            loadProperties();
        } else if (id == R.id.nav_chat) {
            startActivity(new Intent(this, MessagesActivity.class));
        } else if (id == R.id.nav_add) {
            startActivity(new Intent(this, AddPropertyActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(this, HelpActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // Close the drawer after selection
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

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receiver to prevent memory leaks
        if (propertyAddedReceiver != null) {
            unregisterReceiver(propertyAddedReceiver);
        }
    }
}