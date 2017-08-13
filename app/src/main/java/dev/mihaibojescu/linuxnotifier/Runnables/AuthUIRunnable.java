package dev.mihaibojescu.linuxnotifier.Runnables;

import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;

import org.json.JSONObject;

import dev.mihaibojescu.linuxnotifier.DeviceHandlers.Device;
import dev.mihaibojescu.linuxnotifier.DeviceHandlers.DeviceHandler;
import dev.mihaibojescu.linuxnotifier.DeviceHandlers.Request;
import dev.mihaibojescu.linuxnotifier.MainActivity;
import dev.mihaibojescu.linuxnotifier.NetworkTools.NetworkCommunicator;
import dev.mihaibojescu.linuxnotifier.R;

/**
 * Created by michael on 8/13/17.
 */

public class AuthUIRunnable implements Runnable
{
    private Device device;
    private MainActivity mainActivity;
    private DeviceHandler deviceHandler;
    private NetworkCommunicator communicator;

    public AuthUIRunnable(Device device, MainActivity main, NetworkCommunicator communicator, DeviceHandler deviceHandler)
    {
        this.device = device;
        this.mainActivity = main;
        this.communicator = communicator;
        this.deviceHandler = deviceHandler;
    }

    @Override
    public void run()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.mainActivity);
        builder.setTitle("Authentificate");
        builder.setMessage("Authentificate with " + device.getName() + "(" + device.getAddress() + ") with pin: " + device.getPin() + "?");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                device.setStatus(Device.statuses.CONNECTED);
                deviceHandler.updateUI();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                device.setStatus(Device.statuses.WAITING_AUTH);
                deviceHandler.updateUI();

                JSONObject reply = Request.createRequest(Request.reasons.DENY_AUTH);
                communicator.pushMessageToIP(device.getAddress(), 5005, reply, false);
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        new CountDownTimer(10000, 5000)
        {
            @Override
            public void onTick(long ms)
            {
            }

            @Override
            public void onFinish()
            {
                if (alertDialog.isShowing())
                {
                    alertDialog.dismiss();
                    device.setStatus(Device.statuses.WAITING_AUTH);
                    deviceHandler.updateUI();

                    JSONObject reply = Request.createRequest(Request.reasons.DENY_AUTH);
                    communicator.pushMessageToIP(device.getAddress(), 5005, reply, false);
                }
            }
        }.start();
    }
}
