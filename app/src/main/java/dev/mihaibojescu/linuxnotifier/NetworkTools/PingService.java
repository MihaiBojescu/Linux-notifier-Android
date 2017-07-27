package dev.mihaibojescu.linuxnotifier.NetworkTools;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import dev.mihaibojescu.linuxnotifier.MainActivity;

/**
 * Created by michael on 03.05.2017.
 */

public class PingService extends Thread {

    private static PingService service = null;
    private static int port = 5005;
    private Integer interval;
    private BlockingQueue<String> pingQueue;
    private BlockingQueue<Boolean> responses;
    private String myIp;


    public static PingService getInstance()
    {
        if (service == null)
            service = new PingService();

        return service;
    }

    public static PingService getInstance(String myIP, MainActivity main)
    {
        if (service == null)
            service = new PingService();

        service.setParams(myIP, main);
        return service;
    }

    public void setParams(String myIP, MainActivity main)
    {
        this.interval = 200;
        this.pingQueue = new LinkedBlockingQueue<>();
        this.responses = new LinkedBlockingQueue<>();
        this.myIp = myIP;
    }

    @Override
    public void run()
    {
        while (true)
        {
            String address = null;

            try
            {
                address = pingQueue.take();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            if (address != myIp && isPortUp(address))
                responses.add(true);
            else
                responses.add(false);
        }
    }

    private boolean isPortUp(String address)
    {
        try
        {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(address, port), interval);
            s.close();
            return true;
        }
        catch (IOException e)
        {
            return false;
        }

    }

    public void addHost(String address)
    {
        pingQueue.add(address);
    }

    public Boolean wasLastValid()
    {
        try
        {
            return responses.take();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return true;
    }

    public void clearPingList()
    {
        pingQueue.clear();
    }

    public void updateInterval(Integer interval)
    {
        this.interval = interval;
    }
}
