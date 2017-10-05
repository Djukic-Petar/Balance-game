package com.project.dp130634.balancegame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.project.dp130634.balancegame.game.GameActivity;
import com.project.dp130634.balancegame.newMap.NewMapActivity;
import com.project.dp130634.balancegame.newMap.NewMapController;
import com.project.dp130634.balancegame.scores.DBHelper;
import com.project.dp130634.balancegame.scores.ScoresActivity;
import com.project.dp130634.balancegame.settings.SettingsActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String MAP_LIST_FILE_NAME = "maps.dat";
    public static final int SETTINGS_REQUEST_CODE = 1;
    public static final int SCORES_REQUEST_CODE = 2;
    public static final int NEW_MAP_REQUEST_CODE = 3;
    public static final int START_GAME_REQUEST_CODE = 4;
    public static final String MAP_NAME_KEY = "com.project.dp130634.balancegame.mapname";

    private ArrayAdapter<String> mapsListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView mapsListView = (ListView)findViewById(R.id.mapsListView);
        mapsListView.setOnItemClickListener(this);
        List<String> mapList = listMaps();
        mapsListViewAdapter = new ArrayAdapter<>(this, R.layout.maps_list_view_entry, mapList);
        mapsListView.setAdapter(mapsListViewAdapter);

        registerForContextMenu(mapsListView);

        //new DBHelper(this).clearDB();

        /*
        for(String mapName : mapList) {
            deleteFile(mapName);
        }
        deleteFile(MAP_LIST_FILE_NAME);
        */
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_context_main, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.deleteMap) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            String mapName = mapsListViewAdapter.getItem(info.position);
            deleteMap(mapName);
            mapsListViewAdapter.remove(mapName);
            Toast.makeText(this, R.string.mapDeleted, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.newMapMenuItem:
                goToMapCreation();
                return true;
            case R.id.highScoresMenuItem:
                showHighScores();
                return true;
            case R.id.settingsMenuItem:
                showSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToMapCreation() {
        startActivityForResult(new Intent(this, NewMapActivity.class), NEW_MAP_REQUEST_CODE);
    }

    private void showHighScores() {
        Intent scoresIntent = new Intent(this, ScoresActivity.class);
        startActivity(scoresIntent);
    }

    private void showSettings() {
        Intent showSettingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(showSettingsIntent);
    }

    private List<String> listMaps() {
        ArrayList<String> retVal = new ArrayList<>();

        if(getBaseContext().getFileStreamPath(MAP_LIST_FILE_NAME).exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(MAP_LIST_FILE_NAME)))) {
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String mapName = ((TextView)view).getText().toString();
        Intent startGameIntent = new Intent(this, GameActivity.class);

        startGameIntent.putExtra(MAP_NAME_KEY, mapName);
        startActivityForResult(startGameIntent, START_GAME_REQUEST_CODE);
    }

    public void deleteMap(String mapName) {
        deleteFile(mapName);

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput(MAP_LIST_FILE_NAME)));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(mapName, MODE_APPEND)))) {

            String line;
            while((line = reader.readLine()) != null) {
                line = line.trim();
                if(!mapName.equals(line)) {
                    writer.write(line);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        File tempFile = getFileStreamPath(mapName);
        tempFile.renameTo(getFileStreamPath(MAP_LIST_FILE_NAME));

        new DBHelper(this).deleteMap(mapName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK && requestCode == NEW_MAP_REQUEST_CODE) {
            String newMap = data.getStringExtra(NewMapController.KEY_MAP_NAME);
            if(newMap != null) {
                mapsListViewAdapter.add(newMap);
            }
        }
    }
}
