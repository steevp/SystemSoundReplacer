package com.steevsapps.systemsoundreplacer.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.steevsapps.systemsoundreplacer.R;
import com.steevsapps.systemsoundreplacer.adapters.SystemSoundAdapter;
import com.steevsapps.systemsoundreplacer.dialogs.ConfirmDialog;
import com.steevsapps.systemsoundreplacer.dialogs.ErrorDialog;
import com.steevsapps.systemsoundreplacer.tasks.ReplaceFileTaskFragment;
import com.steevsapps.systemsoundreplacer.tasks.RestoreFileTaskFragment;
import com.steevsapps.systemsoundreplacer.tasks.TaskCallbacks;
import com.steevsapps.systemsoundreplacer.utils.Shell;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements SystemSoundAdapter.ItemClickedListener, TaskCallbacks, ConfirmDialog.DialogListener {

    private final static String TAG = "com.steevsapps.TAG";
    private final static String TAG_TASK_FRAGMENT = "TAG_TASK_FRAGMENT";
    private final static String SOUND_FOLDER = "/system/media/audio/ui/";
    private final static String SELECTED_SOUND = "SELECTED_SOUND";
    private final static String REPLACEMENT_SOUND = "REPLACEMENT_SOUND";


    private RecyclerView mRecyclerView;
    private SystemSoundAdapter mAdapter;
    private ProgressDialog mDialog;
    private MediaPlayer mMediaPlayer;
    private String mSelectedSound;
    private Uri mReplacementSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));

        mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setMessage(getString(R.string.loading));

        mMediaPlayer = new MediaPlayer();

        if (savedInstanceState != null) {
            mSelectedSound = savedInstanceState.getString(SELECTED_SOUND);
            mReplacementSound = savedInstanceState.getParcelable(REPLACEMENT_SOUND);
        }

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SELECTED_SOUND, mSelectedSound);
        outState.putParcelable(REPLACEMENT_SOUND, mReplacementSound);
    }

    /**
     * Extract the helper script from res/raw
     */
    private void extractHelper() {
        File of = new File(getApplicationInfo().dataDir, "suhelper.sh");
        InputStream input = getResources().openRawResource(R.raw.suhelper);
        FileOutputStream output = null;
        try {
            byte[] buffer = new byte[input.available()];
            //noinspection ResultOfMethodCallIgnored
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
        File f = new File(SOUND_FOLDER);
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
        mAdapter = new SystemSoundAdapter(this, soundList, SOUND_FOLDER);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void showError() {
        ErrorDialog dialog = new ErrorDialog();
        dialog.show(getSupportFragmentManager(), "error_dialog");
    }

    private void showSuccess() {
        ConfirmDialog dialog = ConfirmDialog.newInstance(getString(R.string.success_dialog_title),
                getString(R.string.success_dialog_message), "reboot");
        dialog.show(getSupportFragmentManager(), "success_dialog");
    }

    @Override
    public void playSound(String soundFile) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(SOUND_FOLDER + soundFile);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openSound(String soundFile) {
        mSelectedSound = SOUND_FOLDER + soundFile;
        Intent intent = new Intent();
        intent.setType("application/ogg");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.file_chooser_title)), 123);
    }

    @Override
    public void askRestoreSound(final String soundFile) {
        mSelectedSound = SOUND_FOLDER + soundFile;
        ConfirmDialog dialog = ConfirmDialog.newInstance(getString(R.string.confirm_dialog_title),
                "Are you sure you want to restore " + mSelectedSound + "?", "restore");
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            mReplacementSound = data.getData();
            ConfirmDialog dialog = ConfirmDialog.newInstance(getString(R.string.confirm_dialog_title),
                    "Are you sure you want to replace " + mSelectedSound + "?", "replace");
            dialog.show(getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    public void onPreExecute() {
        mDialog.show();
    }

    @Override
    public void onPostExecute(Boolean result) {
        mDialog.dismiss();
        // Remove task fragment
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        if (fragment != null) {
            fm.beginTransaction().remove(fragment).commit();
        }
        if (result) {
            showSuccess();
            mAdapter.notifyDataSetChanged();
        } else {
            showError();
        }
    }

    @Override
    public void onYesClicked(String tag) {
        FragmentManager fm = getSupportFragmentManager();
        switch (tag) {
            case "replace":
                // Replace the file
                ReplaceFileTaskFragment replaceTask = ReplaceFileTaskFragment.newInstance(
                        getApplicationInfo().dataDir + "/suhelper.sh",
                        mSelectedSound,
                        mReplacementSound);
                fm.beginTransaction().add(replaceTask, TAG_TASK_FRAGMENT).commit();
                break;
            case "restore":
                // Restore the file
                RestoreFileTaskFragment restoreTask = RestoreFileTaskFragment.newInstance(
                        getApplicationInfo().dataDir + "/suhelper.sh",
                        mSelectedSound);
                fm.beginTransaction().add(restoreTask, TAG_TASK_FRAGMENT).commit();
                break;
            case "reboot":
                try {
                    Shell.runAsRoot("reboot");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
