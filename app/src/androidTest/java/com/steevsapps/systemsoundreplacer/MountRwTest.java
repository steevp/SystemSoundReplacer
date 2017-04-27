package com.steevsapps.systemsoundreplacer;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.DataOutputStream;

import static org.junit.Assert.*;

/**
 * Check that remount,rw works
 */
@RunWith(AndroidJUnit4.class)
public class MountRwTest {
    private final static String cmd = "mount -o remount,rw -t ext4 /system";

    @Test
    public void mountRw() throws Exception {
        Process p = Runtime.getRuntime().exec("su");
        DataOutputStream dos = new DataOutputStream(p.getOutputStream());
        dos.writeBytes(cmd + "\n");
        dos.writeBytes("exit $?\n");
        dos.close();
        int returnCode = p.waitFor();
        assertEquals(0, returnCode);
    }

}
