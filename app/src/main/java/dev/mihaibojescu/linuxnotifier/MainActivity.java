package dev.mihaibojescu.linuxnotifier;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    private String message;
    private String IP;
    private PingDevices pingService;
    private Queue<String> notifications;
    private NetworkCommunicator communicatorThread;
    private NotificationReceiver notificationReceiver;
    private String myIpAddress;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        this.myIpAddress = getLocalIpAddress();
        this.notifications = new LinkedList<String>();
        this.pingService = new PingDevices(myIpAddress, this);
        this.pingService.execute();
        this.communicatorThread = new NetworkCommunicator();
        this.communicatorThread.start();

        this.notificationReceiver = new NotificationReceiver(this);

        this.pingService.clearPingList();
        for(int i = 0; i < 255; i++)
            this.pingService.addHost(myIpAddress.substring(0, myIpAddress.lastIndexOf('.')) + '.' + String.valueOf(i));


    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refreshdevices:
                this.pingService.clearPingList();
                for(int i = 0; i < 255; i++)
                    this.pingService.addHost(myIpAddress.substring(0, myIpAddress.lastIndexOf('.')) + '.' + String.valueOf(i));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void sendpacket(View V) {
        try {
            String spinnerText = ((Spinner) findViewById(R.id.spinner)).getSelectedItem().toString();
            String message = ((EditText) findViewById(R.id.editText)).getText().toString();
            this.communicatorThread.pushMessageToIP(spinnerText, 5005, message);
        }
        catch (Exception e)
        {
            ((TextView)findViewById(R.id.textView)).setText("Error: address can't be null. Refresh the list." + e.toString());
        }
        ((TextView)findViewById(R.id.response)).setText(this.communicatorThread.getMessage());
    }

    //Network stuff
    private boolean amIOnWiFi()
    {
        ConnectivityManager connectivityManager = ((ConnectivityManager)getSystemService
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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