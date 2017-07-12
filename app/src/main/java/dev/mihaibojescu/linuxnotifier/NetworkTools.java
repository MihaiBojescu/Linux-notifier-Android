package dev.mihaibojescu.linuxnotifier;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by michael on 10.07.2017.
 */

public class NetworkTools {

    private Activity activity;

    public NetworkTools(Activity main)
    {
        activity = main;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean amIOnWiFi()
    {
        ConnectivityManager connectivityManager = ((ConnectivityManager)activity.getSystemService
                (Context.CONNECTIVITY_SERVICE));
        boolean isWifiConnected = false;
        Network[] networks = connectivityManager.getAllNetworks();
        if (networks == null) {
            isWifiConnected = false;
        } else {
            for (Network network : networks) {
                NetworkInfo info = connectivityManager.getNetworkInfo(network);
                if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (info.isAvailable() && info.isConnected()) {
                        isWifiConnected = true;
                        break;
                    }
                }
            }
        }
        return isWifiConnected;
    }

    public String getLocalIpAddress() {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            // Do whatever
        }try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Error: ", ex.toString());
        }
        return null;
    }
}
