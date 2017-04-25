package com.steevsapps.systemsoundreplacer.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.steevsapps.systemsoundreplacer.R;
import com.steevsapps.systemsoundreplacer.adapters.SystemSoundAdapter;
import com.steevsapps.systemsoundreplacer.dialogs.ConfirmDialog;
import com.steevsapps.systemsoundreplacer.dialogs.ErrorDialog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SystemSoundAdapter.ItemClickedListener {
    public final static String TAG = "com.steevsapps.TAG";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private SystemSoundAdapter mAdapter;
    private String mSoundFolder = "/system/media/audio/ui/";
    private ProgressDialog mDialog;
    private MediaPlayer mMediaPlayer;
    private String mSelectedSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(mRecyclerView.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setMessage(getString(R.string.loading));

        mMediaPlayer = new MediaPlayer();

        // Extract the helper script
        File f = new File(getApplicationInfo().dataDir, "suhelper.sh");
        if (!f.exists()) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    extractHelper();
                }
            });
        }

        // Get System UI sounds
        listSoundFiles();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
    }

    private void extractHelper() {
        File of = new File(getApplicationInfo().dataDir, "suhelper.sh");
        InputStream input = getResources().openRawResource(R.raw.suhelper);
        FileOutputStream output = null;
        try {
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            output = new FileOutputStream(of);
            output.write(buffer);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
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
    }

    private void listSoundFiles() {
        File f = new File(mSoundFolder);
        File[] files = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String name = file.getName().toLowerCase();
                return name.endsWith(".ogg") && file.isFile();
            }
        });
        List<String> soundList = new ArrayList<>();
        for (File af: files) {
            soundList.add(af.getName());
        }
        mAdapter = new SystemSoundAdapter(this, soundList, mSoundFolder);
        mRecyclerView.setAdapter(mAdapter);
    }

    private boolean replaceFile(String original, String replacement) {
        String script = getApplicationInfo().dataDir + "/suhelper.sh";
        String cmd = String.format("/system/bin/sh %s replace \"%s\" \"%s\"",
                script, original, replacement);
        Log.i(TAG, cmd);
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes(cmd + "\n");
            dos.writeBytes("exit $?\n");
            dos.flush();
            dos.close();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            Log.i(TAG, result.toString());
            int returnCode = p.waitFor();
            if (returnCode == 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError();
        }
        return false;
    }

    private void showError() {
        ErrorDialog dialog = new ErrorDialog();
        dialog.show(getSupportFragmentManager(), "error_dialog");
    }

    private void showSuccess() {
        ConfirmDialog dialog = ConfirmDialog.newInstance(getString(R.string.success_dialog_title),
                getString(R.string.success_dialog_message));
        dialog.setListener(new ConfirmDialog.DialogListener() {
            @Override
            public void onYesClicked() {
                try {
                    Process p = Runtime.getRuntime().exec("su");
                    DataOutputStream dos = new DataOutputStream(p.getOutputStream());
                    dos.writeBytes("reboot\n");
                    dos.writeBytes("exit\n");
                    dos.flush();
                    dos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "success_dialog");
    }

    @Override
    public void playSound(String soundFile) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mSoundFolder + soundFile);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openSound(String soundFile) {
        mSelectedSound = mSoundFolder + soundFile;
        Intent intent = new Intent();
        intent.setType("application/ogg");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.file_chooser_title)), 123);
    }

    @Override
    public void askRestoreSound(final String soundFile) {
        mSelectedSound = mSoundFolder + soundFile;
        ConfirmDialog dialog = ConfirmDialog.newInstance(getString(R.string.confirm_dialog_title),
                "Are you sure you want to restore " + mSelectedSound + "?");
        dialog.setListener(new ConfirmDialog.DialogListener() {
            @Override
            public void onYesClicked() {
                // Restore the file
                new RestoreFileTask().execute(soundFile);
            }
        });
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    private boolean restoreSound(String soundFile) {
        mSelectedSound = mSoundFolder + soundFile;
        String script = getApplicationInfo().dataDir + "/suhelper.sh";
        String cmd = String.format("/system/bin/sh %s restore \"%s\"",
                script, mSelectedSound);
        Log.i(TAG, cmd);
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes(cmd + "\n");
            dos.writeBytes("exit $?\n");
            dos.flush();
            dos.close();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            Log.i(TAG, result.toString());
            int returnCode = p.waitFor();
            if (returnCode == 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            ConfirmDialog dialog = ConfirmDialog.newInstance(getString(R.string.confirm_dialog_title),
                    "Are you sure you want to replace " + mSelectedSound + "?");
            dialog.setListener(new ConfirmDialog.DialogListener() {
                @Override
                public void onYesClicked() {
                    new ReplaceFileTask().execute(data.getData());
                }
            });
            dialog.show(getSupportFragmentManager(), "dialog");
        }
    }

    private class ReplaceFileTask extends AsyncTask<Uri,Void,Boolean> {

        @Override
        protected void onPreExecute() {
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(Uri... params) {
            // Retrieve file from Content Provider first
            InputStream input = null;
            FileOutputStream output = null;
            File tmpFile = null;
            try {
                input = getContentResolver().openInputStream(params[0]);
                tmpFile = File.createTempFile("fileToCopy", ".ogg", getExternalCacheDir());
                output = new FileOutputStream(tmpFile);

                byte[] buffer = new byte[4 * 1024];
                int read;

                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
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

            if (mSelectedSound != null && tmpFile != null) {
                // Replace the file
                return replaceFile(mSelectedSound, tmpFile.getAbsolutePath());
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mDialog.dismiss();
            if (result) {
                showSuccess();
                mAdapter.notifyDataSetChanged();
            } else {
                showError();
            }
        }
    }

    private class RestoreFileTask extends AsyncTask<String,Void,Boolean> {
        @Override
        protected void onPreExecute() {
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            return restoreSound(params[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mDialog.dismiss();
            if (result) {
                showSuccess();
                mAdapter.notifyDataSetChanged();
            } else {
                showError();
            }
        }
    }
}
