package com.steevsapps.systemsoundreplacer;

import android.support.test.runner.AndroidJUnit4;

import com.steevsapps.systemsoundreplacer.utils.Shell;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Check that remount,rw works
 */
@RunWith(AndroidJUnit4.class)
public class MountRwTest {
    private final static String cmd = "mount -o remount,rw -t ext4 /system";

    @Test
    public void mountRw() throws Exception {
        Shell.Result result = Shell.runAsRoot(cmd);
        assertEquals(0, result.returnCode);
    }

}
