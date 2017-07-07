package dev.mihaibojescu.linuxnotifier;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by michael on 03.05.2017.
 */

public class PingDevices extends AsyncTask<Void, String, Void> {

    private static int port = 5005;
    private Integer interval;
    private BlockingQueue<String> pingQueue;
    private ArrayAdapter<String> upDevices;
    private List<Device> devices;
    private String myIp;
    private MainActivity main;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;

    public PingDevices(String myIP, MainActivity main, Context context)
    {
        this.interval = 50;
        this.pingQueue = new LinkedBlockingQueue<>();
        this.main = main;
        this.myIp = myIP;
        this.devices = new ArrayList<Device>();
        this.upDevices = new ArrayAdapter<String>(main, android.R.layout.simple_spinner_item);
        this.recyclerView = (RecyclerView) main.findViewById(R.id.recycler_view);
        recyclerViewAdapter = new RecyclerViewAdapter(devices);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    protected Void doInBackground(Void... params) {
        while(true) {
            InetAddress host = null;
            String address = null;
            try {
                address = pingQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                host = Inet4Address.getByName(address);
                Log.d("Current ip", address);
                if (address != myIp &&
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
        upDevices.add(address[0]);
        upDevices.notifyDataSetChanged();
        ((Spinner)main.findViewById(R.id.spinner)).setAdapter(upDevices);
        Device newDev = new Device("New device!", address[0]);
        devices.add(newDev);
        recyclerViewAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private boolean isPortUp(String address)
    {
        try
        {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(address, port), interval);
            s.close();
            return true;
        } catch (IOException e) {
            return false;
        }

    }

    private boolean contains(String x)
    {
        for(int i = 0; i < upDevices.getCount(); i++)
            if (upDevices.getItem(i).equals(x)) return true;
        return false;
    }

    public void addHost(String address)
    {
        pingQueue.add(address);
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
