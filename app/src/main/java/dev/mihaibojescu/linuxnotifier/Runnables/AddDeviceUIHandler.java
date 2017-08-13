package dev.mihaibojescu.linuxnotifier.Runnables;

import java.util.List;

import dev.mihaibojescu.linuxnotifier.Crypto.CryptHandler;
import dev.mihaibojescu.linuxnotifier.DeviceHandlers.Device;
import dev.mihaibojescu.linuxnotifier.MainActivity;

/**
 * Created by michael on 8/13/17.
 */

public class AddDeviceUIHandler implements Runnable
{
    private List<Device> devices;
    private Device device;


    public AddDeviceUIHandler(List<Device> devices, Device device)
    {
        this.devices = devices;
        this.device = device;
    }

    @Override
    public void run()
    {
        devices.add(device);
        if(device.getStatus() == Device.statuses.NEW)
            device.setPin(CryptHandler.getInstance().createPin(6));
    }
}
