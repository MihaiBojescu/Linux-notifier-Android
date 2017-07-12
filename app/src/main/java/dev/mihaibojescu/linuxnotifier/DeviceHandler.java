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
            Device device = null;
            try {
                device = this.checkList.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isDeviceValid(device) && device != null)
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

                try
                {
                    device.setName(response.getString("name"));
                    device.setMac(response.getString("mac"));
                    device.setStatus(Device.statuses.CONNECTED);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                publishProgress(device);
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
        this.devices.add(device);
        main.getAdapter().notifyDataSetChanged();
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
        Log.d("hello, imma json object", names.toString());

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

    public void addDeviceToCheckList(Device device)
    {
        this.checkList.add(device);
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
