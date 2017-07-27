package dev.mihaibojescu.linuxnotifier;

import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.LinkedList;
import java.util.Queue;

import dev.mihaibojescu.linuxnotifier.Crypto.CryptHandler;
import dev.mihaibojescu.linuxnotifier.IO.IOClass;
import dev.mihaibojescu.linuxnotifier.NetworkTools.NetworkCommunicator;
import dev.mihaibojescu.linuxnotifier.NetworkTools.NetworkTools;
import dev.mihaibojescu.linuxnotifier.NetworkTools.PingService;
import dev.mihaibojescu.linuxnotifier.NotificationHandlers.NotificationBroadcastReceiver;
import dev.mihaibojescu.linuxnotifier.DeviceHandlers.DeviceHandler;
import dev.mihaibojescu.linuxnotifier.UiTools.ClickListener;
import dev.mihaibojescu.linuxnotifier.UiTools.RecyclerViewAdapter;
import dev.mihaibojescu.linuxnotifier.UiTools.RecyclerViewTouchHandler;

public class MainActivity extends AppCompatActivity
{

    private GoogleApiClient client;
    private PingService pingService;
    private Queue<String> notifications;
    private NetworkCommunicator communicatorThread;
    private DeviceHandler deviceHandler;
    private RecyclerViewAdapter recyclerViewAdapter;
    private String myIpAddress;
    private NotificationBroadcastReceiver notificationBroadcastReceiver;

    
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        NetworkTools tools = NetworkTools.getInstance(this);
        IOClass ioClass = IOClass.getInstance();
        ioClass.setContext(this.getApplicationContext());

        this.myIpAddress = tools.getLocalIpAddress();
        this.notifications = new LinkedList<>();
        this.pingService = PingService.getInstance(myIpAddress, this);
        this.pingService.start();
        this.communicatorThread = NetworkCommunicator.getInstance();
        this.communicatorThread.start();


        SeekBar seekbar = ((SeekBar)findViewById(R.id.seekBar));
        pingService.updateInterval(seekbar.getProgress());
        communicatorThread.setInterval(seekbar.getProgress());
        seekbar.setOnSeekBarChangeListener(new SeekBarHandler());

        this.deviceHandler = DeviceHandler.getInstance(this);
        this.deviceHandler.execute();
        this.deviceHandler.getDevicesFromFile();
        if (deviceHandler.getDeviceList().size() == 0)
            deviceHandler.scanSubnet();

        this.notificationBroadcastReceiver = new NotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("dev.mihaibojescu.linuxnotifier");
        registerReceiver(notificationBroadcastReceiver, intentFilter);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(new RecyclerViewTouchHandler(getApplicationContext(), recyclerView, new ClickListenerExample()));
        recyclerViewAdapter = new RecyclerViewAdapter(deviceHandler.getDeviceList());
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    public Action getIndexApiAction()
    {
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

    public RecyclerViewAdapter getAdapter()
    {
        return this.recyclerViewAdapter;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop()
    {
        super.onStop();
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public void onDestroy()
    {
        if (notificationBroadcastReceiver != null)
        {
            unregisterReceiver(notificationBroadcastReceiver);
            notificationBroadcastReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.refreshdevices:
                deviceHandler.scanSubnet();
                return true;

            case R.id.renewkeys:
                CryptHandler auth = CryptHandler.getInstance();
                auth.createNewKeys(2048);

                try
                {
                    Toast.makeText(this.getApplicationContext(), auth.getPublicKey().getEncoded().toString(), Toast.LENGTH_LONG).show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.cleancache:
                this.deviceHandler.cleanCache();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class ClickListenerExample implements ClickListener
    {
        @Override
        public void onClick(View view, int position)
        {
            Log.d("Info: ", deviceHandler.getHostByIndex(position).getStatus().toString());
            deviceHandler.addPriorityDeviceToCheckList(deviceHandler.getHostByIndex(position));
        }

        @Override
        public void onLongClick(View view, int position)
        {

        }
    }

    public class SeekBarHandler implements SeekBar.OnSeekBarChangeListener
    {
        @Override
        public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser)
        {
            pingService.updateInterval(progress);
            communicatorThread.setInterval(progress);
        }

        @Override
        public void onStartTrackingTouch(android.widget.SeekBar seekBar)
        {

        }

        @Override
        public void onStopTrackingTouch(android.widget.SeekBar seekBar)
        {

        }
    }
}