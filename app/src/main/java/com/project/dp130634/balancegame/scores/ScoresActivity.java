package com.project.dp130634.balancegame.scores;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.project.dp130634.balancegame.MainActivity;
import com.project.dp130634.balancegame.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ScoresActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ArrayAdapter<String> mapsListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.activity_scores);


        ListView mapsListView = (ListView)findViewById(R.id.mapScoresListView);

        List<String> mapList = listMaps();
        if(mapList.size() > 0) {
            mapsListViewAdapter = new ArrayAdapter<>(this, R.layout.maps_list_view_entry, mapList);
            mapsListView.setAdapter(mapsListViewAdapter);
        }

        mapsListView.setOnItemClickListener(this);
        registerForContextMenu(mapsListView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_context_scores, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.clearScoresContextItem) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            String mapName = mapsListViewAdapter.getItem(info.position);
            new DBHelper(this).clearScores(mapName);
            Toast.makeText(this, R.string.scoresCleared, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onContextItemSelected(item);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_scores, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.clearAllScores) {
            new DBHelper(this).clearAllScores();
            Toast.makeText(this, R.string.scoresCleared, Toast.LENGTH_SHORT).show();
            return true;
        }
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String mapName = ((TextView)view).getText().toString();
        Intent mapScoresIntent = new Intent(this, MapScoresActivity.class);
        mapScoresIntent.putExtra(MainActivity.MAP_NAME_KEY, mapName);
        startActivity(mapScoresIntent);
    }

    private List<String> listMaps() {
        ArrayList<String> retVal = new ArrayList<>();

        if(getBaseContext().getFileStreamPath(MainActivity.MAP_LIST_FILE_NAME).exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(MainActivity.MAP_LIST_FILE_NAME)))) {
                String line = br.readLine();
                while(line != null && !"".equals(line)) {
                    retVal.add(line);
                    line = br.readLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return retVal;
    }
}
