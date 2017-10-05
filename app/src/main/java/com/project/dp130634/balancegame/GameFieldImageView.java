package com.project.dp130634.balancegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import java.util.List;

/**
 * Created by John on 21-Aug-17.
 */

public class GameFieldImageView extends AppCompatImageView {

    private Paint paint;
    GameField gameField;

    public GameFieldImageView(Context context) {
        super(context);
        paint = new Paint();
    }

    public GameFieldImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
    }

    public GameFieldImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
    }

    public void setGameField(GameField gameField) {
        this.gameField = gameField;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        GameField.Hole startHole = gameField.getStartHole();
        if(startHole != null) {
            paint.setColor(Color.CYAN);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(startHole.getCenter().x, startHole.getCenter().y, gameField.getHoleRadius(), paint);
        }

        GameField.Hole endHole = gameField.getEndHole();
        if(endHole != null) {
            paint.setColor(Color.MAGENTA);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(endHole.getCenter().x, endHole.getCenter().y, gameField.getHoleRadius(), paint);
        }

        List<GameField.Hole> deathHoles = gameField.getDeathHoles();
        for(int i = 0; i < deathHoles.size(); i++) {
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            GameField.Hole deathHole = deathHoles.get(i);
            canvas.drawCircle(deathHole.getCenter().x, deathHole.getCenter().y, gameField.getHoleRadius(), paint);
        }

        if(gameField.getTempWallHorizontal() != null) {
            GameField.Wall wall = gameField.getTempWallHorizontal();
            Rect rect = new Rect();
            if(wall.getStartPoint().y == wall.getEndPoint().y) {
                int left = (int) (wall.getStartPoint().x < wall.getEndPoint().x ? wall.getStartPoint().x : wall.getEndPoint().x);
                int right = (int) (wall.getStartPoint().x < wall.getEndPoint().x ? wall.getEndPoint().x : wall.getStartPoint().x);
                int top = (int) (wall.getStartPoint().y - gameField.getWallWidth() / 2);
                int bottom = (int) (wall.getStartPoint().y + gameField.getWallWidth() / 2);

                rect.set(left, top, right, bottom);
            } else {
                int top = (int) (wall.getStartPoint().y < wall.getEndPoint().y ? wall.getStartPoint().y : wall.getEndPoint().y);
                int bottom = (int) (wall.getStartPoint().y < wall.getEndPoint().y ? wall.getEndPoint().y : wall.getStartPoint().y);
                int left = (int) (wall.getStartPoint().x - gameField.wallWidth / 2);
                int right = (int) (wall.getStartPoint().x + gameField.wallWidth / 2);

                rect.set(left, top, right, bottom);
            }
            paint.setColor(Color.GRAY);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rect, paint);
        } else if(gameField.getTempWallVertical() != null) {
            GameField.Wall wall = gameField.getTempWallVertical();
            Rect rect = new Rect();
            if(wall.getStartPoint().y == wall.getEndPoint().y) {
                int left = (int) (wall.getStartPoint().x < wall.getEndPoint().x ? wall.getStartPoint().x : wall.getEndPoint().x);
                int right = (int) (wall.getStartPoint().x < wall.getEndPoint().x ? wall.getEndPoint().x : wall.getStartPoint().x);
                int top = (int) (wall.getStartPoint().y - gameField.getWallWidth() / 2);
                int bottom = (int) (wall.getStartPoint().y + gameField.getWallWidth() / 2);

                rect.set(left, top, right, bottom);
            } else {
                int top = (int) (wall.getStartPoint().y < wall.getEndPoint().y ? wall.getStartPoint().y : wall.getEndPoint().y);
                int bottom = (int) (wall.getStartPoint().y < wall.getEndPoint().y ? wall.getEndPoint().y : wall.getStartPoint().y);
                int left = (int) (wall.getStartPoint().x - gameField.wallWidth / 2);
                int right = (int) (wall.getStartPoint().x + gameField.wallWidth / 2);

                rect.set(left, top, right, bottom);
            }
            paint.setColor(Color.GRAY);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rect, paint);
        }

        GameField.Hole playingBall = gameField.getPlayingBall();
        if(playingBall != null) {
            paint.setColor(Color.YELLOW);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(playingBall.getCenter().x, playingBall.getCenter().y, gameField.getHoleRadius(), paint);
        }

        List<GameField.Wall> walls = gameField.getWalls();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wall_texture);
        BitmapShader fillBMPshader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        Paint texturePaint = new Paint();
        texturePaint.setShader(fillBMPshader);
        for(int i = 0; i < walls.size(); i++) {
            /*
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            */
            Rect rect = new Rect();
            GameField.Wall wall = walls.get(i);
            if(wall.getStartPoint().y == wall.getEndPoint().y) {
                int left = (int) (wall.getStartPoint().x < wall.getEndPoint().x ? wall.getStartPoint().x : wall.getEndPoint().x);
                int right = (int) (wall.getStartPoint().x < wall.getEndPoint().x ? wall.getEndPoint().x : wall.getStartPoint().x);
                int top = (int) (wall.getStartPoint().y - gameField.getWallWidth() / 2);
                int bottom = (int) (wall.getStartPoint().y + gameField.getWallWidth() / 2);

                rect.set(left, top, right, bottom);
            } else {
                int top = (int) (wall.getStartPoint().y < wall.getEndPoint().y ? wall.getStartPoint().y : wall.getEndPoint().y);
                int bottom = (int) (wall.getStartPoint().y < wall.getEndPoint().y ? wall.getEndPoint().y : wall.getStartPoint().y);
                int left = (int) (wall.getStartPoint().x - gameField.wallWidth / 2);
                int right = (int) (wall.getStartPoint().x + gameField.wallWidth / 2);

                rect.set(left, top, right, bottom);
            }
            canvas.drawRect(rect, texturePaint);
        }
    }
}
