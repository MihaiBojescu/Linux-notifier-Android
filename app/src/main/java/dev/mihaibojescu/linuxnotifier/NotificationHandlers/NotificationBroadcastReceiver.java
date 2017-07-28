package dev.mihaibojescu.linuxnotifier.NotificationHandlers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import dev.mihaibojescu.linuxnotifier.DeviceHandlers.DeviceHandler;

/**
 * Created by michael on 7/27/17.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            JSONObject message = new JSONObject();
            message.put("request", "notification");
            message.put("appName", intent.getStringExtra("appName"));
            message.put("data", intent.getStringExtra("data"));

            DeviceHandler.getInstance().dispatchMessageToAllDevices(message);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }

    }
}
