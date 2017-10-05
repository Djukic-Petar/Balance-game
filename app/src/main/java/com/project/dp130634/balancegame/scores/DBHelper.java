package com.project.dp130634.balancegame.scores;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by John on 05-Sep-17.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "Scores.db";

    public static final int DB_VERSION = 1;

    private static final String CREATE_MAPS_TABLE =
            "CREATE TABLE MAPS " +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT UNIQUE NOT NULL);";

    private static final String CREATE_SCORES_TABLE =
            "CREATE TABLE SCORES " +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "score INTEGER NOT NULL, " +
            "map INTEGER NOT NULL, " +
            "FOREIGN KEY(map) REFERENCES MAPS(_id) " +
            "ON UPDATE CASCADE " +
            "ON DELETE CASCADE);";

    private static final String DROP_SCORES =
            "DROP TABLE IF EXISTS SCORES;";

    private static final String DROP_MAPS =
            "DROP TABLE IF EXISTS MAPS;";

    private static final String ADD_MAP =
            "INSERT INTO MAPS(name) VALUES (?);";

    private static final String ADD_SCORE =
            "INSERT INTO SCORES(name, score, map) VALUES (?, ?, ?);";

    private static final String FIND_MAP_ID =
            "SELECT _id FROM MAPS where name = ?;";

    private static final String FIND_MAP_SCORES =
            "SELECT _id, name, score " +
            "FROM SCORES " +
            "WHERE map = ? " +
            "ORDER BY score ASC;";

    private static final String CLEAR_MAP_SCORES =
            "DELETE FROM SCORES " +
                    "WHERE map = ?;";

    private static final String DELETE_MAP =
            "DELETE FROM MAPS " +
                    "WHERE name = ?;";


    public DBHelper(Context activity) {
        super(activity, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MAPS_TABLE);
        db.execSQL(CREATE_SCORES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_SCORES);
        db.execSQL(DROP_MAPS);
        onCreate(db);
    }

    public Cursor selectScoresForMap(String mapName) {
        SQLiteDatabase db = getReadableDatabase();
        String mapNameArg[] = {mapName};
        Cursor mapIdCursor = db.rawQuery(FIND_MAP_ID, mapNameArg);
        mapIdCursor.moveToFirst();
        int mapId = mapIdCursor.getInt(0);

        String scoresArg[] = {""+mapId};
        return db.rawQuery(FIND_MAP_SCORES, scoresArg);
    }

    public void addMap(String mapName) {
        SQLiteDatabase db = getWritableDatabase();
        String[] mapNameArg = {mapName};
        db.execSQL(ADD_MAP, mapNameArg);
    }

    public void addScore(String name, long score, String mapName){
        SQLiteDatabase db = getWritableDatabase();
        String mapNameArg[] = {mapName};
        Cursor mapIdCursor = db.rawQuery(FIND_MAP_ID, mapNameArg);
        mapIdCursor.moveToFirst();
        int mapId = mapIdCursor.getInt(0);

        Object[] args = {name, score, mapId};
        db.execSQL(ADD_SCORE, args);
    }

    public void clearAllScores() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(DROP_SCORES);
        db.execSQL(CREATE_SCORES_TABLE);
    }

    public void clearScores(String mapName) {
        SQLiteDatabase db = getWritableDatabase();

        String mapNameArg[] = {mapName};
        Cursor mapIdCursor = db.rawQuery(FIND_MAP_ID, mapNameArg);
        mapIdCursor.moveToFirst();
        int mapId = mapIdCursor.getInt(0);

        String mapIdArg[] = {""+mapId};
        db.execSQL(CLEAR_MAP_SCORES, mapIdArg);
    }

    public void deleteMap(String mapName) {
        SQLiteDatabase db = getWritableDatabase();
        String mapNameArg[] = {mapName};
        db.execSQL(DELETE_MAP, mapNameArg);
    }

    public void clearDB() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(DROP_SCORES);
        db.execSQL(DROP_MAPS);
        onCreate(db);
    }
}
