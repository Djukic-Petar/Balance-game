package com.project.dp130634.balancegame.newMap;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.project.dp130634.balancegame.GameFieldImageView;
import com.project.dp130634.balancegame.Model;
import com.project.dp130634.balancegame.R;
import com.project.dp130634.balancegame.ViewInterface;

public class NewMapActivity extends AppCompatActivity implements ViewInterface {

    private NewMapController controller;
    private GameFieldImageView gameFieldImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_map);

        controller = new NewMapController(this, this);

        gameFieldImageView = (GameFieldImageView) findViewById(R.id.newMapImageView);
        gameFieldImageView.setGameField(controller.getModel());
        gameFieldImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return controller.onTouch(v, event);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_new_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.startingPositionMenuItem:
                controller.setCurrentElement(NewMapController.Element.START_POSITION);
                item.setChecked(true);
                return true;

            case R.id.victoryHoleMenuItem:
                controller.setCurrentElement(NewMapController.Element.VICTORY_HOLE);
                item.setChecked(true);
                return true;

            case R.id.deathHoleMenuItem:
                controller.setCurrentElement(NewMapController.Element.DEATH_HOLE);
                item.setChecked(true);
                return true;

            case R.id.wallMenuItem:
                controller.setCurrentElement(NewMapController.Element.WALL);
                item.setChecked(true);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveMap(View v) {
        if(controller.hasRequiredElements()) {
            controller.showSaveDialog();
        } else {
            Toast.makeText(this, R.string.noRequiredHoles, Toast.LENGTH_LONG).show();
        }
    }

    public void cancel(View v) {
        finish();
    }

    @Override
    public void refresh(Model model) {
        if(model instanceof NewMapModel) {
            gameFieldImageView.invalidate();
        } else {
            throw new IllegalArgumentException("Model must match activity");
        }
    }

    @Override
    public void goBack() {
        finish();
    }

    public void goBack(Intent returnIntent) {
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
