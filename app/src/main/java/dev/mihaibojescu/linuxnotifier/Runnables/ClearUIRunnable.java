package dev.mihaibojescu.linuxnotifier.Runnables;

import java.util.List;

import dev.mihaibojescu.linuxnotifier.DeviceHandlers.Device;
import dev.mihaibojescu.linuxnotifier.MainActivity;

/**
 * Created by michael on 8/13/17.
 */

public class ClearUIRunnable implements Runnable
{
    private List<Device> devices;
    private MainActivity main;


    public ClearUIRunnable(List<Device> devices, MainActivity main)
    {
        this.devices = devices;
        this.main = main;
    }

    @Override
    public void run()
    {
        devices.clear();
        main.getAdapter().notifyDataSetChanged();
    }
}
