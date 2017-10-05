package com.project.dp130634.balancegame.newMap;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.project.dp130634.balancegame.GameField;
import com.project.dp130634.balancegame.R;

/**
 * Created by John on 24-Aug-17.
 */

public class SaveMapDialog implements DialogInterface.OnCancelListener {

    public interface DialogCloseListener{
        void onDialogClose(String mapName, GameField map);
    }

    private DialogCloseListener dialogCloseListener;
    private AppCompatActivity activity;
    private AlertDialog dialog;
    private GameField map;

    public SaveMapDialog(AppCompatActivity activity, DialogCloseListener dialogCloseListener) {
        this.activity = activity;
        this.dialogCloseListener = dialogCloseListener;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.activity);
        dialogBuilder.setView(R.layout.dialog_save_map);
        dialogBuilder.setPositiveButton(R.string.save, dialogClickListener);
        dialogBuilder.setNegativeButton(R.string.cancel, dialogClickListener);
        dialogBuilder.setOnCancelListener(this);
        dialog = dialogBuilder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        dialogCloseListener.onDialogClose(null, null);
    }

    private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:
                    String mapName = ((EditText)((AlertDialog)dialog).findViewById(R.id.dialogMapName)).getText().toString();
                    dialogCloseListener.onDialogClose(mapName, map);
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    dialogCloseListener.onDialogClose(null, null);
                    break;
            }
        }
    };

    public void showDialog(GameField map) {
        this.map = map;
        dialog.show();
        //activity.runOnUiThread(new DialogStarter());
    }

    /*
    private class DialogStarter implements Runnable {

        @Override
        public void run() {
            dialog.show();
        }
    }
    */
}
