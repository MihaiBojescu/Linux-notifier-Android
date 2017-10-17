package dev.mihaibojescu.linuxnotifier.NotificationHandlers;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import dev.mihaibojescu.linuxnotifier.R;

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
    public IBinder onBind(Intent intent)
    {
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
        Intent intent = new Intent(String.valueOf(R.string.app_name));
        intent.setClass(this, NotificationBroadcastReceiver.class);
        intent.setAction(actions.NOTIFICATION_RECEIVED.toString());

        try
        {
            PackageManager packageManager = getApplicationContext().getPackageManager();
            ApplicationInfo applicationInfo;
            applicationInfo = packageManager.getApplicationInfo(sbn.getPackageName(), 0);

            intent.putExtra("app name", packageManager.getApplicationLabel(applicationInfo));
        }
        catch(PackageManager.NameNotFoundException e)
        {
            intent.putExtra("app name", "unknown application");
        }

        intent.putExtra("title", sbn.getNotification().extras.getString("android.title"));
        intent.putExtra("data", sbn.getNotification().extras.getString("android.text"));
        sendBroadcast(intent);
    }
}
