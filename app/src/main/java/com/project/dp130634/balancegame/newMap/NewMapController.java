package com.project.dp130634.balancegame.newMap;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.project.dp130634.balancegame.GameField;
import com.project.dp130634.balancegame.MainActivity;
import com.project.dp130634.balancegame.ViewInterface;
import com.project.dp130634.balancegame.scores.DBHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

/**
 * Created by John on 23-Aug-17.
 */

public class NewMapController implements SaveMapDialog.DialogCloseListener {

    public static final String KEY_MAP_NAME = "com.project.dp130634.balancegame.newMap.mapName";

    public enum Element {START_POSITION, VICTORY_HOLE, DEATH_HOLE, WALL}

    private Element currentElement = Element.START_POSITION;
    private NewMapModel newMapModel;
    private ViewInterface viewInterface;
    private SaveMapDialog saveMapDialog;
    private AppCompatActivity activity;

    public NewMapController(AppCompatActivity activity, ViewInterface viewInterface) {
        newMapModel = new NewMapModel();
        this.viewInterface = viewInterface;
        saveMapDialog = new SaveMapDialog(activity, this);
        this.activity = activity;
    }

    @Override
    public void onDialogClose(String mapName, GameField map) {
        if(mapName != null) {
            if(map.hasRequiredElements()) {
                saveMap(mapName, map);
                Intent returnIntent = new Intent();
                returnIntent.putExtra(KEY_MAP_NAME, mapName);
                viewInterface.goBack(returnIntent);
            }
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        boolean outcome = false;
        switch (currentElement) {
            case START_POSITION:
                if(newMapModel.hasStartPosition()) {
                    outcome = newMapModel.replaceStartPosition(event.getX(), event.getY());
                } else {
                    outcome = newMapModel.putStartPosition(event.getX(), event.getY());
                }
                break;

            case VICTORY_HOLE:
                if(newMapModel.hasVictoryHole()) {
                    outcome = newMapModel.replaceVictoryHole(event.getX(), event.getY());
                } else {
                    outcome = newMapModel.putVictoryHole(event.getX(), event.getY());
                }
                break;

            case DEATH_HOLE:
                outcome = newMapModel.addDeathHole(event.getX(), event.getY());
                break;

            case WALL:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        outcome = newMapModel.createTempWall(event.getX(), event.getY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(newMapModel.hasTempWall()) {
                            outcome = newMapModel.extendTempWall(event.getX(), event.getY());
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if(newMapModel.hasTempWall()) {
                            outcome = newMapModel.addWall();
                        }
                        break;
                }
                break;

        }
        if(outcome) {
            viewInterface.refresh(newMapModel);
        }
        return outcome;
    }

    public boolean hasRequiredElements() {
        return newMapModel.hasRequiredElements();
    }

    public void showSaveDialog() {
        saveMapDialog.showDialog(newMapModel);
    }

    public void setCurrentElement(Element currentElement) {
        this.currentElement = currentElement;
    }

    public NewMapModel getModel() {
        return newMapModel;
    }

    private void saveMap(String mapName, GameField map) {
        try(ObjectOutputStream oos = new ObjectOutputStream(activity.openFileOutput(mapName, Context.MODE_PRIVATE))){
            oos.writeObject(map);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try(PrintWriter out = new PrintWriter(activity.openFileOutput(MainActivity.MAP_LIST_FILE_NAME, Context.MODE_APPEND))){
            out.println(mapName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new DBHelper(activity).addMap(mapName);
    }
}
