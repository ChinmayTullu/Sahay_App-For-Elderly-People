package com.sahay;

import android.app.Activity;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

public class Utils {
    protected static void displayAlertDialog(Context context, String msg, String title) {
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setMessage(msg)
                .setCancelable(false)
                .setNeutralButton("Ok", (dialog, which) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.setTitle(title);
        alert.show();
    }

    protected static AlertDialog displayProgressBarCircular(Context context, String msg, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);

        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(progressBar);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) progressBar.getLayoutParams();
        params.setMargins(20, 20, 20, 50);
        progressBar.setLayoutParams(params);

        builder.setView(layout)
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    protected static void confirmExitAppAlertDialog(Context context, String msg, String title)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> System.exit(0));
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.setTitle(title);
        alert.show();
    }

    protected static void confirmLogoutAlertDialog(Activity activity, String msg, String title)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> activity.finish());

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.setTitle(title);
        alert.show();
    }
}
