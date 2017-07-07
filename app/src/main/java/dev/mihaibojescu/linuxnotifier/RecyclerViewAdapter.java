package dev.mihaibojescu.linuxnotifier;

import android.support.v7.widget.RecyclerView;
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

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView devicename;
        public TextView devicemac;
        public ViewHolder(View view) {
            super(view);
            devicename = (TextView) view.findViewById(R.id.devicename);
            devicemac = (TextView) view.findViewById(R.id.devicemac);
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
        holder.devicename.setText(device.getName());
        holder.devicemac.setText(device.getMac());
    }

    @Override
    public int getItemCount()
    {
        return dataset.size();
    }
}
