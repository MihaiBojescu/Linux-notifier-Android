package dev.mihaibojescu.linuxnotifier;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by michael on 03.05.2017.
 */

public class NetworkCommunicator extends Thread {

    private BlockingQueue<String> hosts;
    private BlockingQueue<Integer> ports;
    private BlockingQueue<String> messages;
    private BlockingQueue<String> receivedMessages;
    private Socket socket;
    private BufferedReader streamIn;
    private BufferedWriter streamOut;

    public NetworkCommunicator()
    {
        this.hosts = new LinkedBlockingQueue<String>();
        this.ports = new LinkedBlockingQueue<Integer>();
        this.messages = new LinkedBlockingQueue<String>();
        this.receivedMessages = new LinkedBlockingQueue<String>();
    }

    @Override
    public void run() {
        while (true) {
            try {
                String host = this.hosts.take();
                Integer port = this.ports.take();
                String currentMessage = this.messages.take();
                String receivedMessage = "";

                Log.d("IP and port", host + ":" + port);
                Log.d("Message", currentMessage);
                if(this.socket == null || this.socket.isClosed() || !this.socket.isConnected())
                    this.connect(host, port);

                this.sendMessage(currentMessage);
                if((receivedMessage = this.receiveMessage()) != null)
                    this.receivedMessages.add(receivedMessage);

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
                streamOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
        }
        return null;
    }

    public void pushMessageToIP(String IP, Integer port, String message)
    {
        this.hosts.add(IP);
        this.ports.add(port);
        this.messages.add(message);
    }

    public String getMessage()
    {
        try {
            return this.receivedMessages.take();
        }
        catch (Exception e) {
            return null;
        }
    }
}
