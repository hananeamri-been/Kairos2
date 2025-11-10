package com.example.kairos;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TravelActivity extends BaseActivity implements AddTravelDialog.Listener, TravelAdapter.Listener {

    private TravelStore store;
    private TravelAdapter adapter;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);

        store = new TravelStore(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("My Travels");
        setSupportActionBar(toolbar);

        RecyclerView rv = findViewById(R.id.rvTravels);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TravelAdapter(this, this);
        rv.setAdapter(adapter);

        // bottom bar (mêmes IDs que part_bottom_bar)
        setupBottomBar();
    }

    @Override protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        ArrayList<Travel> all = store.loadAll();
        adapter.submit(all);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_travel, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_travel) {
            new AddTravelDialog(this, this).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // AddTravelDialog.Listener
    @Override public void onTravelCreated(Travel t) {
        store.upsert(t);
        refresh();

    }

    // TravelAdapter.Listener
    @Override public void onOpen(Travel t) {
        Intent it = new Intent(this, TravelDetailActivity.class);
        it.putExtra("travel_id", t.id);
        startActivity(it);
    }

    @Override public void onDelete(Travel t) {
        store.deleteById(t.id);
        refresh();
    }
}
