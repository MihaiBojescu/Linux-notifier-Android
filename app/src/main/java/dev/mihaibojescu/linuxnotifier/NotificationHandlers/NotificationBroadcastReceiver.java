package dev.mihaibojescu.linuxnotifier.NotificationHandlers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import dev.mihaibojescu.linuxnotifier.DeviceHandlers.DeviceHandler;
import dev.mihaibojescu.linuxnotifier.DeviceHandlers.Request;

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
            JSONObject message = Request.createRequest(Request.reasons.NOTIFICATION);
            message.put("app name", intent.getStringExtra("app name"));
            message.put("title", intent.getStringExtra("title"));
            message.put("data", intent.getStringExtra("data"));

            DeviceHandler.getInstance().dispatchMessageToAllDevices(message);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }
}
