package com.lobxy.achs.User.Utils;

import android.app.Activity;
import android.app.ProgressDialog;

public class mProgressDialog {
    Activity context;

    public mProgressDialog(Activity context) {
        this.context = context;
    }

    public void progressdialog(Integer cmd) {
        // 1 for show and 0 for dismiss

        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Working...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);

        if (cmd == 1) {
            dialog.show();
        } else{
            dialog.dismiss();
        }
    }

}
