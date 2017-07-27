package dev.mihaibojescu.linuxnotifier;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by michael on 07.07.2017.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<Device> dataset;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView deviceName;
        public TextView deviceMac;
        public ViewHolder(View view) {
            super(view);
            deviceName = (TextView) view.findViewById(R.id.devicename);
            deviceMac = (TextView) view.findViewById(R.id.devicemac);
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
    }

    @Override
    public int getItemCount()
    {
        return dataset.size();
    }
}