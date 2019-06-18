package com.example.a1.blemodule;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.angmarch.views.NiceSpinner;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
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
    public ItemFragment itemFragment;
    TextView tv1, tv2;
    EditText edit;
    Button btn_scan, btn_write, btn_start, btn_stop, btn_firm;
    NiceSpinner locale;

    BluetoothModule bluetoothModule = BluetoothModule.getInstance();
    RadioGroup radio;

    FrameLayout layout;
    private int DEVCIE_LIST_BT_ACTIVITY = 99;

    @RequiresApi(api = Build.VERSION_CODES.O)
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
        btn_firm = findViewById(R.id.btn_firm);
        locale = findViewById(R.id.locale);
        layout = findViewById(R.id.frame);

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
                                .switchMapMaybe(it -> get(it))
                                .subscribeOn(Schedulers.single())
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
                                .subscribeOn(Schedulers.single())
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

            Observable<String> tuple2Observable = Observable.just(1, 2, 3, 4, 5, 6)
                    .flatMap(integer ->
                            Observable.fromCallable(() -> getVitalName(integer))
                                    .subscribeOn(Schedulers.single())
                                    .doOnNext(s -> System.out.println("Value:: " + Thread.currentThread().getName() + "-" + Instant.now()))
                                    .map(s -> s)
                    ).doOnComplete(() -> System.out.println("Finished:: " + Thread.currentThread().getName() + "-" + Instant.now()));

            try {
                tuple2Observable.test()
                        .await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getVitalName(int vitalId) throws Exception {
        System.out.println("getVitalName method called with vitalId = " + vitalId + "-" + Thread.currentThread().getName() + "-" + Instant.now());

        Thread.sleep(500);

        String name = "le fake value";
        return name;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DEVCIE_LIST_BT_ACTIVITY && resultCode == RESULT_OK) {

            Log.d(TAG, "onActivityResult: " + data.getStringExtra(BluetoothDevice.EXTRA_DEVICE));
            String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);

            bluetoothModule.gattConnect(deviceAddress, new BluetoothModule.BluetoothConnectImpl() {
                @Override
                public void onSuccessConnect(BluetoothDevice device) {
                    tv1.setText("");
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
    public void onListFragmentInteraction(BluetoothDevice item) {
        bluetoothModule.disconnect();
        itemFragment.dismiss();
        bluetoothModule.gattConnect(item.getAddress(), new BluetoothModule.BluetoothConnectImpl() {
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


    }


    public Maybe<String> get(String protocol) {

        MaybeSubject<String> maybeSubject = MaybeSubject.create();
        bluetoothModule.sendProtocol(protocol, new BluetoothModule.BluetoothWriteImpl() {
            @Override
            public void onSuccessWrite(int status, String data) {
                maybeSubject.onSuccess(data);
            }

            @Override
            public void onFailed(Exception e) {
                maybeSubject.onError(e);
                maybeSubject.onComplete();
            }
        });

        return maybeSubject;

    }

}

