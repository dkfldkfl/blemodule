package com.example.a1.blemodule;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.angmarch.views.NiceSpinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener {

    private static final String TAG = "TEST";
    private static final int REQUEST_ENABLE_BT = 999;
    public ItemFragment itemFragment;
    TextView tv1, tv2;
    EditText edit;
    Button btn_scan, btn_write, btn_start, btn_stop;
    NiceSpinner spin_firm;
    NiceSpinner locale;

    BluetoothModule bluetoothModule = BluetoothModule.getInstance();
    RadioGroup radio;

    public BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);
        edit = findViewById(R.id.edit);
        btn_scan = findViewById(R.id.btn_scan);
        btn_write = findViewById(R.id.btn_write);
        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
        radio = findViewById(R.id.radio);
        spin_firm = findViewById(R.id.spin_firm);
        locale = findViewById(R.id.locale);

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
                    System.out.println("미국");
                    Locale locale = new Locale("en");
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                    recreate();

                } else if (locale.getSelectedItem().toString().equals("한국")) {
                    System.out.println("한국");
                    Locale locale = new Locale("ko");
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                    recreate();

                } else if (locale.getSelectedItem().toString().equals("일본")) {
                    System.out.println("일본");
                    Locale locale = new Locale("ja");
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                    recreate();

                } else if (locale.getSelectedItem().toString().equals("중국")) {
                    System.out.println("중국");
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

        /**
         * 1. SCAN -> i3,i4 구분 ABC
         * 2. RX Polling
         * 3. 비디오 플레이어 테스트
         * 4. 레이아웃작성
         * 5. 다국어 설정 Language ->
         */

        btn_scan.setOnClickListener(v -> {
            checkPermissionBluetooth();
        });

        //스레드 시작
        btn_start.setOnClickListener(v -> {
            //Fragment 화면 띄워 주기

            if (radio.getCheckedRadioButtonId() == R.id.left) {

                bluetoothModule.sendProtocol("rxlg0", new BluetoothModule.BluetoothWriteImpl() {
                    @Override
                    public void onSuccessWrite(int status, String data) throws IOException {

                        if (!data.contains("RRLCM")) {
                            System.out.println("지금 바쁜 상태입니다");
                        }

                        bluetoothModule.sendProtocol("rxlst", new BluetoothModule.BluetoothWriteImpl() {
                            @Override
                            public void onSuccessWrite(int status, String data) throws IOException {
                                Observable.just("rxlg0")
                                        .delay(5, TimeUnit.SECONDS)
                                        .switchMapSingle(it -> get(it))
                                        .subscribeOn(Schedulers.newThread())
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

                        bluetoothModule.sendProtocol("rxrst", new BluetoothModule.BluetoothWriteImpl() {
                            @Override
                            public void onSuccessWrite(int status, String data) throws IOException {
                                Observable.just("rxrg0")
                                        .delay(5, TimeUnit.SECONDS)
                                        .subscribeOn(Schedulers.newThread())
                                        .switchMapSingle(it -> get(it))
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

                    @Override
                    public void onFailed(Exception e) {

                    }
                });
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
                                    .switchMapSingle(it -> get(it))
                                    .subscribeOn(Schedulers.newThread())
                                    .repeatWhen(o -> o.delay(1, TimeUnit.SECONDS))
                                    .takeUntil(it -> {
                                        if (it.equals("CPL" + set + "0")) return true;
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
                                    .switchMapSingle(it -> get(it))
                                    .subscribeOn(Schedulers.newThread())
                                    .repeatWhen(o -> o.delay(1, TimeUnit.SECONDS))
                                    .takeUntil(it -> {
                                        if (it.equals("CPR" + set + "0")) return true;
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


        btn_stop.setOnClickListener(v -> {

            Toast.makeText(this, "하드웨어 미개발", Toast.LENGTH_SHORT).show();
        });

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
            return;
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
                            itemFragment = ItemFragment.newInstance();
                            itemFragment.show(getSupportFragmentManager(), "ScanFragment");

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            checkPermissionBluetooth();
        }
    }

    public Single<String> get(String protocol) {

        return Single.create(emitter -> {
            bluetoothModule.sendProtocol(protocol, new BluetoothModule.BluetoothWriteImpl() {
                @Override
                public void onSuccessWrite(int status, String data) throws IOException {
                    emitter.onSuccess(data);
                }

                @Override
                public void onFailed(Exception e) {
                    emitter.onError(e);
                }

            });

        });
    }
/*
    public Maybe<String> get(String protocol) {

        MaybeSubject<String> maybeSubject = MaybeSubject.create();
        bluetoothModule.sendProtocol(protocol, new BluetoothModule.BluetoothWriteImpl() {
            @Override
            public void onSuccessWrite(int status, String data) {
                Log.d(TAG, "onSuccessWrite: " + data);
                maybeSubject.onSuccess(data);
            }

            @Override
            public void onFailed(Exception e) {
                maybeSubject.onError(e);
                maybeSubject.onComplete();
            }
        });

        return maybeSubject;

    }*/

    //BT 선택시
    @Override
    public void onListFragmentInteraction(BTModel item) {

        bluetoothModule.disconnect();
        itemFragment.dismiss();
        bluetoothModule.gattConnect(item.getMac(), new BluetoothModule.BluetoothConnectImpl() {
            @Override
            public void onSuccessConnect(BluetoothDevice device) {

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
            }
        }, MainActivity.this);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("Destroyed");
        bluetoothModule.disconnect();
    }
}

