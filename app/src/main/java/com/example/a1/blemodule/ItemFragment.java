package com.example.a1.blemodule;

import android.Manifest;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;


public class ItemFragment extends DialogFragment {

    public static final String TAG = "DeviceListBTActivity";
    private static final String KEY_DATA = "KEY_DATA";
    private static final int REQUEST_ENABLE_BT = 99;
    private static final long SCAN_PERIOD = 10000; //scanning for 10 seconds
    ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    MyItemRecyclerViewAdapter adapter;
    public BluetoothAdapter.LeScanCallback scanLeCallBack = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {

            if (deviceList.contains(device)) //중복 검색 제외
                return;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addDevice(device);
                }
            });
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);

            if (deviceList.contains(result.getDevice())) //중복 검색 제외
                return;

            addDevice(result.getDevice());
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
    private OnListFragmentInteractionListener mListener;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private boolean mScanning;
    private BluetoothLeScanner scanner;
    private Button cancelButton;

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

        cancelButton = view.findViewById(R.id.btn_cancel);
        mHandler = new Handler();

        checkPermissionBluetooth();

        RecyclerView recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyItemRecyclerViewAdapter(deviceList, mListener);
        recyclerView.setAdapter(adapter);
    }

    private void checkPermissionBluetooth() {
        /**
         * 로직 : 1. 블루투스 검사
         *            1-1. 블루투스 연결 된 경우 : 2 이동
         *            1-2. 블루투스연결 안된 경우 : 설정 창 띄움
         *                 1-2-1. 설정에서 허용한경우 :  2 이동
         *                 1-2-2. 설정에서 허용하지 않은 경우 : finish
         *        2. 위치 권한 검사
         *              2-1. 위치 권한 허용 된 경우 : 스캔 시작
         *              2-2. 위치 권한 허용 안된 경우 : 허용창 띄움
         *                  2-2-1. 허용창에서 허용한 경우 : 스캔 시작
         *                  2-2-2. 허용창에서 허용하지 않은 경우 : 설정창 이동 창 띄움
         *                         2-2-2-1. 설정창에서 허용하지 않은 경우 : finish
         *                         2-2-2-2. 설정창에서 허용한 경우 : 스캔 시작
         */

        final BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getContext(), "이 기기는 블루투스를 지원하지 않습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            TedPermission.with(getContext())
                    .setPermissionListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            Log.d(TAG, "onPermissionGranted: 권한 설정 완료");
                            scanLeDevice(true);
                        }

                        @Override
                        public void onPermissionDenied(ArrayList<String> deniedPermissions) {

                        }
                    }) // 퍼미션에 대한 콜백 작업 (아래)
                    .setRationaleMessage("블루투스 기기를 검색하기 위해 [위치]권한을 허용하셔야 합니다")
                    .setDeniedMessage("설정에서 IOBED 앱의 [위치]권한을 허용하신 후 블루투스 검색을 할 수 있습니다")
                    .setGotoSettingButton(true)
                    .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .check();
        }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scanner.stopScan(scanCallback);
                        mScanning = false;
                        cancelButton.setText("스캔시작");
                    }
                }, SCAN_PERIOD);

                mScanning = true;
                scanner = mBluetoothAdapter.getBluetoothLeScanner();
                List<ScanFilter> scanFilters = new ArrayList<>();
                ScanFilter scanFilter = new ScanFilter.Builder()
//                        .setServiceUuid(ParcelUuid.fromString(IOBEDApplication.IOBED_SERIVCE.toString()))
                        .build();

                scanFilters.add(scanFilter);
                ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
                scanner.startScan(scanFilters, scanSettings, scanCallback);
                cancelButton.setText("취소");

            } else {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(scanLeCallBack);
                        cancelButton.setText("스캔시작");
                    }
                }, SCAN_PERIOD);

                mScanning = true;
                mBluetoothAdapter.startLeScan(scanLeCallBack);
                cancelButton.setText("취소");
            }
        }
    }

    private void addDevice(BluetoothDevice device) {

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
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(BluetoothDevice item);
    }
}
