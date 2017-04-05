package com.procrastech.cruisevolume;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by IEnteramine on 05.04.2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        if (data.containsKey("click_action")) {
            ClickActionHelper.startActivity(data.get("click_action"), null, this);
        }
        NotificationManager mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mgr.notify(13, buildNotification(remoteMessage.getNotification().getBody()));
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
    }


    private Notification buildNotification(String body){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mBuilder.setContentTitle("CruiseVolume");

        mBuilder.setAutoCancel(true);
        mBuilder.setContentText("Message from the Developer");
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(body));
        mBuilder.setSmallIcon(R.drawable.ic_firebase_notification);
        if( Build.VERSION.SDK_INT < 23){
            mBuilder.setColor(ContextCompat.getColor(getBaseContext(), R.color.color_not_background));
        }else{
            mBuilder.setColor(getResources().getColor(R.color.color_not_background,getTheme()));
        }



        return mBuilder.build();
    }
}
