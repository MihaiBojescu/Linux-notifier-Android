package dev.mihaibojescu.linuxnotifier.NotificationHandlers;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by michael on 7/27/17.
 */

public class NotificationBroadcastReceiver extends android.content.BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Toast.makeText(context, intent.getStringExtra("notification_event"), Toast.LENGTH_SHORT).show();
    }
}
