package dev.mihaibojescu.linuxnotifier.DeviceHandlers;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import dev.mihaibojescu.linuxnotifier.Crypto.CryptHandler;
import dev.mihaibojescu.linuxnotifier.IO.IOClass;
import dev.mihaibojescu.linuxnotifier.MainActivity;
import dev.mihaibojescu.linuxnotifier.NetworkTools.NetworkCommunicator;
import dev.mihaibojescu.linuxnotifier.NetworkTools.NetworkTools;
import dev.mihaibojescu.linuxnotifier.NetworkTools.PingService;

/**
 * Created by michael on 12.07.2017.
 */

public class DeviceHandler extends AsyncTask<Void, Device, Void>
{

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
        if (instance == null)
            instance = new DeviceHandler();

        return instance;
    }

    public static DeviceHandler getInstance(MainActivity main)
    {
        if (instance == null)
            instance = new DeviceHandler();

        instance.setParams(main);
        return instance;
    }

    public void setParams(MainActivity main)
    {
        this.main = main;
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        while (true)
        {
            Device device = null;
            try
            {
                device = this.checkList.takeLast();

                if (!devices.contains(device))
                {
                    if (isDeviceValid(device))
                    {
                        publishProgress(device);
                        switch(device.getStatus())
                        {
                            case NEW:
                                Log.d("device info", device.getName() + " " + device.getStatus() + " " + device.getAddress() + " " + device.getMac() + ".");
                                requestInfo(device);
                                Log.d("device info after", device.getName() + " " + device.getStatus() + " " + device.getAddress() + " " + device.getMac() + ".");
                                break;
                            case WAITING_AUTH:
                                authentificateDevice(device);
                                break;
                        }
                    }
                }
                else
                {
                    switch(device.getStatus())
                    {
                        case NEW:
                            requestInfo(device);
                            break;
                        case WAITING_AUTH:
                            authentificateDevice(device);
                            break;
                    }
                }
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
        if (device != null && device[0] != null)
            this.addDevice(device[0]);
        else
            main.getAdapter().notifyDataSetChanged();
    }

    private void addDevice(Device device)
    {
        device.setPin(CryptHandler.getInstance().createPin(4));
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
        JSONObject request = Request.createRequest(Request.reasons.REQUEST_INFO);

        communicator.pushMessageToIP(device.getAddress(), 5005, request, true);
        JSONObject response = communicator.getResponse();

        if (response != null)
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
        JSONObject request = Request.createRequest(Request.reasons.AUTHENTIFICATE);
        int lastInterval;

        try
        {
            request.put("name", NetworkTools.getInstance().getMyHostname());
            request.put("address", NetworkTools.getInstance().getLocalIpAddress());
            request.put("pin", device.getPin());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        communicator.pushMessageToIP(device.getAddress(), 5005, request, true);

        lastInterval = communicator.getInterval();
        communicator.setInterval(10000);
        JSONObject response = communicator.getResponse();
        communicator.setInterval(lastInterval);

        if (response != null)
            try
            {
                if (response.getString("response").equals("1"))
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

    public void dispatchMessageToAllDevices(JSONObject message)
    {
        NetworkCommunicator communicator = NetworkCommunicator.getInstance();

        for(Device currentDevice: devices)
            if(currentDevice.getStatus() == Device.statuses.CONNECTED)
                communicator.pushMessageToIP(currentDevice.getAddress(), 5005, message, false);
    }

    public void getAndCheckDevicesFromFile()
    {
        IOClass ioClass = IOClass.getInstance();
        ioClass.openFile("devices.json");
        JSONObject result = ioClass.readFile();

        if (result == null) return;

        try
        {
            JSONArray names = result.getJSONArray("name");
            JSONArray addresses = result.getJSONArray("address");
            JSONArray macs = result.getJSONArray("mac");
            JSONArray pins = result.getJSONArray("pin");

            for (int i = 0; i < names.length(); i++)
            {
                try
                {
                    String currentName = names.getString(i);
                    String currentAddress = addresses.getString(i);
                    String currentMac = macs.getString(i);
                    String currentPin = pins.getString(i);

                    Device newDevice = new Device(currentName, currentAddress,
                            currentMac, currentPin.getBytes());

                    if(isDeviceValid(newDevice))
                    {
                        newDevice.setStatus(Device.statuses.CONNECTED);
                        addPriorityDeviceToCheckList(newDevice);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void writeDevicesToFile()
    {
        IOClass ioClass = IOClass.getInstance();
        ioClass.openFile("devices.json");
        JSONObject output = new JSONObject();

        try
        {
            JSONArray names = new JSONArray();
            JSONArray addresses = new JSONArray();
            JSONArray macs = new JSONArray();
            JSONArray pins = new JSONArray();

            for (Device currentDevice : this.devices)
            {
                names.put(currentDevice.getName());
                addresses.put(currentDevice.getAddress());
                macs.put(currentDevice.getMac());
                pins.put(currentDevice.getPin());
            }

            output.put("name", names);
            output.put("address", addresses);
            output.put("mac", macs);
            output.put("pin", pins);

            ioClass.writeToFile(output);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private Boolean isDeviceValid(Device device)
    {
        PingService service = PingService.getInstance();
        service.addHost(device.getAddress());

        if (service.wasLastValid())
            return true;
        else
            return false;
    }

    public void cleanCache()
    {
        IOClass ioClass = IOClass.getInstance();
        ioClass.openFile("devices.json");
        ioClass.writeToFile(new JSONObject());
        this.devices.clear();
        this.checkList.clear();
        this.publishProgress(null);
    }

    public void scanSubnet()
    {
        String myIP = NetworkTools.getInstance().getLocalIpAddress();
        String address = myIP.substring(0, myIP.lastIndexOf('.')) + '.';
        PingService.getInstance().clearPingList();

        for (int i = 2; i < 254; i++)
        {
            Device device = new Device();
            device.setAddress(address + String.valueOf(i));
            this.addDeviceToCheckList(device);
        }
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
