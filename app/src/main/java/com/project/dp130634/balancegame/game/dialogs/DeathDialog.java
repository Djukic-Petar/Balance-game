package com.project.dp130634.balancegame.game.dialogs;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.project.dp130634.balancegame.R;

/**
 * Created by John on 04-Sep-17.
 */

public class DeathDialog implements DialogInterface.OnCancelListener {

    public interface DeathDialogListener {
        void onDialogClose(boolean restart);
    }

    AppCompatActivity activity;
    AlertDialog alertDialog;
    DeathDialogListener deathDialogListener;

    public DeathDialog(AppCompatActivity activity, DeathDialogListener deathDialogListener) {
        this.activity = activity;
        this.deathDialogListener = deathDialogListener;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setView(R.layout.dialog_death);
        dialogBuilder.setNegativeButton(R.string.backToMain, dialogClickListener);
        dialogBuilder.setPositiveButton(R.string.retry, dialogClickListener);
        alertDialog = dialogBuilder.create();
    }

    public void showDialog() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.show();
            }
        });
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        deathDialogListener.onDialogClose(false);
    }

    private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    deathDialogListener.onDialogClose(true);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    deathDialogListener.onDialogClose(false);
                    break;
            }
        }
    };
}
