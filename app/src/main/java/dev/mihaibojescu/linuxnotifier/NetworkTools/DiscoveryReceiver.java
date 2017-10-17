package dev.mihaibojescu.linuxnotifier.NetworkTools;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import dev.mihaibojescu.linuxnotifier.Crypto.CryptHandler;
import dev.mihaibojescu.linuxnotifier.DeviceHandlers.Device;
import dev.mihaibojescu.linuxnotifier.DeviceHandlers.DeviceHandler;
import dev.mihaibojescu.linuxnotifier.DeviceHandlers.Request;

/**
 * Created by michael on 9/12/17.
 */

public class DiscoveryReceiver extends Thread
{
    private static DiscoveryReceiver instance = null;
    private MulticastSocket socket;

    private DiscoveryReceiver()
    {
        try
        {
            socket = new MulticastSocket(5005);
            socket.joinGroup(InetAddress.getByName("224.0.0.1"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static DiscoveryReceiver getInstance()
    {
        if (instance == null)
            instance = new DiscoveryReceiver();

        return instance;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                socket.receive(packet);

                JSONObject message = new JSONObject(new String(packet.getData(), 0, packet.getLength()));

                if (message.getString("reason").equals(Request.reasons.DISCOVER) &&
                    message.getString("from").equals("desktop") &&
                    !DeviceHandler.getInstance().deviceExists(message.getString("mac")))
                {
                    Device device = new Device(message.getString("name"),
                                               packet.getAddress().getHostAddress(),
                                               message.getString("mac"),
                                               null);

                    DeviceHandler.getInstance().addDevice(device);
                }
            }
            catch (JSONException | IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
