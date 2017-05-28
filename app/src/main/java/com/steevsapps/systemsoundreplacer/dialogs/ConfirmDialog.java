package com.steevsapps.systemsoundreplacer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.steevsapps.systemsoundreplacer.R;

public class ConfirmDialog extends DialogFragment {
    private final static String ARG_TITLE = "ARG_TITLE";
    private final static String ARG_MSG = "ARG_MSG";
    private final static String ARG_TAG = "ARG_TAG";

    private String mTitle;
    private String mMsg;
    private String mTag;

    private DialogListener mListener;

    public interface DialogListener {
        void onYesClicked(String tag);
    }

    public static ConfirmDialog newInstance(String title, String msg, String tag) {
        ConfirmDialog fragment = new ConfirmDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MSG, msg);
        args.putString(ARG_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mTitle = args.getString(ARG_TITLE);
        mMsg = args.getString(ARG_MSG);
        mTag = args.getString(ARG_TAG);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (DialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DialogListener!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(mTitle)
                .setMessage(mMsg)
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onYesClicked(mTag);
                    }
                })
                .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        return builder.create();
    }
}
