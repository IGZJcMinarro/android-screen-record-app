package com.cr5315.screenrecord;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;

/**
 * Created by Ben on 11/17/13.
 */
public class TimerService extends Service {
    Notification notification;
    Notification.Builder builder;
    NotificationManager notificationManager;
    Tools tools;

    private static final int NOTIFICATION_ID = 5315;

    private long seconds;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        seconds = intent.getExtras().getLong("time");
        tools = new Tools(this);

        builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.recording));
        builder.setSmallIcon(R.drawable.ic_notification_recording);
        builder.setOngoing(true);
        builder.setAutoCancel(false);

        notification = builder.build();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID, notification);
        new RecordingTimer(seconds, 1000).start();

        return Service.START_NOT_STICKY;
    }

    private class RecordingTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public RecordingTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            int totalSeconds = tools.getSecondsFromMillis(millisUntilFinished);
            int minutes = tools.getMinutes(totalSeconds);
            int seconds = tools.getSeconds(totalSeconds);

            String time = String.format(getString(R.string.recording_time_left), minutes, seconds);
            builder.setContentText(time);
            notification = builder.build();

            notificationManager.notify(NOTIFICATION_ID, notification);
        }

        @Override
        public void onFinish() {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
