package com.example.roomiespot;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Favorites");

        // Setup swipe refresh
        swipeRefresh.setOnRefreshListener(this::loadFavorites);

        // Initial load
        loadFavorites();
    }

    private void loadFavorites() {
        // TODO: Implement loading favorites from Firebase
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}