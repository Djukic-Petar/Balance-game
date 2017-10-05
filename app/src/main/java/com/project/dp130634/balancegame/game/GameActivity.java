package com.project.dp130634.balancegame.game;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.project.dp130634.balancegame.GameFieldImageView;
import com.project.dp130634.balancegame.MainActivity;
import com.project.dp130634.balancegame.Model;
import com.project.dp130634.balancegame.R;
import com.project.dp130634.balancegame.ViewInterface;

public class GameActivity extends AppCompatActivity implements ViewInterface, SensorEventListener, GameController.GameRestartListener {

    public static final String RESULT_KEY = "com.project.dp130634.balancegame.gameresult";
    public static final int RESULT_ERROR = -1;
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILURE = 1;

    private Sensor accelerometer;
    private SensorManager sensorManager;
    private GameFieldImageView gameFieldImageView;
    private GameController controller;
    private String mapName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        mapName = getIntent().getStringExtra(MainActivity.MAP_NAME_KEY);
        controller = new GameController(this, this, this, mapName);
        gameFieldImageView = (GameFieldImageView)findViewById(R.id.gameFieldImageView);
        gameFieldImageView.setGameField(controller.getModel());


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        if(accelerometer == null) {
            Toast.makeText(this, R.string.unsupportedDevice, Toast.LENGTH_LONG);

            //setResult(Activity.RESULT_CANCELED, new Intent().putExtra(RESULT_KEY, RESULT_ERROR));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        controller.back();
        super.onBackPressed();
    }

    @Override
    public void refresh(Model model) {
        if(model instanceof GameModel) {
            gameFieldImageView.invalidate();
        } else {
            throw new IllegalArgumentException("Model must match activity");
        }
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void goBack(Intent returnIntent) {
        throw new UnsupportedOperationException("This activity shouldn't return an intent! Use goBack()");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        controller.angleChanged(event.values[0], event.values[1], event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void restart() {
        controller = new GameController(this, this, this, mapName);
        gameFieldImageView.setGameField(controller.getModel());
    }
}
