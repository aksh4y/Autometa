package com.akshaysadarangani.autometa.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.akshaysadarangani.autometa.GeofenceErrorMessages;
import com.akshaysadarangani.autometa.MainActivity;
import com.akshaysadarangani.autometa.SplashActivity;
import com.akshaysadarangani.autometa.receivers.Button_listener;
import com.akshaysadarangani.autometa.R;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

public class MyNotificationService extends Service implements OnCompleteListener<Void> {


    String userID, content, title;
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

    protected void sendNotification(final String content) {
        Log.e("NService", "trying to notify");
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                getApplicationContext());

        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

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
            notificationManager.createNotificationChannel(notificationChannel);
            mBuilder.setChannelId(channelId);
        }

        final String[] args = content.split("&&");

        final RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.custom_notification);
        switch (args[1]) {
            case "SMS": title = "Send SMS to " + args[2];
            break;
            case "EMAIL": title = "Send Email to " + args[2];
            break;
            default: title = args[2];
        }
        remoteViews.setTextViewText(R.id.notif_title, title);
        remoteViews.setTextViewText(R.id.countdown, "60");

        final int notification_id = 1001;

        Intent button_intent = new Intent(this, Button_listener.class);
        button_intent.putExtra("id",notification_id);
        button_intent.putExtra("uid", userID);
        PendingIntent button_pending_event = PendingIntent.getBroadcast(getApplicationContext(),notification_id,
                button_intent,0);

        remoteViews.setOnClickPendingIntent(R.id.cancel_btn,button_pending_event);

        Intent notification_intent = new Intent(getApplicationContext(),SplashActivity.class);
        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(SplashActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notification_intent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        // Define the notification settings.
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setContentTitle("Autometa")
                .setCustomContentView(remoteViews)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId(channelId); // Channel ID
        }

        //        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),notification_id,notification_intent,PendingIntent.FLAG_CANCEL_CURRENT);

        // Issue the notification
        notificationManager.notify(notification_id, mBuilder.build());

        timer = new CountDownTimer(60 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                remoteViews.setTextViewText(R.id.countdown, Long.toString(millisUntilFinished/1000));
                startForeground(notification_id, mBuilder.build());
                flag = true;
            }

            public void onFinish() {
                if(args[1].equals("SMS")) {
                    sendSMS(args[4], "I am near " + args[3]);
                }
                else if(args[1].equals("EMAIL")) {
                    sendEmail(args[4], "I am near " + args[3]);
                }
                final RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.custom_notification_done);
                remoteViews.setTextViewText(R.id.notif_content , title);
                Intent button_intent = new Intent(getApplicationContext(), Button_listener.class);
                button_intent.putExtra("id",notification_id);
                button_intent.putExtra("uid", userID);
                PendingIntent button_pending_event = PendingIntent.getBroadcast(getApplicationContext(),notification_id,
                        button_intent,0);
                remoteViews.setOnClickPendingIntent(R.id.close_btn,button_pending_event);
                mBuilder.setCustomContentView(remoteViews);
                startForeground(notification_id, mBuilder.build());
                // Database update
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("tasks").child(args[0]);
                myRef.child("completed").setValue(true);
                //Remove geofence
                removeGeofence(content);
                //stopSelf();
            }
        };

        timer.start();
    }

    private void removeGeofence(String requestID) {
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        Collections.addAll(triggeringGeofencesIdsList, requestID.split(", "));
        GeofencingClient mGeofencingClient = LocationServices.getGeofencingClient(this);
        mGeofencingClient.removeGeofences(triggeringGeofencesIdsList).addOnCompleteListener(this);
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

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            //sendNotification(geofenceTransitionDetails);
            Log.e("SERVICE", "DELETED GEOFENCE");
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w("NotificationService", errorMessage);
        }
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    public void sendEmail(String email, String msg) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        String[] recipients = new String[]{email, "",};
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Autometa: Automated Email");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, msg);
        emailIntent.setType("text/plain");
        startActivity(Intent.createChooser(emailIntent, "Autometa Email"));
    }
}
