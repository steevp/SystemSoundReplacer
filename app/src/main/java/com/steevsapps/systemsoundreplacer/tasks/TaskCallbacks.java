package com.steevsapps.systemsoundreplacer.tasks;

public interface TaskCallbacks {
    void onPreExecute();
    void onPostExecute(Boolean result);
}
