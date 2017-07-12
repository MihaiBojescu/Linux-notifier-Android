package dev.mihaibojescu.linuxnotifier;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by michael on 12.07.2017.
 */

public class DeviceHandler extends AsyncTask<Void, Device, Void> {
    private static DeviceHandler instance = null;
    private MainActivity main;
    private List<Device> devices;
    private BlockingQueue<Device> checkList;

    private DeviceHandler()
    {
        this.devices = new ArrayList<>();
        this.checkList = new LinkedBlockingQueue<>();
    }

    @Override
    protected Void doInBackground(Void... params) {
        while(true) {
            Log.d("exec", "");
            Device device = null;
            try {
                device = this.checkList.take();
                Log.d("", device.getAddress());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isDeviceValid(device) && device != null)
                publishProgress(device);
        }
    }

    @Override
    protected void onProgressUpdate(Device... device)
    {
        this.addDevice(device[0]);
    }

    public static DeviceHandler getInstance()
    {
        if(instance == null)
            instance = new DeviceHandler();

        return instance;
    }

    public static DeviceHandler getInstance(MainActivity main)
    {
        if(instance == null)
            instance = new DeviceHandler();

        instance.setParams(main);
        return instance;
    }

    public void setParams(MainActivity main)
    {
        this.main = main;
    }

    public List<Device> getDeviceList()
    {
        return devices;
    }

    public void getDevicesFromFile()
    {
        IOClass ioClass = IOClass.getInstance();
        ioClass.openFile("devices.json");
        JSONObject result = ioClass.readFile();
        JSONArray names = null;
        JSONArray addresses = null;
        JSONArray macs = null;
        JSONArray pins = null;

        try
        {
            names = result.getJSONArray("name");
            addresses = result.getJSONArray("address");
            macs = result.getJSONArray("macs");
            pins = result.getJSONArray("pins");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        this.devices.clear();
        for(int i = 0; i < names.length(); i++)
        {
            try {
                JSONObject currentName = names.getJSONObject(i);
                JSONObject currentAddress = addresses.getJSONObject(i);
                JSONObject currentMac = macs.getJSONObject(i);
                JSONObject currentPin = pins.getJSONObject(i);

                Device newDevice = new Device(currentName.getString("name"), currentAddress.getString("address"),
                                              currentMac.getString("mac"), currentPin.getString("pin").getBytes());

                addDeviceToCheckList(newDevice);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void addDeviceToCheckList(Device device)
    {
        this.checkList.add(device);
    }

    private void addDevice(Device device)
    {
        this.devices.add(device);
        Log.d("New device", "added device " + device.getAddress());
        main.getAdapter().notifyDataSetChanged();
    }

    private Boolean isDeviceValid(Device device)
    {
        PingService service = PingService.getInstance();
        service.addHost(device.getAddress());

        if(service.wasLastValid())
            return true;
        else
            return false;
    }

    public Device getHostByIndex(int index)
    {
        return devices.get(index);
    }
}
