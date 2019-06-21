package com.example.a1.blemodule;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;


public class ItemFragment extends DialogFragment {

    public static final String TAG = "DeviceListBTActivity";
    private static final long SCAN_PERIOD = 10000; //scanning for 10 seconds
    ArrayList<BTModel> deviceList = new ArrayList<>();
    MyItemRecyclerViewAdapter adapter;

    private OnListFragmentInteractionListener mListener;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private boolean mScanning;
    private BluetoothLeScanner scanner;
    private Button rescan;

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

        rescan = view.findViewById(R.id.btn_rescan);
        mHandler = new Handler();

        RecyclerView recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyItemRecyclerViewAdapter(deviceList, mListener);
        recyclerView.setAdapter(adapter);
        scanLeDevice(true);

        rescan.setOnClickListener(v -> {
            deviceList.clear();
            adapter.notifyDataSetChanged();
            if (mScanning == false) scanLeDevice(true);
            else dismiss();
        });
    }

    /**
     * 킷캣 , 롤리팝 분기 처리
     *
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {

        if (enable) {
            BluetoothModule.getInstance().disconnect();
            //롤리팝 기준으로 분기 처리 (킷캣 별도 처리)
            mHandler.postDelayed(() -> {
                scanner.stopScan(scanCallback);
                mScanning = false;
                rescan.setText("다시찾기");
            }, SCAN_PERIOD);

            mScanning = true;

            final BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            scanner = mBluetoothAdapter.getBluetoothLeScanner();
            List<ScanFilter> scanFilters = new ArrayList<>();
            ScanFilter scanFilter = new ScanFilter.Builder()
//                    .setServiceUuid(ParcelUuid.fromString(IOBEDApplication.IOBED_SERIVCE.toString()))
                    .build();

            scanFilters.add(scanFilter);
            ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
            scanner.startScan(scanFilters, scanSettings, scanCallback);
            rescan.setText("취소");

        }
    }

    public ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);

            Log.d(TAG, "onScanResult: " + result.getDevice().getName() + " : FOUND");
            for (int i = 0; i < deviceList.size(); i++) {

                if (deviceList.get(i).getMac().equals(result.getDevice().getAddress())) {
                    return;
                }
            }

            addDevice(new BTModel(result.getDevice().getName(), result.getDevice().getAddress(), result.getRssi()));
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG, "onBatchScanResults: " + results.toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private void addDevice(BTModel device) {

        deviceList.add(device);
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
        if(mScanning){
            scanner.stopScan(scanCallback);
        }
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(BTModel item);
    }
}
