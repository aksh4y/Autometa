package com.akshaysadarangani.autometa.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import com.akshaysadarangani.autometa.receivers.Button_listener;
import com.akshaysadarangani.autometa.R;
import com.akshaysadarangani.autometa.TriggersActivity;

public class MyNotificationService extends Service {


    String userID, content;
    Intent br;
    CountDownTimer timer;
    boolean flag;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        userID = intent.getStringExtra("uid");
        content = intent.getStringExtra("content");
        if(!isNotificationVisible())
            sendNotification(content);
        return START_NOT_STICKY;
    }

    protected void sendNotification(String content) {
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                getApplicationContext());

        String channelId = "Autometa_100";
        CharSequence channelName = "Autometa";

        int importance = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_HIGH;
        }
        //Bitmap big_bitmap_image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_reminder);

        NotificationChannel notificationChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mBuilder.setChannelId(channelId);
        }

        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        final RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.custom_notification);
        remoteViews.setTextViewText(R.id.notif_title, content);
        remoteViews.setTextViewText(R.id.countdown, "60");


        final int notification_id = 1001;

        Intent button_intent = new Intent(this, Button_listener.class);
        button_intent.putExtra("id",notification_id);
        button_intent.putExtra("uid", userID);
        PendingIntent button_pending_event = PendingIntent.getBroadcast(getApplicationContext(),notification_id,
                button_intent,0);

        remoteViews.setOnClickPendingIntent(R.id.cancel_btn,button_pending_event);

        Intent notification_intent = new Intent(getApplicationContext(),TriggersActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),notification_id,notification_intent,PendingIntent.FLAG_CANCEL_CURRENT);


        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setCustomContentView(remoteViews)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);


        notificationManager.notify(notification_id,mBuilder.build());

        timer = new CountDownTimer(10 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                remoteViews.setTextViewText(R.id.countdown, Long.toString(millisUntilFinished/1000));
                startForeground(notification_id, mBuilder.build());
                flag = true;
            }

            public void onFinish() {
                //sendSMS("+919886901826", content);
                final RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.custom_notification_done);
                Intent button_intent = new Intent(getApplicationContext(), Button_listener.class);
                button_intent.putExtra("id",notification_id);
                button_intent.putExtra("uid", userID);
                PendingIntent button_pending_event = PendingIntent.getBroadcast(getApplicationContext(),notification_id,
                        button_intent,0);
                remoteViews.setOnClickPendingIntent(R.id.close_btn,button_pending_event);
                mBuilder.setCustomContentView(remoteViews);
                startForeground(notification_id, mBuilder.build());
                //stopSelf();
            }
        };

        timer.start();
    }

    private boolean isNotificationVisible() {
       return flag;
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        flag = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
