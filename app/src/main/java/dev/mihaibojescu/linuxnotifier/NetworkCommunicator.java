package dev.mihaibojescu.linuxnotifier;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by michael on 03.05.2017.
 */

public class NetworkCommunicator extends Thread {

    private static NetworkCommunicator instance = null;
    private BlockingQueue<String> hosts;
    private BlockingQueue<Integer> ports;
    private BlockingQueue<JSONObject> messages;
    private BlockingQueue<JSONObject> receivedMessages;
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
        this.interval = 200;
    }

    public static NetworkCommunicator getInstance()
    {
        if(instance == null)
            instance = new NetworkCommunicator();

        return instance;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String host = this.hosts.take();
                Integer port = this.ports.take();
                JSONObject currentMessage = this.messages.take();
                JSONObject receivedMessage = null;

                Log.d("IP and port", host + ":" + port);
                Log.d("Message", currentMessage.toString());
                if(this.socket == null || this.socket.isClosed() || !this.socket.isConnected())
                    this.connect(host, port);

                this.sendMessage(currentMessage.toString());
                try
                {
                    if ((receivedMessage = new JSONObject(this.receiveMessage())) != null)
                        this.receivedMessages.add(receivedMessage);
                }
                catch (Exception e)
                {
                    Log.e("Error", "No message received from host");
                    this.receivedMessages.add(receivedMessage);
                }

                this.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void connect(String host, int port)
    {
        try {
            if(this.socket == null || this.socket.isClosed() || !this.socket.isConnected())
            {
                this.socket = new Socket(host, port);
                this.socket.setSoTimeout(this.interval);
                this.streamOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
        }
        catch (IOException e)
        {
        }
    }

    private void disconnect()
    {
        try {
            if(this.socket != null && this.socket.isConnected())
            {
                this.streamIn.close();
                this.streamOut.close();
                this.socket.close();
            }
        }
        catch (IOException e)
        {
        }
    }

    private void sendMessage(String message){
        try {
            streamOut.write(message);
            streamOut.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private String receiveMessage(){
        try {
            String result = "";
            int charactersRead;
            char[] buffer = new char[1024];

            while ((charactersRead = streamIn.read(buffer)) != -1)
                result += new String(buffer).substring(0, charactersRead);

            return result;
        }
        catch (IOException e)
        {
            return null;
        }
    }

    public void pushMessageToIP(String IP, Integer port, JSONObject message)
    {
        this.hosts.add(IP);
        this.ports.add(port);
        this.messages.add(message);
    }

    public JSONObject getResponse()
    {
        try {
            return this.receivedMessages.take();
        }
        catch (Exception e) {
            return null;
        }
    }

    public void setInterval(Integer value) {
        this.interval = value;
        if(this.socket != null)
            try {
                this.socket.setSoTimeout(this.interval);
            }
            catch(SocketException e)
            {
            }
    }
}
