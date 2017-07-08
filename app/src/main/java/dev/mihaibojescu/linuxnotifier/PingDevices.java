package dev.mihaibojescu.linuxnotifier;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by michael on 03.05.2017.
 */

public class PingDevices extends AsyncTask<Void, String, Void> {

    private static int port = 5005;
    private Integer interval;
    private BlockingQueue<String> pingQueue;
    private List<Device> devices;
    private String myIp;
    private MainActivity main;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;

    public PingDevices(String myIP, MainActivity main)
    {
        this.interval = 50;
        this.pingQueue = new LinkedBlockingQueue<>();
        this.main = main;
        this.myIp = myIP;
        this.devices = new ArrayList<>();
        this.recyclerView = (RecyclerView) main.findViewById(R.id.recycler_view);
        recyclerViewAdapter = new RecyclerViewAdapter(devices);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    @Override
    protected Void doInBackground(Void... params) {
        while(true) {
            String devicename;
            String address = null;

            try {
                address = pingQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                devicename = Inet4Address.getByName(address).toString();
                Log.d("Current ip", address);
                if (address != myIp &&
                        isPortUp(address) &&
                        !contains(address))
                    publishProgress(devicename, address);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onProgressUpdate(String... address)
    {
        Log.d("New host", address[0]);
        Device newDev = new Device(address[0], address[1], address[1]);
        devices.add(newDev);
        recyclerViewAdapter.notifyDataSetChanged();
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

    private boolean contains(String address)
    {
        for(int i = 0; i < devices.size(); i++)
            if (devices.get(i).getAddress().equals(address)) return true;
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

    public Device getHostByIndex(int index)
    {
        return devices.get(index);
    }

    public void updateInterval(Integer interval)
    {
        this.interval = interval;
    }
}
