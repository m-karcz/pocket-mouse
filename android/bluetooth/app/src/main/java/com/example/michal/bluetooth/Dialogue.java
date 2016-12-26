package com.example.michal.bluetooth;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;


class Dialogue {
    private Activity activity;

    Dialogue(Activity ac)
    {
        activity = ac;
    }

    void exit_app(int resId)
    {//exits the app with message given by resId
        new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Error!")
                .setMessage(activity.getString(resId)+", press OK to close the application.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }

                })
                .show();
    }

    void popup(int resId)
    {//shows warning with message given by resId
        new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Warning!")
                .setMessage(activity.getString(resId))
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                })
                .show();
    }
}
