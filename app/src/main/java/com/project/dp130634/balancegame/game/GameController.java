package com.project.dp130634.balancegame.game;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.project.dp130634.balancegame.GameField;
import com.project.dp130634.balancegame.R;
import com.project.dp130634.balancegame.ViewInterface;
import com.project.dp130634.balancegame.game.dialogs.DeathDialog;
import com.project.dp130634.balancegame.game.dialogs.VictoryDialog;
import com.project.dp130634.balancegame.newMap.NewMapModel;
import com.project.dp130634.balancegame.scores.DBHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by John on 25-Aug-17.
 */

class GameController extends Thread implements DeathDialog.DeathDialogListener, VictoryDialog.ScoreDialogListener {

    public interface GameRestartListener {
        void restart();
    }

    private static final int TICK_RATE = 128;

    public class Vector {
        public float x;
        public float y;

        public Vector(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public Vector() {
            x = 0;
            y = 0;
        }
    }

    public class Vector3d {
        public float x;
        public float y;
        public float z;

        public Vector3d(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vector3d() {
            x = 0;
            y = 0;
            z = 0;
        }
    }

    private GameModel model;
    private ViewInterface viewInterface;
    private GameRestartListener restartListener;
    private AppCompatActivity activity;
    private String mapName;

    private Vector velocity;
    private Vector acceleration;
    private Vector tiltAcceleration;
    private float normalForceAcceleration;
    private Vector frictionForce;
    private Vector frictionAcceleration;
    private Vector3d tilt;
    private GameField.Hole ball;
    private GameModel.Encounter previousEncounter = GameModel.Encounter.NOTHING;
    private GameModel.Encounter currentEncounter = GameModel.Encounter.NOTHING;

    private float frictionCoefficient;
    private float ballMass;
    private float ballElasticity;

    private boolean gameOver;
    private DeathDialog deathDialog;
    private VictoryDialog victoryDialog;
    private long startTime;

