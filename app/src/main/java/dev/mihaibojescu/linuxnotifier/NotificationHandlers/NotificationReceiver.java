package dev.mihaibojescu.linuxnotifier.NotificationHandlers;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.TextView;

import dev.mihaibojescu.linuxnotifier.MainActivity;

/**
 * Created by michael on 05.05.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationReceiver extends NotificationListenerService
{

    public enum actions
    {
        NOTIFICATION_RECEIVED
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn)
    {
        super.onNotificationRemoved(sbn);
    }

    @Override
    public void onNotificationPosted (StatusBarNotification sbn)
    {
        Intent intent = new Intent("dev.mihaibojescu.linuxnotifier");
        intent.setAction(actions.NOTIFICATION_RECEIVED.toString());

        try
        {
            PackageManager packageManager = getApplicationContext().getPackageManager();
            ApplicationInfo applicationInfo;
            applicationInfo = packageManager.getApplicationInfo(sbn.getPackageName(), 0);

            intent.putExtra("appName", packageManager.getApplicationLabel(applicationInfo));
        }
        catch(PackageManager.NameNotFoundException e)
        {
            intent.putExtra("appName", "unknown application");
        }

        intent.putExtra("data", sbn.getNotification().extras.toString());
        sendBroadcast(intent);
    }
}
