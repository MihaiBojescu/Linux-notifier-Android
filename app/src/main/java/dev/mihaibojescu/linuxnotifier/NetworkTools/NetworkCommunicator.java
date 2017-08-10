package dev.mihaibojescu.linuxnotifier.NetworkTools;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by michael on 03.05.2017.
 */

public class NetworkCommunicator extends Thread
{

    private static NetworkCommunicator instance = null;
    private BlockingQueue<String> hosts;
    private BlockingQueue<Integer> ports;
    private BlockingQueue<JSONObject> messages;
    private BlockingQueue<JSONObject> receivedMessages;
    private BlockingQueue<Boolean> needsResponse;
    private Socket socket;
    private BufferedReader streamIn;
    private BufferedWriter streamOut;
    private Integer interval;


    private NetworkCommunicator()
    {
        this.hosts = new LinkedBlockingQueue<>();
        this.ports = new LinkedBlockingQueue<>();
        this.messages = new LinkedBlockingQueue<>();
        this.receivedMessages = new LinkedBlockingQueue<>();
        this.needsResponse = new LinkedBlockingQueue<>();
        this.interval = 200;
    }

    public static NetworkCommunicator getInstance()
    {
        if (instance == null)
            instance = new NetworkCommunicator();

        return instance;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                String host = this.hosts.take();
                Integer port = this.ports.take();
                Boolean responseRequest = this.needsResponse.take();
                JSONObject currentMessage = this.messages.take();
                JSONObject receivedMessage = null;

                Log.d("IP and port", host + ":" + port);
                Log.d("Message", currentMessage.toString());
                if (this.socket == null || this.socket.isClosed() || !this.socket.isConnected())
                    this.connect(host, port);

                this.sendMessage(currentMessage.toString());

                if(responseRequest)
                {
                    try
                    {
                        if ((receivedMessage = new JSONObject(this.receiveMessage())).length() != 0)
                            this.receivedMessages.add(receivedMessage);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.e("Error", "No message received from host");
                        this.receivedMessages.add(receivedMessage);
                    }
                }
                this.disconnect();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }

    private void connect(String host, int port)
    {
        try
        {
            if (this.socket == null || this.socket.isClosed() || !this.socket.isConnected())
            {
                this.socket = new Socket(host, port);
                this.socket.setSoTimeout(10000);
                this.streamOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void disconnect()
    {
        try
        {
            if (this.socket != null && this.socket.isConnected())
            {
                this.streamIn.close();
                this.streamOut.close();
                this.socket.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message)
    {
        try
        {
            streamOut.write(message);
            streamOut.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private String receiveMessage()
    {
        try
        {
            StringBuilder stringBuilder = new StringBuilder();
            char[] buffer = new char[1024];

            while (streamIn.read(buffer) != -1)
                stringBuilder.append(buffer);

            return stringBuilder.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void pushMessageToIP(String IP, Integer port, JSONObject message, boolean needsResponse)
    {
        this.hosts.add(IP);
        this.ports.add(port);
        this.needsResponse.add(needsResponse);
        this.messages.add(message);
    }

    public JSONObject getResponse()
    {
        try
        {
            return this.receivedMessages.take();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void setInterval(Integer value)
    {
        this.interval = value;

        if (this.socket != null)
            try
            {
                this.socket.setSoTimeout(this.interval);
            }
            catch (SocketException e)
            {
            }
    }

    public void clearQueues()
    {
        this.hosts.clear();
        this.needsResponse.clear();
        this.messages.clear();
        this.ports.clear();
        this.receivedMessages.clear();
    }

    public void clearAll()
    {
        clearQueues();

        if (this.socket != null)
            if(!this.socket.isClosed() || this.socket.isConnected())
                disconnect();
    }

    public int getInterval()
    {
        return this.interval;
    }
}
