package com.project.dp130634.balancegame.scores.util;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.project.dp130634.balancegame.R;

/**
 * Created by John on 07-Sep-17.
 */

public class ScoreCursorAdapter extends CursorAdapter {
    private LayoutInflater cursorLayoutInflater;

    public ScoreCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        cursorLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorLayoutInflater.inflate(R.layout.score_list_view_entry, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView)view.findViewById(R.id.scoreEntryName);
        String name = cursor.getString(cursor.getColumnIndex("name"));
        nameTextView.setText(name);

        TextView timeTextView = (TextView)view.findViewById(R.id.scoreEntryTime);
        long time = cursor.getLong(cursor.getColumnIndex("score"));
        String timeString = timeToString(time);
        timeTextView.setText(timeString);
    }

    private String timeToString(long time) {
        long ms = time % 1000;
        long s = (time / 1000) % 60;
        long m = time / (1000 * 60);
        return String.format("%02d:%02d.%02d", m, s, ms);
    }
}
