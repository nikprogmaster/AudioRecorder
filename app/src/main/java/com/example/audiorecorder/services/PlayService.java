package com.example.audiorecorder.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.audiorecorder.R;
import com.example.audiorecorder.view.MainActivity;
import com.example.audiorecorder.view.RecordsFragment;


public class PlayService extends Service {


    private static final String CHANNEL_ID = "playing";
    private RemoteViews remoteViews;
    private boolean isRunning = false;
    private boolean flag = true;
    private int seconds = 0;
    private int minutes = 0;
    private int secs = 0;
    private static final int NOTIFICATION_ID = 2;
    public static final String ACTION_CLOSE = "ACTION_CLOSE";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    private Messenger mMessenger = new Messenger(new IncomingHandler());
    public static final int MSG_START_PlAYER = 1;
    public static final int MSG_PAUSE_PlAYER = 2;
    public static final int MSG_RESUME_PlAYER = 3;
    public static final int MSG_STOP_PlAYER = 4;
    private Messenger newMessenger = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_CLOSE:
                stopTimer();
                sendMessage(MSG_STOP_PlAYER);
                stopSelf();
                break;
            case ACTION_PAUSE:
                remoteViews.setImageViewResource(R.id.play_pause_btn, flag ? R.drawable.play_im : R.drawable.pause_im);
                if (flag) {
                    stopTimer();
                    sendMessage(MSG_PAUSE_PlAYER);
                } else {
                    resumeTimer();
                    sendMessage(MSG_RESUME_PlAYER);
                }
                flag = !flag;
                updateNotification(createNotification());

                break;
            default:
                remoteViews = new RemoteViews(getPackageName(), R.layout.play_notification_layout);
                isRunning = true;
                startForeground(NOTIFICATION_ID, createNotification());
                runTimer();
                break;
        }
        return START_NOT_STICKY;
    }

    private void sendMessage(int msg){
        newMessenger = new Messenger(onBind(new Intent(this, MainActivity.class)));
        Message message = Message.obtain(null, msg);
        try {
            newMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 3, intent, 0);

        Intent pauseIntent = new Intent(this, PlayService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pause = PendingIntent.getService(getApplicationContext(), 4, pauseIntent, 0);

        Intent stopIntent = new Intent(this, PlayService.class);
        stopIntent.setAction(ACTION_CLOSE);
        PendingIntent stop = PendingIntent.getService(getApplicationContext(), 5, stopIntent, 0);

        remoteViews.setOnClickPendingIntent(R.id.play_pause_btn, pause);
        remoteViews.setOnClickPendingIntent(R.id.stop_playing_btn, stop);
        remoteViews.setTextViewText(R.id.play_time, getResources().getString(R.string.time, minutes, secs));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContent(remoteViews)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);

        Notification notif = builder.build();

        return notif;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getResources().getString(R.string.playing_notification_name);
            String description = getResources().getString(R.string.playing_notification_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
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


    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_PlAYER:
                    remoteViews.setTextViewText(R.id.play_notif_title, msg.getData().getString(RecordsFragment.BUNDLE_RECORD_NAME));
                    updateNotification(createNotification());
                    mMessenger = msg.replyTo;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


}
