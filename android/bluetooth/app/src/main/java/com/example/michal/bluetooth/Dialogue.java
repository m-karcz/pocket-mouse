package com.example.michal.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;


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

    void toast(int resId)
    {
        Context context = activity.getApplicationContext();
        CharSequence text = activity.getText( resId );
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
