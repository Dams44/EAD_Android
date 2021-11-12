package org.miage.placesearcher.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
//import android.support.v4.app.NotificationCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.miage.placesearcher.MainActivity;
import org.miage.placesearcher.R;

/**
 * Created by alexmorel on 14/02/2018.
 */

public class PlaceSearcherFirebaseNotificationService extends FirebaseMessagingService {

    private static final String NOTIF_CHANNEL_ID = "PlaceSearcher Notifications";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        // Step 1 : Parse notification
        String body = message.getNotification().getBody();

        // Step 2 : Build push notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, NOTIF_CHANNEL_ID);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle(mBuilder)
                .bigText(body)
                .setBigContentTitle("Place Searcher Notification"))
                .setContentTitle("Place Searcher Notification")
                .setContentText(body)
                .setSmallIcon(R.drawable.home_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.street_icon))
                .setDefaults(android.app.Notification.DEFAULT_VIBRATE);

        // Step 3 : Define clic behavior
        PendingIntent contentIntent = PendingIntent.getActivity(this, 26,
                new Intent(this, MainActivity.class), 0);
        mBuilder.setContentIntent(contentIntent);

        // Step 4 : send notification
        NotificationManager mNotificationManager = ((NotificationManager)getSystemService(NOTIFICATION_SERVICE));
        mNotificationManager.notify(26, mBuilder.build());
    }
}
