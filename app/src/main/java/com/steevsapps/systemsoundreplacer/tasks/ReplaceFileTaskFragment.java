package com.steevsapps.systemsoundreplacer.tasks;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.steevsapps.systemsoundreplacer.utils.Shell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This headless fragment retains ReplaceFileTask across configuration changes
 */
public class ReplaceFileTaskFragment extends Fragment {
    private final static String TAG = "com.steevsapps.TAG";

    private final static String ORIGINAL = "ORIGINAL";
    private final static String REPLACEMENT = "REPLACEMENT";
    private final static String SU_HELPER = "SU_HELPER";

    private String mSuHelper; // Helper script path
    private String mOriginal; // File to replace
    private Uri mReplacement; // Replacement file
    private TaskCallbacks mCallbacks;
    private ReplaceFileTask mTask;

    public static ReplaceFileTaskFragment newInstance(String suhelper, String original, Uri replacement) {
        ReplaceFileTaskFragment fragment = new ReplaceFileTaskFragment();
        Bundle args = new Bundle();
        args.putString(SU_HELPER, suhelper);
        args.putString(ORIGINAL, original);
        args.putParcelable(REPLACEMENT, replacement);
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
        mOriginal = args.getString(ORIGINAL);
        mReplacement = args.getParcelable(REPLACEMENT);

        // Start the AsyncTask
        mTask = new ReplaceFileTask();
        mTask.execute();
    }

    /**
     * Task to replace a system sound
     */
    private class ReplaceFileTask extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // Retrieve file from Content Provider first
            InputStream input = null;
            FileOutputStream output = null;
            File tmpFile = null;
            try {
                final Activity activity = getActivity();
                if (activity == null) {
                    return false;
                }
                input = activity.getContentResolver().openInputStream(mReplacement);
                tmpFile = File.createTempFile("fileToCopy", ".ogg", activity.getExternalCacheDir());
                output = new FileOutputStream(tmpFile);

                byte[] buffer = new byte[4 * 1024];
                int read;

                if (input != null) {
                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                }
                output.flush();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Now replace the file
            String cmd = String.format("/system/bin/sh %s replace \"%s\" \"%s\"",
                    mSuHelper, mOriginal, tmpFile.getAbsolutePath());
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
