package com.project.dp130634.balancegame.game.dialogs;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import com.project.dp130634.balancegame.R;

/**
 * Created by John on 04-Sep-17.
 */

public class VictoryDialog implements DialogInterface.OnCancelListener {

    public interface ScoreDialogListener {
        void onScoreSave(String name, long time);
    }

    private AppCompatActivity activity;
    private AlertDialog alertDialog;
    private ScoreDialogListener scoreDialogListener;
    private long time;

    public VictoryDialog(AppCompatActivity activity, ScoreDialogListener scoreDialogListener) {
        this.activity = activity;
        this.scoreDialogListener = scoreDialogListener;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.activity);
        dialogBuilder.setView(R.layout.dialog_victory);
        dialogBuilder.setPositiveButton(R.string.save, dialogClickListener);
        dialogBuilder.setNegativeButton(R.string.cancel, dialogClickListener);
        dialogBuilder.setOnCancelListener(this);
        alertDialog = dialogBuilder.create();
    }

    public void showDialog(long time) {
        this.time = time;
        activity.runOnUiThread(new DialogStarter(time));
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        scoreDialogListener.onScoreSave(null, time);
    }

    private class DialogStarter implements Runnable {

        public DialogStarter(long time) {
            this.time = time;
        }

        @Override
        public void run() {
            alertDialog.show();
            String formattedTime = formatTime();
            ((TextView)alertDialog.findViewById(R.id.victoryDialogTime)).setText(formattedTime);
        }

        private long time;
        private String formatTime() {
            long ms = time % 1000;
            long s = (time / 1000) % 60;
            long m = time / (1000 * 60);
            return String.format("%02d:%02d.%02d", m, s, ms);
        }
    }

    private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:
                    String name = ((EditText)(alertDialog.findViewById(R.id.victoryDialogName))).getText().toString();
                    scoreDialogListener.onScoreSave(name, time);
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    scoreDialogListener.onScoreSave(null, time);
                    break;
            }
        }
    };
}
