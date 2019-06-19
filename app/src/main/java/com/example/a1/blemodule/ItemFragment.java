package com.example.a1.blemodule;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;


public class ItemFragment extends DialogFragment {

    ArrayList<BTModel> deviceList = new ArrayList<>();
    MyItemRecyclerViewAdapter adapter;

    private OnListFragmentInteractionListener mListener;
    private Button btn_rescan;

    public static ItemFragment newInstance() {

        Bundle args = new Bundle();
        ItemFragment fragment = new ItemFragment();
//        args.putParcelable(KEY_DATA, items);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn_rescan = view.findViewById(R.id.btn_rescan);
        btn_rescan.setOnClickListener(v -> {
            mListener.onReScan();
        });

        RecyclerView recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyItemRecyclerViewAdapter(deviceList, mListener);
        recyclerView.setAdapter(adapter);
    }


    public void addDevice(BTModel model) {
        if (deviceList.contains(model)) {
            return;
        }
        deviceList.add(model);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(BTModel item);
        void onReScan();
    }
}
