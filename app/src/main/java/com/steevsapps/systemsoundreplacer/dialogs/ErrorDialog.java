package com.steevsapps.systemsoundreplacer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.steevsapps.systemsoundreplacer.R;

public class ErrorDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.error_dialog_title)
                .setMessage(R.string.error_dialog_message);
        return builder.create();
    }

}
