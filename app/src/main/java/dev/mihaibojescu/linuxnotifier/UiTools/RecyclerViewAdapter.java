package dev.mihaibojescu.linuxnotifier.UiTools;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import dev.mihaibojescu.linuxnotifier.DeviceHandlers.Device;
import dev.mihaibojescu.linuxnotifier.R;

/**
 * Created by michael on 07.07.2017.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<Device> dataset;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView deviceName;
        public TextView deviceMac;
        public TextView connectionStatus;
        public ViewHolder(View view) {
            super(view);
            deviceName = (TextView) view.findViewById(R.id.devicename);
            deviceMac = (TextView) view.findViewById(R.id.devicemac);
            connectionStatus = (TextView) view.findViewById(R.id.connectionStatus);
        }
    }

    public RecyclerViewAdapter(List<Device> deviceList)
    {
        dataset = deviceList;

    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerviewrow, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        Device device = dataset.get(position);
        holder.deviceName.setText(device.getName());
        holder.deviceMac.setText(device.getMac());
        if(device.getStatus() == Device.statuses.CONNECTED)
            holder.connectionStatus.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount()
    {
        return dataset.size();
    }

    public void clear()
    {
        this.dataset.clear();
        notifyDataSetChanged();
    }
}
