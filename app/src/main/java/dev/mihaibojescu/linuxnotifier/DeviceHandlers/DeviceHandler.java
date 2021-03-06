package dev.mihaibojescu.linuxnotifier.DeviceHandlers;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import dev.mihaibojescu.linuxnotifier.IO.IOClass;
import dev.mihaibojescu.linuxnotifier.MainActivity;
import dev.mihaibojescu.linuxnotifier.NetworkTools.DiscoverySender;
import dev.mihaibojescu.linuxnotifier.NetworkTools.NetworkCommunicator;
import dev.mihaibojescu.linuxnotifier.NetworkTools.NetworkTools;
import dev.mihaibojescu.linuxnotifier.NetworkTools.PingService;
import dev.mihaibojescu.linuxnotifier.Runnables.AddDeviceUIHandler;
import dev.mihaibojescu.linuxnotifier.Runnables.AuthUIRunnable;
import dev.mihaibojescu.linuxnotifier.Runnables.ClearUIRunnable;
import dev.mihaibojescu.linuxnotifier.Runnables.UpdateUIRunnable;

/**
 * Created by michael on 12.07.2017.
 */

public class DeviceHandler extends Thread
{

    private static DeviceHandler instance = null;
    private MainActivity main;
    private List<Device> devices;
    private BlockingDeque<Device> checkList;
    private Handler UIHandler;


    private DeviceHandler()
    {
        this.devices = new ArrayList<>();
        this.checkList = new LinkedBlockingDeque<>();
        this.UIHandler = new Handler(Looper.getMainLooper());
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
    public void run()
    {
        while (true)
        {
            Device device = null;
            try
            {
                device = this.checkList.takeLast();

                if (isDeviceValid(device))
                    switch (device.getStatus())
                    {
                        case DISCONNECTED:
                            device.setStatus(Device.statuses.WAITING_AUTH);
                        case WAITING_AUTH:
                            authentificateDevice(device);
                            break;
                    }
                else
                {
                    device.setStatus(Device.statuses.DISCONNECTED);
                    this.updateUI();
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void updateUI()
    {
        UIHandler.post(new UpdateUIRunnable(main));
    }

    private void clearUI()
    {
        UIHandler.post(new ClearUIRunnable(devices, main));
    }

    public void addPriorityDeviceToCheckList(Device device)
    {
        this.checkList.addLast(device);
    }

    private void authentificateDevice(final Device device)
    {
        final NetworkCommunicator communicator = NetworkCommunicator.getInstance();
        JSONObject request = Request.createRequest(Request.reasons.AUTHENTIFICATE);
        int lastInterval;

        try
        {
            request.put("name", NetworkTools.getInstance().getMyHostname());
            request.put("address", NetworkTools.getInstance().getLocalIpAddress());
            request.put("pin", device.getPin());

            UIHandler.post(new AuthUIRunnable(device, main, communicator, this));

            lastInterval = communicator.getInterval();
            communicator.setInterval(10000);
            communicator.pushMessageToIP(device.getAddress(), 5005, request, true);
            JSONObject response = communicator.getResponse();
            communicator.setInterval(lastInterval);

            if(response != null && response.getString("response").equals("1"))
            {
                device.setStatus(Device.statuses.CONNECTED);
                this.updateUI();
                this.writeDevicesToFile();
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void getAndCheckDevicesFromFile()
    {
        IOClass ioClass = IOClass.getInstance();
        JSONObject result = ioClass.readFile("devices.json");

        if (result.length() == 0) return;

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
                            currentMac, currentPin);

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
        JSONObject output = new JSONObject();

        try
        {
            JSONArray names = new JSONArray();
            JSONArray addresses = new JSONArray();
            JSONArray macs = new JSONArray();
            JSONArray pins = new JSONArray();

            for (Device currentDevice: this.devices)
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

            ioClass.writeToFile("devices.json", output);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void scanSubnet()
    {
        DiscoverySender.getInstance().discover();
    }

    public void dispatchMessageToAllDevices(JSONObject message)
    {
        NetworkCommunicator communicator = NetworkCommunicator.getInstance();

        for (Device currentDevice: devices)
            if (isDeviceValid(currentDevice))
                if (currentDevice.getStatus() == Device.statuses.CONNECTED)
                    communicator.pushMessageToIP(currentDevice.getAddress(), 5005, message, false);
                else
                {
                    currentDevice.setStatus(Device.statuses.DISCONNECTED);
                    updateUI();
                }
    }

    public void addDevice(Device device)
    {
        UIHandler.post(new AddDeviceUIHandler(devices, device));
        UIHandler.post(new UpdateUIRunnable(main));
    }

    private Boolean isDeviceValid(Device device)
    {
        PingService service = PingService.getInstance();
        service.addHost(device.getAddress());

        return service.wasLastValid();
    }

    public Boolean deviceExists(Device device)
    {
        for (Device currentDevice: this.devices)
            if (currentDevice.getMac().equals(device.getMac()))
                return true;

        return false;
    }

    public Boolean deviceExists(String mac)
    {
        for (Device currentDevice: this.devices)
            if (currentDevice.getMac().equals(mac))
                return true;

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

    public void clearCache()
    {
        IOClass ioClass = IOClass.getInstance();
        ioClass.writeToFile("devices.json", new JSONObject());
        this.checkList.clear();
        this.clearUI();
    }
}
