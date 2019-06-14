package com.example.a1.blemodule;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

public class DeviceListBTActivity extends Activity {

    public static final String TAG = "DeviceListBTActivity";
    private static final int REQUEST_ENABLE_BT = 99;
    private static final long SCAN_PERIOD = 10000; //scanning for 10 seconds
    ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    ArrayList<Integer> rssiList = new ArrayList<>();
    ListView listView;
    private BluetoothAdapter mBluetoothAdapter;
    private TextView mEmptyList;
    private BTAdapter deviceAdapter;
    private Handler mHandler;
    private boolean mScanning;
    private BluetoothLeScanner scanner;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        checkPermissionBluetooth();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //등록됐을때 맥주소를 DB에 저장해놓고, 저장플래그도 설정하고
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

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "이 기기는 블루투스를 지원하지 않습니다", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            TedPermission.with(this)
                    .setPermissionListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            Log.d(TAG, "onPermissionGranted: 권한 설정 완료");
                            initView();
                        }

                        @Override
                        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                            Toast.makeText(DeviceListBTActivity.this, "[위치]권한을 허용하신 후 다시 이용해 주세요", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }) // 퍼미션에 대한 콜백 작업 (아래)
                    .setRationaleMessage("블루투스 기기를 검색하기 위해 [위치]권한을 허용하셔야 합니다")
                    .setDeniedMessage("설정에서 IOBED 앱의 [위치]권한을 허용하신 후 블루투스 검색을 할 수 있습니다")
                    .setGotoSettingButton(true)
                    .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .check();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                checkPermissionBluetooth();
            } else {
                Toast.makeText(DeviceListBTActivity.this, "블루투스 기능을 활성화한 후  다시 실행해주세요", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initView() {
        /* Initialize device list container */

        // TODO: 2019-06-11 화면 수정 필요 액티비티로 할필요없음
        setContentView(R.layout.activity_device_list_bt);
        android.view.WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
        layoutParams.gravity = Gravity.TOP;
        layoutParams.y = 200;
        mHandler = new Handler();

        deviceAdapter = new BTAdapter(DeviceListBTActivity.this, deviceList);
        listView = findViewById(R.id.new_devices);
        listView.setAdapter(deviceAdapter);
        listView.setOnItemClickListener(deviceClickListener);
        cancelButton = findViewById(R.id.btn_cancel);
        mEmptyList = (TextView) findViewById(R.id.empty);

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScanning == false) scanLeDevice(true);
                else finish();
            }
        });

        scanLeDevice(true);
    }

    private OnItemClickListener deviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent result = new Intent();
            result.putExtra(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());
            setResult(Activity.RESULT_OK, result);
            finish();
        }
    };

    public BluetoothAdapter.LeScanCallback scanLeCallBack = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {

                if (deviceList.contains(device)) //중복 검색 제외
                    return;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addDevice(device, rssi);
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addDevice(result.getDevice(), result.getRssi());
                }
            });
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

    private void addDevice(BluetoothDevice device, int rssi) {
        rssiList.add(rssi);
        deviceList.add(device);
        mEmptyList.setVisibility(View.GONE);
        deviceAdapter.notifyDataSetChanged();
    }

    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class BTAdapter extends BaseAdapter {
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;

        BTAdapter(Context context, List<BluetoothDevice> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.device_bt_element, null);
            }

            BluetoothDevice device = devices.get(position);
            final TextView tvadd = vg.findViewById(R.id.address);
            final TextView tvname = vg.findViewById(R.id.name);
            final TextView tvpaired = vg.findViewById(R.id.paired);
            final TextView tvrssi = vg.findViewById(R.id.rssi);

            tvrssi.setVisibility(View.VISIBLE);
            tvrssi.setText(rssiList.get(position).toString());

            tvname.setText(device.getName());
            tvadd.setText(device.getAddress());
            tvname.setTextColor(Color.BLACK);
            tvadd.setTextColor(Color.BLACK);
            tvpaired.setTextColor(Color.GRAY);
            tvrssi.setTextColor(Color.BLACK);
            tvrssi.setVisibility(View.VISIBLE);

            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.i(TAG, "device::" + device.getName());

                tvpaired.setText("paired");
                tvpaired.setVisibility(View.VISIBLE);

            } else {
                tvpaired.setVisibility(View.GONE);
            }
            return vg;
        }
    }

}

