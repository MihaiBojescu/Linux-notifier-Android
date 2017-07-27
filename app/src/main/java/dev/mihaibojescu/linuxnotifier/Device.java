package dev.mihaibojescu.linuxnotifier;

/**
 * Created by michael on 07.07.2017.
 */

public class Device {
    private String name;
    private String address;
    private String mac;
    private byte[] pin;
    private statuses status;

    public enum statuses
    {
        NEW,
        WAITING_AUTH,
        CONNECTED
    };

    public Device()
    {

    }

    public Device(String name, String address, String mac, byte[] pin)
    {
        this.name = name;
        this.address = address;
        this.mac = mac;
        this.pin = pin;
        this.status = statuses.NEW;
    }

    public Device(String name, String address, String mac, String pin)
    {
        this.name = name;
        this.address = address;
        this.mac = mac;
        this.pin = pin.getBytes();
        this.status = statuses.NEW;
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

    public byte[] getPin()
    {
        return pin;
    }

    public statuses getStatus()
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

    public void setPin(byte[] pin)
    {
        this.pin = pin;
    }

    public void setStatus(statuses status)
    {
        this.status = status;
    }
}
