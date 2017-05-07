package dev.mihaibojescu.linuxnotifier;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by michael on 03.05.2017.
 */

public class PingDevices extends AsyncTask<Void, String, Void> {

    private static int port = 5005;
    private BlockingQueue<String> pingQueue;
    private ArrayAdapter<String> upDevices;
    private String myIp;
    private MainActivity main;

    public PingDevices(String myIP, MainActivity main)
    {
        this.pingQueue = new LinkedBlockingQueue<>();
        this.main = main;
        this.myIp = myIP;
        this.upDevices = new ArrayAdapter<String>(main, android.R.layout.simple_spinner_item);
    }

    @Override
    protected Void doInBackground(Void... params) {
        while(true) {
            InetAddress host = null;
            String address = null;
            try {
                address = this.pingQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                host = Inet4Address.getByName(address);
                if (address != this.myIp &&
                        isPortUp(address) &&
                        !contains(address))
                    publishProgress(address);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onProgressUpdate(String... address)
    {
        Log.d("New host", address[0]);
        this.upDevices.add(address[0]);
        this.upDevices.notifyDataSetChanged();
        ((Spinner)main.findViewById(R.id.spinner)).setAdapter(this.upDevices);
    }

    private boolean isPortUp(String address)
    {
        try
        {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(address, this.port), 150);
            s.close();
            return true;
        } catch (IOException e) {
            return false;
        }

    }

    private boolean contains(String x)
    {
        for(int i = 0; i < this.upDevices.getCount(); i++)
            if (this.upDevices.getItem(i).equals(x)) return true;
        return false;
    }

    public void addHost(String address)
    {
        this.pingQueue.add(address);
    }

    public void clearPingList()
    {
        this.pingQueue.clear();
    }
}
