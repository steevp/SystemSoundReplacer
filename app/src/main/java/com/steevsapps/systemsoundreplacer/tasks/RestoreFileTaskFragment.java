package com.steevsapps.systemsoundreplacer.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.steevsapps.systemsoundreplacer.utils.Shell;

/**
 * This headless fragment retains RestoreFileTask across configuration changes
 */
public class RestoreFileTaskFragment extends Fragment {
    private final static String TAG = "com.steevsapps.TAG";

    private final static String RESTORE_FILE = "RESTORE_FILE";
    private final static String SU_HELPER = "SU_HELPER";

    private String mSuHelper; // Helper script path
    private String mRestoreFile; // File to restore

    private TaskCallbacks mCallbacks;
    private RestoreFileTask mTask;

    public static RestoreFileTaskFragment newInstance(String suhelper, String restoreFile) {
        RestoreFileTaskFragment fragment = new RestoreFileTaskFragment();
        Bundle args = new Bundle();
        args.putString(SU_HELPER, suhelper);
        args.putString(RESTORE_FILE, restoreFile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallbacks = (TaskCallbacks) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement TaskCallbacks!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Avoid leaking activity
        mCallbacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain instance across configuration changes
        setRetainInstance(true);

        Bundle args = getArguments();
        mSuHelper = args.getString(SU_HELPER);
        mRestoreFile = args.getString(RESTORE_FILE);

        // Start the AsyncTask
        mTask = new RestoreFileTask();
        mTask.execute();
    }

    private class RestoreFileTask extends AsyncTask<Void,Void,Boolean> {
        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String cmd = String.format("/system/bin/sh %s restore \"%s\"",
                    mSuHelper, mRestoreFile);
            Log.i(TAG, cmd);
            try {
                Shell.Result result = Shell.runAsRoot(cmd);
                Log.i(TAG, result.output);
                if (result.returnCode == 0) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (mCallbacks != null) {
                mCallbacks.onPostExecute(result);
            }
        }
    }
}
