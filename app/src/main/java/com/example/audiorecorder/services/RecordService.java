package com.example.audiorecorder.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.audiorecorder.R;
import com.example.audiorecorder.view.IServiceControler;
import com.example.audiorecorder.view.MainActivity;

public class RecordService extends Service {

    private static final String CHANNEL_ID = "record";
    public static final String ACTION_CLOSE = "ACTION_CLOSE";
    public static final String ACTION_START = "ACTION_START";
    private static final int NOTIFICATION_ID = 1;
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    private boolean isRunning = false;
    private boolean flag = true;
    private int seconds = 0;
    private int minutes = 0;
    private int secs = 0;
    RemoteViews remoteViews;
    IServiceControler serviceControler;
    private final IBinder binder = new LocalBinder();



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_CLOSE:
                stopTimer();
                serviceControler.onStopService();
                stopSelf();
                break;
            case ACTION_PAUSE:
                remoteViews.setImageViewResource(R.id.pplay, flag ? R.drawable.record_im : R.drawable.pause_im);
                if (flag) {
                    stopTimer();
                    serviceControler.onPauseService();
                } else {
                    resumeTimer();
                    serviceControler.onResumeService();
                }
                flag = !flag;
                updateNotification(createNotification());
                break;
            default:
                remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
                isRunning = true;
                startForeground(NOTIFICATION_ID, createNotification());
                runTimer();
                break;
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    public void setControler(IServiceControler iServiceControler){
        serviceControler = iServiceControler;
    }


    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent pauseIntent = new Intent(this, RecordService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pause = PendingIntent.getService(getApplicationContext(), 1, pauseIntent, 0);

        Intent stopIntent = new Intent(this, RecordService.class);
        stopIntent.setAction(ACTION_CLOSE);
        PendingIntent stop = PendingIntent.getService(getApplicationContext(), 2, stopIntent, 0);

        remoteViews.setOnClickPendingIntent(R.id.pplay, pause);
        remoteViews.setOnClickPendingIntent(R.id.stop, stop);
        remoteViews.setTextViewText(R.id.record_time, getResources().getString(R.string.time, minutes, secs));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.record_image)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContent(remoteViews)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);

        Notification notif = builder.build();

        return notif;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(false);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void updateNotification(@NonNull Notification notification) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void runTimer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    minutes = seconds / 60;
                    secs = (seconds % 60);
                    seconds++;
                    updateNotification(createNotification());
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void stopTimer() {
        isRunning = false;
    }

    private void resumeTimer() {
        isRunning = true;
    }


    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder{
        public RecordService getService() {
            return RecordService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
