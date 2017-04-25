package com.steevsapps.systemsoundreplacer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.steevsapps.systemsoundreplacer.R;

public class ConfirmDialog extends DialogFragment {
    public final static String ARG_TITLE = "ARG_TITLE";
    public final static String ARG_MSG = "ARG_MSG";

    private DialogListener mListener;

    public interface DialogListener {
        void onYesClicked();
    }

    public static ConfirmDialog newInstance(String title, String msg) {
        ConfirmDialog fragment = new ConfirmDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MSG, msg);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString(ARG_TITLE))
                .setMessage(getArguments().getString(ARG_MSG))
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onYesClicked();
                    }
                })
                .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        return builder.create();
    }

    public void setListener(DialogListener listener) {
        mListener = listener;
    }

}
