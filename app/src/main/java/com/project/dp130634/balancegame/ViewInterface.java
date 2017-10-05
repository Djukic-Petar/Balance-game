package com.project.dp130634.balancegame;


import android.content.Intent;

/**
 * Created by John on 23-Aug-17.
 */

public interface ViewInterface {
    void refresh(Model model);
    void goBack();
    void goBack(Intent returnIntent);
}
