package dev.mihaibojescu.linuxnotifier;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import dev.mihaibojescu.linuxnotifier.DeviceHandlers.DeviceHandler;
import dev.mihaibojescu.linuxnotifier.IO.IOClass;
import dev.mihaibojescu.linuxnotifier.NetworkTools.DiscoveryReceiver;
import dev.mihaibojescu.linuxnotifier.NetworkTools.DiscoverySender;
import dev.mihaibojescu.linuxnotifier.NetworkTools.NetworkCommunicator;
import dev.mihaibojescu.linuxnotifier.NetworkTools.NetworkTools;
import dev.mihaibojescu.linuxnotifier.NetworkTools.PingService;
import dev.mihaibojescu.linuxnotifier.NotificationHandlers.NotificationBroadcastReceiver;
import dev.mihaibojescu.linuxnotifier.UiTools.ClickListener;
import dev.mihaibojescu.linuxnotifier.UiTools.RecyclerViewAdapter;
import dev.mihaibojescu.linuxnotifier.UiTools.RecyclerViewTouchHandler;

public class MainActivity extends AppCompatActivity
{

    private GoogleApiClient client;
    private PingService pingService;
    private NetworkCommunicator communicatorThread;
    private DeviceHandler deviceHandler;
    private RecyclerViewAdapter recyclerViewAdapter;
    private NotificationBroadcastReceiver notificationBroadcastReceiver;
    private DiscoveryReceiver receiver;
    private DiscoverySender sender;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        NetworkTools.getInstance(this);
        IOClass ioClass = IOClass.getInstance();
        ioClass.setContext(this.getApplicationContext());

        this.pingService = PingService.getInstance();
        this.communicatorThread = NetworkCommunicator.getInstance();
        this.deviceHandler = DeviceHandler.getInstance(this);
        this.sender = DiscoverySender.getInstance();
        this.receiver = DiscoveryReceiver.getInstance();

        startThreads();

        checkAndUpdatePermissions();

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(new RecyclerViewTouchHandler(getApplicationContext(), recyclerView, new ClickListenerHandler()));
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

            case R.id.cleancache:
                this.deviceHandler.clearCache();
                this.communicatorThread.clearAll();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startThreads()
    {
        if (pingService.getState() == Thread.State.NEW)
            this.pingService.start();

        if (communicatorThread.getState() == Thread.State.NEW)
            this.communicatorThread.start();

        if (this.deviceHandler.getState() == Thread.State.NEW)
        {
//            this.deviceHandler.getAndCheckDevicesFromFile();
            this.deviceHandler.start();
        }

        Log.d("MAINACTIVITY", this.sender.getState().toString());
        Log.d("MAINACTIVITY", this.receiver.getState().toString());
        if (this.sender.getState() == Thread.State.NEW)
            this.sender.start();

        if (this.receiver.getState() == Thread.State.NEW)
            this.receiver.start();

        Log.d("MAINACTIVITY", this.sender.getState().toString());
        Log.d("MAINACTIVITY", this.receiver.getState().toString());

    }

    private void checkAndUpdatePermissions()
    {
        if (!isNotificationListenerEnabled())
            buildAndShowNLSdialog();
    }

    private Boolean isNotificationListenerEnabled()
    {
        String listenerString = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");

        if (listenerString != null && !listenerString.equals(""))
        {
            String[] listeners = listenerString.split(":");
            for (String listener : listeners)
            {
                ComponentName componentName = ComponentName.unflattenFromString(listener);
                if (componentName != null)
                    if (componentName.getPackageName().equals(getPackageName()))
                        return true;
            }
        }
        return false;
    }

    private void buildAndShowNLSdialog()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.enable_notification_listener_title);
        dialog.setMessage(R.string.enable_notification_listener);
        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });
        dialog.create().show();
    }

    private class ClickListenerHandler implements ClickListener
    {
        @Override
        public void onClick(View view, int position)
        {
            deviceHandler.addPriorityDeviceToCheckList(deviceHandler.getHostByIndex(position));
        }
    }
}