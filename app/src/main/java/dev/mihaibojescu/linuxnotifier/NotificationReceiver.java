package dev.mihaibojescu.linuxnotifier;

import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by michael on 05.05.2017.
 */

public class NotificationReceiver extends NotificationListenerService{
    private MainActivity main;
    public NotificationReceiver(MainActivity main)
    {
        this.main = main;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        // Implement what you want here
    }


    public void onNotificationPosted (StatusBarNotification sbn, NotificationListenerService.RankingMap rankingMap)
    {
        Log.d("Notification from", sbn.getPackageName().toString());
        Log.d("Notification text", sbn.getNotification().toString());

        ((TextView)main.findViewById(R.id.textView)).setText(sbn.getPackageName() + "\n" + sbn.getNotification().toString());
        Intent intent = new Intent("dev.mihaibojescu.linuxnotifier.NotificationListener");
        intent.putExtra("notification_event", "onNotificationPosted :" + sbn.getPackageName() + "\n");
        sendBroadcast(intent);
    }
}
