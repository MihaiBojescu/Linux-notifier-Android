package dev.mihaibojescu.linuxnotifier;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        NetworkTools tools = new NetworkTools(this);
        IOClass ioClass = IOClass.getInstance();
        ioClass.setContext(this.getApplicationContext());

        this.myIpAddress = tools.getLocalIpAddress();
        this.notifications = new LinkedList<>();
        this.pingService = PingService.getInstance(myIpAddress, this);
        this.pingService.start();
        this.communicatorThread = new NetworkCommunicator();
        this.communicatorThread.start();

        this.deviceHandler = DeviceHandler.getInstance(this);
        this.deviceHandler.execute();

        this.notificationBroadcastReceiver = new NotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("dev.mihaibojescu.linuxnotifier");
        registerReceiver(notificationBroadcastReceiver, intentFilter);


        this.pingService.clearPingList();
        for(int i = 0; i < 255; i++)
            this.deviceHandler.addDeviceToCheckList(new Device("test", this.myIpAddress.substring(0, myIpAddress.lastIndexOf('.')) + '.' + String.valueOf(i), "test", "test"));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(new RecyclerViewTouchHandler(getApplicationContext(), recyclerView, new ClickListenerExample()));
        recyclerViewAdapter = new RecyclerViewAdapter(deviceHandler.getDeviceList());
        recyclerView.setAdapter(recyclerViewAdapter);

        ((SeekBar)findViewById(R.id.seekBar)).setProgress(100);
        pingService.updateInterval(((SeekBar)findViewById(R.id.seekBar)).getProgress());
        ((SeekBar)findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBarHandler());
    }

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

    public RecyclerViewAdapter getAdapter()
    {
        return this.recyclerViewAdapter;
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.refreshdevices:
                String address = myIpAddress.substring(0, myIpAddress.lastIndexOf('.')) + '.';

                for(int i = 0; i < 255; i++)
                    this.deviceHandler.addDeviceToCheckList(new Device("", address + String.valueOf(i), "", ""));
                return true;

            case R.id.renewkeys:
                Authentification auth = Authentification.getInstance();
                auth.createNewKeys(2048);
                try {
                    ((TextView)findViewById(R.id.response)).setText(auth.getPublicKey().getEncoded().toString());
                } catch(Exception e) {

                }
                Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class ClickListenerExample implements ClickListener
    {
        @Override
        public void onClick(View view, int position) {
            communicatorThread.pushMessageToIP(deviceHandler.getHostByIndex(position).getAddress(), 5005, ((EditText)findViewById(R.id.editText)).getText().toString());
        }

        @Override
        public void onLongClick(View view, int position) {

        }
    }

    public class SeekBarHandler implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
            pingService.updateInterval(progress);
            communicatorThread.setInterval(progress);
        }

        @Override
        public void onStartTrackingTouch(android.widget.SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(android.widget.SeekBar seekBar) {

        }
    }

    public class NotificationBroadcastReceiver extends android.content.BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, intent.getStringExtra("notification_event"), Toast.LENGTH_SHORT).show();
        }
    }
}