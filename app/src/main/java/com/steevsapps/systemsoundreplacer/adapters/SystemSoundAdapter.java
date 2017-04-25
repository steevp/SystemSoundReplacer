package com.steevsapps.systemsoundreplacer.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.steevsapps.systemsoundreplacer.R;

import java.io.File;
import java.util.List;

public class SystemSoundAdapter extends RecyclerView.Adapter<SystemSoundAdapter.ViewHolder> {
    private List<String> mDataSet;
    private ItemClickedListener mListener;
    private String mSoundFolder;

    public interface ItemClickedListener {
        void playSound(String soundFile);
        void openSound(String soundFile);
        void askRestoreSound(String soundFile);
    }

    public SystemSoundAdapter(Context context, List<String> data, String soundFolder) {
        try {
            mListener = (ItemClickedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ItemClickedListener!");
        }
        mDataSet = data;
        mSoundFolder = soundFolder;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sound, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String soundFile = mDataSet.get(position);
        holder.bind(soundFile, mListener, mSoundFolder);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mSoundFile;
        private ImageView mPlayButton;
        private ImageView mOpenButton;
        private ImageView mRestoreButton;

        public ViewHolder(View itemView) {
            super(itemView);
            mSoundFile = (TextView) itemView.findViewById(R.id.sound_file);
            mPlayButton = (ImageView) itemView.findViewById(R.id.play_button);
            mOpenButton = (ImageView) itemView.findViewById(R.id.open_button);
            mRestoreButton = (ImageView) itemView.findViewById(R.id.resore_button);
        }

        public void bind(final String soundFile, final ItemClickedListener listener, String soundFolder) {
            mSoundFile.setText(soundFile);
            mPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.playSound(soundFile);
                }
            });
            mOpenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.openSound(soundFile);
                }
            });
            mRestoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.askRestoreSound(soundFile);
                }
            });
            File backupFile = new File(soundFolder + soundFile + ".bak");
            if (backupFile.exists()) {
                mSoundFile.setTypeface(null, Typeface.BOLD);
                mRestoreButton.setVisibility(View.VISIBLE);
            } else {
                mSoundFile.setTypeface(null, Typeface.NORMAL);
                mRestoreButton.setVisibility(View.GONE);
            }
        }
    }
}
