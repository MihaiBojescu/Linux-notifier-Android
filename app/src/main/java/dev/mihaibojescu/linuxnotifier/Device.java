package dev.mihaibojescu.linuxnotifier;

/**
 * Created by michael on 07.07.2017.
 */

public class Device {
    private String name;
    private String address;
    private String mac;
    private byte[] pin;
    private int status;

    public enum statuses
    {
        NEW,
        CONNECTED
    };

    public Device()
    {

    }

    public Device(String name, String address, String mac)
    {
        this.name = name;
        this.address = address;
        this.mac = mac;
    }

    public String getName()
    {
        return name;
    }

    public String getAddress()
    {
        return address;
    }

    public String getMac()
    {
        return mac;
    }

    public int getStatus()
    {
        return status;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public void setMac(String mac)
    {
        this.mac = mac;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }
}