    public GameController(AppCompatActivity activity, ViewInterface viewInterface, GameRestartListener restartListener, String mapName) {
        this.activity = activity;
        this.viewInterface = viewInterface;
        this.restartListener = restartListener;
        this.mapName = mapName;

        try(ObjectInputStream ois = new ObjectInputStream(activity.openFileInput(mapName))) {
            NewMapModel field = (NewMapModel) ois.readObject();
            model = new GameModel(field);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        frictionCoefficient = Float.parseFloat(sharedPrefs.getString("frictionPreference", "40")) / 100;
        ballMass =  ((float)Integer.parseInt(sharedPrefs.getString("massPreference", "20"))) / 50;
        ballElasticity = Float.parseFloat(sharedPrefs.getString("elasticityPreference", "80")) / 100;

        ball = model.getPlayingBall();
        velocity = new Vector();
        acceleration = new Vector();
        tiltAcceleration = new Vector();
        tilt = new Vector3d();

        gameOver = false;
        deathDialog = new DeathDialog(activity, this);
        victoryDialog = new VictoryDialog(activity, this);
        startTime = SystemClock.uptimeMillis();
        start();
    }

    @Override
    public void run() {
        int debugPrint = 0;
        while(!gameOver) {
            if(debugPrint++ % 20 == 0) {
                System.out.println("printing. Ball pos " + ball.getCenter().x + ", " + ball.getCenter().y);
                System.out.println("Starting hole pos " + model.getStartHole().getCenter().x + ", " + model.getStartHole().getCenter().y);
            }

            try {
                Thread.sleep(1000/TICK_RATE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentEncounter = calculateVectors();
            if(currentEncounter != previousEncounter && (currentEncounter == GameModel.Encounter.WALL_DOWN || currentEncounter == GameModel.Encounter.WALL_LEFT || currentEncounter == GameModel.Encounter.WALL_RIGHT || currentEncounter == GameModel.Encounter.WALL_UP)) {
                playBounceSound();
            }
            if(currentEncounter == GameModel.Encounter.VICTORY) {
                playVictorySound();
                gameOver = true;
            }
            if(currentEncounter == GameModel.Encounter.DEATH) {
                playDeathSound();
                gameOver = true;
            }
            previousEncounter = currentEncounter;
            activity.runOnUiThread(viewRefresher);
        }

        if(currentEncounter == GameModel.Encounter.VICTORY) {
            long playingTime = SystemClock.uptimeMillis() - startTime;
            victoryDialog.showDialog(playingTime);
        } else if(currentEncounter == GameModel.Encounter.DEATH) {
            deathDialog.showDialog();
        }

    }

    public synchronized void back() {
        currentEncounter = GameModel.Encounter.NOTHING;
        gameOver = true;
    }

    public GameModel getModel() {
        return this.model;
    }


    public synchronized void angleChanged(float xAngle, float yAngle, float zAngle) {
        tilt.x = -xAngle;   //Sensor coordinate system orientation does not match the model's
        tilt.y = yAngle;
        tilt.z = zAngle;
    }

    private synchronized GameModel.Encounter calculateVectors() {
        Vector velocityNewComponent = new Vector(acceleration.x / TICK_RATE, acceleration.y / TICK_RATE);
        velocity = resultantVector(velocity, velocityNewComponent);
        correctVelocityForWallBlock();

        GameModel.Encounter ballEncounter = moveBall(velocity.x / TICK_RATE, velocity.y / TICK_RATE);
        switch (ballEncounter) {
            case DEATH:
                model.setPlayingBall(null);
                return GameModel.Encounter.DEATH;
            case VICTORY:
                model.setPlayingBall(null);
                return GameModel.Encounter.VICTORY;
            case WALL_UP:
                bounceDown();
                break;
            case WALL_DOWN:
                bounceUp();
                break;
            case WALL_LEFT:
                bounceRight();
                break;
            case WALL_RIGHT:
                bounceLeft();
        }

        tiltAcceleration.x = tilt.x;
        tiltAcceleration.y = tilt.y;
        normalForceAcceleration = tilt.z;
        magnifyAcceleration(40);
        frictionForce = calculateFrictionForce();
        frictionAcceleration = calculateFrictionAcceleration(frictionForce);
        acceleration = resultantVector(tiltAcceleration, frictionAcceleration);

        //previousEncounter = ballEncounter;
        return ballEncounter;
    }

    private float getVectorIntensity(Vector vector) {
        return (float)Math.sqrt(Math.pow(vector.x, 2) + Math.pow(vector.y, 2));
    }

    private Vector resultantVector(Vector vec1, Vector vec2) {
        return new Vector(vec1.x + vec2.x, vec1.y + vec2.y);
        /*
        Vector retVal = new Vector();
        float vec1Intensity = getVectorIntensity(vec1);
        float vec2Intensity = getVectorIntensity(vec2);

        if(vec1Intensity == 0) {
            return vec2;
        } else if(vec2Intensity == 0) {
            return vec1;
        }

        float angleCosine = Math.abs(vec1.x * vec2.x + vec1.y * vec2.y) / (vec1Intensity * vec2Intensity);
        float congruentAngle = (float)(Math.PI - Math.acos(angleCosine));


        float resultantIntensity = (float)Math.sqrt(Math.pow(vec1Intensity, 2) + Math.pow(vec2Intensity, 2) - 2 * vec1Intensity * vec2Intensity * Math.cos(congruentAngle));

        float resultantAngleInParallelogramCosine = (float)(Math.pow(vec1Intensity, 2) + Math.pow(resultantIntensity, 2) - Math.pow(vec2Intensity, 2)) / (2 * vec1Intensity * resultantIntensity);
        if(resultantAngleInParallelogramCosine > 1) {
            resultantAngleInParallelogramCosine = 1;
        }
        float resultantAngleInParallelogram = (float)Math.acos(resultantAngleInParallelogramCosine);

        float vector1AngleCosine;
        if(vec1.x == 0){
            vector1AngleCosine = 1;
        }else {
            vector1AngleCosine = (float) (Math.pow(vec1Intensity, 2) + Math.pow(vec1.x, 2) - Math.pow(vec1.y, 2)) / (2 * vec1Intensity * vec1.x);
        }
        if(vector1AngleCosine > 1) {
            vector1AngleCosine = 1;
        }
        float vector1Angle = (float)Math.acos(vector1AngleCosine);
        float absoluteResultantAngle = (float)((resultantAngleInParallelogram + vector1Angle) % (Math.PI * 2));

        retVal.y = (float)(resultantIntensity * Math.sin(absoluteResultantAngle));
        retVal.x = (float)Math.sqrt(Math.pow(resultantIntensity, 2) - Math.pow(retVal.y, 2));
        return retVal;
        */
    }

    private GameModel.Encounter moveBall(float x, float y) {
        return model.moveBall(x, y);
    }

    private Vector calculateFrictionForce() {
        Vector retVal = new Vector();

        float normalForceIntensity = Math.abs(normalForceAcceleration * ballMass);
        float frictionIntensity = normalForceIntensity * frictionCoefficient;

        Vector frictionDirection;
        if(getVectorIntensity(velocity) > 0) {
            frictionDirection = new Vector(-velocity.x, -velocity.y);
        } else {
            frictionDirection = new Vector(1,1);//Unimportant, just don't be zero
        }

        float frictionAngleSine = frictionDirection.y / getVectorIntensity(frictionDirection);

        retVal.y = frictionIntensity * frictionAngleSine;
        retVal.x = (float)Math.sqrt(Math.pow(frictionIntensity, 2) - Math.pow(retVal.y, 2));
        if(frictionDirection.x < 0) {
            retVal.x = -retVal.x;
        }
        return retVal;
    }

    private Vector calculateFrictionAcceleration(Vector frictionForce) {
        Vector retVal = new Vector();

        float velocityIntensity = getVectorIntensity(velocity);
        float tiltAccelerationIntensity = getVectorIntensity(tiltAcceleration);
        float frictionAccelerationIntensity = getVectorIntensity(frictionForce) / ballMass;
        if(velocityIntensity == 0) {
            if(tiltAccelerationIntensity > frictionAccelerationIntensity) {
                retVal.x = frictionForce.x / ballMass;
                retVal.y = frictionForce.y / ballMass;
            } else {
                retVal.x = -tiltAcceleration.x;
                retVal.y = -tiltAcceleration.y;
            }
        } else {
            retVal.x = frictionForce.x / ballMass;
            retVal.y = frictionForce.y / ballMass;
        }


        return retVal;
    }

    private void magnifyAcceleration(int times) {
        tiltAcceleration.x *= times;
        tiltAcceleration.y *= times;
        normalForceAcceleration *= times;
    }

    private void bounceDown() {
        if(previousEncounter != GameModel.Encounter.WALL_UP) {
            velocity.x *= ballElasticity;
            velocity.y *= -ballElasticity;
        }
    }

    private void bounceUp() {
        if(previousEncounter != GameModel.Encounter.WALL_DOWN) {
            velocity.x *= ballElasticity;
            velocity.y *= -ballElasticity;
        }
    }

    private void bounceRight() {
        if(previousEncounter != GameModel.Encounter.WALL_LEFT) {
            velocity.x *= -ballElasticity;
            velocity.y *= ballElasticity;
        }
    }

    private void bounceLeft() {
        if(previousEncounter != GameModel.Encounter.WALL_RIGHT) {
            velocity.x *= -ballElasticity;
            velocity.y *= ballElasticity;
        }
    }

    private void correctVelocityForWallBlock() {
        if(velocity.x > 0 && model.isRightBlocked()) {
            velocity.x = 0;
        }
        if(velocity.x < 0 && model.isLeftBlocked()) {
            velocity.x = 0;
        }
        if(velocity.y > 0 && model.isBottomBlocked()) {
            velocity.y = 0;
        }
        if(velocity.y < 0 && model.isTopBlocked()) {
            velocity.y = 0;
        }
    }

    private void playBounceSound() {
        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(activity, R.raw.bounce);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mediaPlayer.start();
    }

    private void playVictorySound() {
        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(activity, R.raw.victory);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mediaPlayer.setVolume(0.2f, 0.2f);
        mediaPlayer.start();
    }

    private void playDeathSound() {
        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(activity, R.raw.death);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mediaPlayer.setVolume(0.2f, 0.2f);
        mediaPlayer.start();
    }

    private Runnable viewRefresher = new Runnable() {
        @Override
        public void run() {
            viewInterface.refresh(model);
        }
    };

    @Override
    public void onDialogClose(boolean restart) {
        if (restart) {
            restartListener.restart();
        } else {
            //return to main menu
//            Intent returnIntent = new Intent();
//            returnIntent.putExtra("result", GameActivity.RESULT_FAILURE);

            viewInterface.goBack();
        }
    }

    @Override
    public void onScoreSave(String name, long time) {
        if(name != null) {
            new DBHelper(activity).addScore(name, time, mapName);
        }
        viewInterface.goBack();
    }
}
