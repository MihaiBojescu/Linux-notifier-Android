package dev.mihaibojescu.linuxnotifier.NetworkTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import dev.mihaibojescu.linuxnotifier.DeviceHandlers.Request;

/**
 * Created by michael on 9/12/17.
 */

public class DiscoverySender extends Thread
{
    private static DiscoverySender instance = null;
    private final Object send;
    private MulticastSocket socket;
    private DatagramPacket packet;
    private byte[] messageToBytes;


    private DiscoverySender()
    {
        send = new Object();
        try
        {
            InetAddress group = InetAddress.getByName("224.0.0.1");
            socket = new MulticastSocket(null);
            socket.joinGroup(group);

            JSONObject message = Request.createRequest(Request.reasons.DISCOVER);
            assert message != null;
            message.put("from", "android");
            messageToBytes = message.toString().getBytes();
            packet = new DatagramPacket(messageToBytes, messageToBytes.length, group, 5005);
        }
        catch (JSONException | IOException e)
        {
            e.printStackTrace();
        }
    }

    public static DiscoverySender getInstance()
    {
        if (instance == null)
            instance = new DiscoverySender();

        return instance;
    }

    @Override
    public void run()
    {
        while (true)
        {
            synchronized (send)
            {
                try
                {
                    send.wait();
                    socket.send(packet);
                }
                catch (InterruptedException | IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

    }

    public void discover()
    {
        synchronized (send)
        {
            send.notify();
        }
    }
}
