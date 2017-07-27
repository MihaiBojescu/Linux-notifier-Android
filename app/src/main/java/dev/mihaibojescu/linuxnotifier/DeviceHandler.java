package dev.mihaibojescu.linuxnotifier;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by michael on 12.07.2017.
 */

public class DeviceHandler extends AsyncTask<Void, Device, Void> {
    private static DeviceHandler instance = null;
    private MainActivity main;
    private List<Device> devices;
    private BlockingDeque<Device> checkList;

    private DeviceHandler()
    {
        this.devices = new ArrayList<>();
        this.checkList = new LinkedBlockingDeque<>();
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

    @Override
    protected Void doInBackground(Void... params) {
        while(true)
        {
            Device device = null;
            try
            {
                device = this.checkList.takeLast();

                if(!devices.contains(device))
                    if(isDeviceValid(device))
                        publishProgress(device);

                if (device.getStatus() == Device.statuses.NEW)
                    requestInfo(device);
                else if (device.getStatus() == Device.statuses.WAITING_AUTH)
                    authentificateDevice(device);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Device... device)
    {
        this.addDevice(device[0]);
    }

    private void addDevice(Device device)
    {
        device.setPin(Authentification.getInstance().createPin(4));
        device.setStatus(Device.statuses.NEW);
        this.devices.add(device);
        main.getAdapter().notifyDataSetChanged();
    }

    public void addDeviceToCheckList(Device device)
    {
        this.checkList.addFirst(device);
    }

    public void addPriorityDeviceToCheckList(Device device)
    {
        this.checkList.addLast(device);
    }

    private void requestInfo(Device device)
    {
        NetworkCommunicator communicator = NetworkCommunicator.getInstance();
        JSONObject request = new JSONObject();
        try
        {
            request.put("reason", "request info");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        communicator.pushMessageToIP(device.getAddress(), 5005, request);
        JSONObject response = communicator.getResponse();

        if(response != null)
            try
            {
                device.setName(response.getString("name"));
                device.setMac(response.getString("mac"));
                device.setStatus(Device.statuses.WAITING_AUTH);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
    }

    private void authentificateDevice(Device device)
    {
        NetworkCommunicator communicator = NetworkCommunicator.getInstance();
        JSONObject request = new JSONObject();
        int lastInterval;

        try
        {
            request.put("reason", "auth");
            request.put("name", NetworkTools.getInstance().getMyHostname());
            request.put("address", NetworkTools.getInstance().getLocalIpAddress());
            request.put("pin", device.getPin());
            Log.d("made json!", request.toString());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        communicator.pushMessageToIP(device.getAddress(), 5005, request);

        lastInterval = communicator.getInterval();
        communicator.setInterval(10000);
        JSONObject response = communicator.getResponse();
        communicator.setInterval(lastInterval);

        if(response != null)
            try
            {
                if(response.get("response") == "1")
                {
                    device.setStatus(Device.statuses.CONNECTED);
                    this.writeDevicesToFile();
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
    }

    public void getDevicesFromFile()
    {
        IOClass ioClass = IOClass.getInstance();
        ioClass.openFile("devices.json");
        JSONObject result = ioClass.readFile();

        if(result == null) return;

        JSONArray names = null;
        JSONArray addresses = null;
        JSONArray macs = null;
        JSONArray pins = null;

        try
        {
            names = result.getJSONArray("name");
            addresses = result.getJSONArray("address");
            macs = result.getJSONArray("mac");
            pins = result.getJSONArray("pin");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        for(int i = 0; i < names.length(); i++)
        {
            try {
                String currentName = names.getString(i);
                String currentAddress = addresses.getString(i);
                String currentMac = macs.getString(i);
                String currentPin = pins.getString(i);

                Device newDevice = new Device(currentName, currentAddress,
                                              currentMac, currentPin.getBytes());

                addDeviceToCheckList(newDevice);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void writeDevicesToFile()
    {
        IOClass ioClass = IOClass.getInstance();
        ioClass.openFile("devices.json");

        JSONObject output = new JSONObject();
        try
        {
            for(Device currentDevice: this.devices)
            {
                output.put("name", currentDevice.getName());
                output.put("address", currentDevice.getAddress());
                output.put("mac", currentDevice.getMac());
                output.put("pin", currentDevice.getPin());
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        ioClass.writeToFile(output);
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

    public List<Device> getDeviceList()
    {
        return devices;
    }
}
