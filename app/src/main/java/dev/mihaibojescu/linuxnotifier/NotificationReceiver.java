package dev.mihaibojescu.linuxnotifier;

import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by michael on 05.05.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
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
    }


    public void onNotificationPosted (StatusBarNotification sbn, NotificationListenerService.RankingMap rankingMap)
    {
        Intent intent = new Intent("dev.mihaibojescu.linuxnotifier");
        intent.putExtra("notification_event", "onNotificationPosted :" + sbn.getPackageName() + "\n");
        sendBroadcast(intent);
    }
}
