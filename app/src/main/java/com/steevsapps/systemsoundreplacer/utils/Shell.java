package com.steevsapps.systemsoundreplacer.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class Shell {

    public static class Result {
        public String output;
        public int returnCode;

        Result(String output, int returnCode) {
            this.output = output;
            this.returnCode = returnCode;
        }
    }

    public static Result runAsRoot(String cmd) throws Exception {
        Process p = Runtime.getRuntime().exec("su");
        DataOutputStream dos = new DataOutputStream(p.getOutputStream());
        dos.writeBytes(cmd + "\n");
        dos.writeBytes("exit $?\n");
        dos.close();

        // Get output
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        // Get return code
        int returnCode = p.waitFor();

        return new Result(output.toString(), returnCode);
    }
}
