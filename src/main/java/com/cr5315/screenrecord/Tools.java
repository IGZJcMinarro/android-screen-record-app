package com.cr5315.screenrecord;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ben on 11/1/13.
 */
public class Tools {
    Context context;

    public Tools(Context context) {
        this.context = context;
    }

    // Not used in favor of SuTask
    public boolean runAsRoot(String[] commands) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());

            for (String cmd : commands) {
                Log.i("ScreenRecord", "Running command '" + cmd + "'");
                os.writeBytes(cmd + "\n");
            }
            os.writeBytes("exit");
            os.flush();
            os.close();
            return true;
        } catch (IOException e) {
            Log.e("ScreenRecord", e.toString());
            return false;
        }
    }

    public String formatCommand(int timeLimit, String saveLocation, int bitRate, boolean rotate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        // I don't like doing this, but it make the app work
        saveLocation = "/sdcard/Screen Record/";

        String command = "screenrecord \"" + saveLocation + simpleDateFormat.format(new Date())
                + ".mp4\" --time-limit " +
                String.valueOf(timeLimit) + " --bit-rate " + String.valueOf(bitRate); //+
                // " --size " + videoSize.asString() + " ";
        if (rotate) {
            command += " --rotate ";
        }
        // command += saveLocation + simpleDateFormat.format(new Date()) + ".mp4";
        return command;
    }

    public String formatTime(int minute, int second) {
        String m = String.valueOf(minute); String s = String.valueOf(second);
        // minute
        if (m.matches("1")) m = "01";
        else if (m.matches("2")) m = "02";
        else if (m.matches("3")) m = "03";
        else if (m.matches("4")) m = "04";
        else if (m.matches("5")) m = "05";
        else if (m.matches("6")) m = "06";
        else if (m.matches("7")) m = "07";
        else if (m.matches("8")) m = "08";
        else if (m.matches("9")) m = "09";
        else if (m.matches("0")) m = "00";

        // second
        if (s.matches("1")) s = "01";
        else if (s.matches("2")) s = "02";
        else if (s.matches("3")) s = "03";
        else if (s.matches("4")) s = "04";
        else if (s.matches("5")) s = "05";
        else if (s.matches("6")) s = "06";
        else if (s.matches("7")) s = "07";
        else if (s.matches("8")) s = "08";
        else if (s.matches("9")) s = "09";
        else if (s.matches("0")) s = "00";

        return m + ":" + s;
    }

    public String formatSeconds(String second) {
        String s = second;
        // second
        if (s.matches("1")) s = "01";
        else if (s.matches("2")) s = "02";
        else if (s.matches("3")) s = "03";
        else if (s.matches("4")) s = "04";
        else if (s.matches("5")) s = "05";
        else if (s.matches("6")) s = "06";
        else if (s.matches("7")) s = "07";
        else if (s.matches("8")) s = "08";
        else if (s.matches("9")) s = "09";
        else if (s.matches("0")) s = "00";

        return s;
    }

    public int getMinutes(int seconds) {
        int minutes = 0;
        for (int sec = 60; sec <= seconds; sec += 60) {
            minutes++;
        }
        return minutes;
    }

    public int getSeconds(int seconds) {
        return seconds % 60;
    }

    public int getTotalSeconds(int minutes, int seconds) {
        return (minutes * 60) + seconds;
    }

    public String bitRateToString(int bitrate) {
        String result = "";

        int num = bitrate / 1000000;

        return String.valueOf(num) + "Mbps";
    }

    public long getMillis(int seconds) {
        return seconds * 1000;
    }

    public int getSecondsFromMillis(long millis) {
        return (int) millis / 1000;
    }
}
