package com.example.a1.blemodule;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.angmarch.views.NiceSpinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.MaybeSubject;

public class MainActivity extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener {

    private static final String TAG = "TEST";
    private static final int REQUEST_ENABLE_BT = 999;
    private static final long SCAN_PERIOD = 5;
    public ItemFragment itemFragment;
    TextView tv1, tv2;
    EditText edit;
    Button btn_scan, btn_write, btn_start, btn_stop;
    NiceSpinner locale;
    NiceSpinner spin_firm;

    BluetoothModule bluetoothModule = BluetoothModule.getInstance();
    RadioGroup radio;

    FrameLayout layout;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner scanner;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissionBluetooth();

        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);
        edit = findViewById(R.id.edit);
        btn_scan = findViewById(R.id.btn_scan);
        btn_write = findViewById(R.id.btn_write);
        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
        radio = findViewById(R.id.radio);
        locale = findViewById(R.id.locale);
        layout = findViewById(R.id.frame);
        spin_firm = findViewById(R.id.spin_firm);

        ArrayList<String> list = new ArrayList<>();
        list.add("미국");
        list.add("한국");
        list.add("일본");
        list.add("중국");
        locale.attachDataSource(list);
        locale.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (locale.getSelectedItem().toString().equals("미국")) {
                    Locale locale = new Locale("en");
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                    recreate();

                } else if (locale.getSelectedItem().toString().equals("한국")) {
                    Locale locale = new Locale("ko");
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                    recreate();

                } else if (locale.getSelectedItem().toString().equals("일본")) {
                    Locale locale = new Locale("ja");
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                    recreate();

                } else if (locale.getSelectedItem().toString().equals("중국")) {
                    Locale locale = new Locale("zh");
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                    recreate();
                } else {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayList<String> firmList = new ArrayList<String>(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9"));
        spin_firm.attachDataSource(firmList);

        spin_firm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String set = spin_firm.getSelectedItem().toString();

                if (radio.getCheckedRadioButtonId() == R.id.left) {
                    bluetoothModule.sendProtocol("ctl" + set, new BluetoothModule.BluetoothWriteImpl() {
                        @Override
                        public void onSuccessWrite(int status, String data) {

                            Observable.just("ctl0")
                                    .delay(3, TimeUnit.SECONDS)
                                    .switchMapMaybe(it -> get(it))
                                    .subscribeOn(Schedulers.trampoline())
                                    .repeatWhen(o -> o.delay(1, TimeUnit.SECONDS))
                                    .takeUntil(it -> {
                                        if (it.equals("CRLM")) return true;
                                        else return false;
                                    }).subscribeWith(new Observer<String>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(String s) {
                                    System.out.println(s);
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {
                                    System.out.println("왼쪽 정상적으로 종료");
                                }
                            });
                        }

                        @Override
                        public void onFailed(Exception e) {

                        }
                    });

                } else {
                    bluetoothModule.sendProtocol("ctr" + set, new BluetoothModule.BluetoothWriteImpl() {
                        @Override
                        public void onSuccessWrite(int status, String data) {

                            Observable.just("ctr0")
                                    .delay(3, TimeUnit.SECONDS)
                                    .switchMapMaybe(it -> get(it))
                                    .subscribeOn(Schedulers.trampoline())
                                    .repeatWhen(o -> o.delay(1, TimeUnit.SECONDS))
                                    .takeUntil(it -> {
                                        if (it.equals("CRRM")) return true;
                                        else return false;
                                    }).subscribeWith(new Observer<String>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(String s) {
                                    System.out.println(s);
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {
                                    System.out.println("오른쪽 정상적으로 종료");
                                }
                            });
                        }

                        @Override
                        public void onFailed(Exception e) {

                        }
                    });

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_scan.setOnClickListener(v -> {

            // FIXME: 2019-06-19 아이템 프레그먼트 구조로 변경
            itemFragment = ItemFragment.newInstance();

            scanLeDevice();
            itemFragment.show(getSupportFragmentManager(), "ScanFragment");

        });

        btn_write.setOnClickListener(v -> {

            if (TextUtils.isEmpty(edit.getText()) || !bluetoothModule.isConnected()) {
                return;
            }

            bluetoothModule.sendProtocol(edit.getText().toString(), new BluetoothModule.BluetoothWriteImpl() {
                @Override
                public void onSuccessWrite(int status, String data) throws IOException {

                    tv2.setText(tv2.getText().toString() + "\n" + data);
                    Log.i(TAG, "onSuccessWrite: " + data);

                }

                @Override
                public void onFailed(Exception e) {

                }
            });
        });

        //스레드 시작
        btn_start.setOnClickListener(v -> {

            //특정 데이터를 반환할때까지 반복 ( Speak like a human. )

            String protocol = "";
            String param = "";
            if (radio.getCheckedRadioButtonId() == R.id.left) {

                bluetoothModule.sendProtocol("rxlg0", new BluetoothModule.BluetoothWriteImpl() {
                    @Override
                    public void onSuccessWrite(int status, String data) throws IOException {
                        if (!data.contains("RRLCM")) {
                            System.out.println("지금 바쁜 상태입니다");
                        }

                        String protocol = "rxlst";
                        Observable.just(protocol)
                                .delay(5, TimeUnit.SECONDS)
                                .switchMapMaybe(it -> get(it))
                                .subscribeOn(Schedulers.trampoline())
                                .repeatWhen(o -> o.delay(2, TimeUnit.SECONDS))
                                .takeUntil(it -> {
                                    if (it.equals("RRLCM")) return true;
                                    else return false;
                                }).subscribeWith(new Observer<String>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(String s) {
                                System.out.println(s);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {
                                System.out.println("왼쪽 정상적으로 종료되었습니다");
                            }
                        });
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });


            } else {

                bluetoothModule.sendProtocol("rxrg0", new BluetoothModule.BluetoothWriteImpl() {
                    @Override
                    public void onSuccessWrite(int status, String data) throws IOException {
                        if (!data.contains("RRRCM")) {
                            System.out.println("오른쪽 바쁜상태입니다");
                            return;
                        }

                        String protocol = "rxrst";
                        Observable.just(protocol)
                                .delay(5, TimeUnit.SECONDS)
                                .subscribeOn(Schedulers.trampoline())
                                .switchMapMaybe(it -> get(it))
                                .repeatWhen(o -> o.delay(2, TimeUnit.SECONDS))
                                .takeUntil(it -> {
                                    if (it.equals("RRRCM")) return true;
                                    else return false;
                                }).subscribeWith(new Observer<String>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(String s) {
                                System.out.println(s);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {
                                System.out.println("오른쪽 정상적으로 종료되었습니다");
                            }
                        });
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });

            }

        });

        btn_stop.setOnClickListener(v -> {

        });

    }

    public Maybe<String> get(String protocol) {

        MaybeSubject<String> maybeSubject = MaybeSubject.create();
        bluetoothModule.sendProtocol(protocol, new BluetoothModule.BluetoothWriteImpl() {
            @Override
            public void onSuccessWrite(int status, String data) {
                maybeSubject.onSuccess(data);
                Log.d(TAG, "onSuccessWrite: " + data);
            }

            @Override
            public void onFailed(Exception e) {
                maybeSubject.onError(e);
                maybeSubject.onComplete();
            }
        });

        return maybeSubject;

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
                        }

                        @Override
                        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                            Toast.makeText(MainActivity.this, "[위치]권한을 허용하신 후 다시 이용해 주세요", Toast.LENGTH_SHORT).show();
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scanLeDevice() {

        scanner = mBluetoothAdapter.getBluetoothLeScanner();

        Handler mHandler = new Handler();
        mHandler.postDelayed(() -> {
            scanner.stopScan(scanCallback);
        }, SCAN_PERIOD);

//        List<ScanFilter> scanFilters = new ArrayList<>();
//        ScanFilter scanFilter = new ScanFilter.Builder()
//                        .setServiceUuid(ParcelUuid.fromString(IOBEDApplication.IOBED_SERIVCE.toString()))
//                .build();

//        scanFilters.add(scanFilter);
//        ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
        scanner.startScan(scanCallback);

    }

    public ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);

            itemFragment.addDevice(new BTModel(result.getDevice().getName(), result.getDevice().getAddress(), result.getRssi()));

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {

            Log.d(TAG, "onActivityResult: " + data.getStringExtra(BluetoothDevice.EXTRA_DEVICE));
            String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);

            bluetoothModule.gattConnect(deviceAddress, new BluetoothModule.BluetoothConnectImpl() {
                @Override
                public void onSuccessConnect(BluetoothDevice device) {
                    tv1.setText("연결됐습니다 " + device.getName() + " / " + device.getAddress());
                }

                @Override
                public void onFailed() {
                    tv1.setText("연결 실패 다시 연결중입니다.");
                }
            }, MainActivity.this);
        }
    }

    @Override
    public void onListFragmentInteraction(BTModel item) {

        bluetoothModule.disconnect();
        itemFragment.dismiss();

        bluetoothModule.gattConnect(item.getMac(), new BluetoothModule.BluetoothConnectImpl() {
            @Override
            public void onSuccessConnect(BluetoothDevice device) {
                // FIXME: 2019-06-19 A : I4Q B:I3Q C:I3SS
                String[] name = device.getName().split("-");
                if (name[1].equals("4")) {
                    tv1.setText("i4-QN에 연결됐습니다    " + device.getName() + " / " + device.getAddress());
                } else if (name[1].equals("3")) {
                    tv1.setText("i3-QN에 연결됐습니다    " + device.getName() + " / " + device.getAddress());
                } else {
                    tv1.setText("i3-SS에 연결됐습니다    " + device.getName() + " / " + device.getAddress());
                }
            }

            @Override
            public void onFailed() {
                tv1.setText("연결 실패 다시 연결중입니다.");
                Toast.makeText(MainActivity.this, "다시 연결 중입니다...", Toast.LENGTH_SHORT).show();

            }
        }, MainActivity.this);
    }

    @Override
    public void onReScan() {
        scanLeDevice();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("Destroyed");


    }
}

