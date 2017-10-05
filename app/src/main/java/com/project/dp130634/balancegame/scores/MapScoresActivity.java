package com.project.dp130634.balancegame.scores;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.project.dp130634.balancegame.MainActivity;
import com.project.dp130634.balancegame.R;
import com.project.dp130634.balancegame.scores.util.ScoreCursorAdapter;

import java.util.List;

public class MapScoresActivity extends AppCompatActivity {

    private String mapName;
    private DBHelper dbHelper;
    private CursorAdapter cursorAdapter;
    private ListView scoreListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.activity_map_scores);

        mapName = getIntent().getStringExtra(MainActivity.MAP_NAME_KEY);
        dbHelper = new DBHelper(this);
        Cursor mapScoresCursor = dbHelper.selectScoresForMap(mapName);
        cursorAdapter = new ScoreCursorAdapter(this, mapScoresCursor);
        scoreListView = (ListView)findViewById(R.id.scoreEntriesListView);
        scoreListView.setAdapter(cursorAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_map_scores, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.clearScores) {
            dbHelper.clearScores(mapName);
            scoreListView.setAdapter(null);
            Toast.makeText(this, R.string.scoresCleared, Toast.LENGTH_SHORT).show();
            return true;
        }
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
