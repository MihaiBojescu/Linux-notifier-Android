package dev.mihaibojescu.linuxnotifier;

/**
 * Created by michael on 07.07.2017.
 */

public class Device {
    private String name;
    private String mac;

    public Device()
    {

    }

    public Device(String name, String mac)
    {
        this.name = name;
        this.mac = mac;
    }

    public String getName()
    {
        return name;
    }

    public String getMac()
    {
        return mac;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setMac(String mac)
    {
        this.mac = mac;
    }
}
