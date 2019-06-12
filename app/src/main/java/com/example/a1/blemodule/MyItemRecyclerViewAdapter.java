package com.example.a1.blemodule;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.a1.blemodule.ItemFragment.OnListFragmentInteractionListener;

import java.util.ArrayList;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final OnListFragmentInteractionListener mListener;
    ArrayList<BluetoothDevice> deviceList;

    public MyItemRecyclerViewAdapter(ArrayList<BluetoothDevice> items, OnListFragmentInteractionListener listener) {
        deviceList = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = deviceList.get(position);
        holder.name.setText(deviceList.get(position).getName());
        holder.address.setText(deviceList.get(position).getAddress());
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView rssi;
        public final TextView name;
        public final TextView address;
        public final RelativeLayout item_layout;
        public BluetoothDevice mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            rssi = (TextView) view.findViewById(R.id.rssi);
            name = (TextView) view.findViewById(R.id.name);
            address = (TextView) view.findViewById(R.id.address);
            item_layout = (RelativeLayout) view.findViewById(R.id.item_layout);

            item_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener!=null){
                        mListener.onListFragmentInteraction(deviceList.get(getAdapterPosition()));

                    }
                }
            });

        }
    }
}
