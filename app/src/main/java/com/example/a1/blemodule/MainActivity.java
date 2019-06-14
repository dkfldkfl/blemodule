package com.example.a1.blemodule;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener {

    private static final String TAG = "TEST";
    public ItemFragment itemFragment;
    TextView tv1, tv2;
    EditText edit;
    Button btn_scan, btn_write, btn_start, btn_stop, btn_firm;
    NiceSpinner locale;

    BluetoothModule bluetoothModule = BluetoothModule.getInstance();
    RadioGroup radio;

    RelaxLeftThread leftThread;
    RelaxRightThread rightThread;

    FrameLayout layout;
    private int DEVCIE_LIST_BT_ACTIVITY = 99;

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
                    System.out.println("뭘선택한거야?");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //스레드 시작
        btn_start.setOnClickListener(v -> {

            String _position = "l";
            if (radio.getCheckedRadioButtonId() == R.id.right) {
                _position = "r";
            }

            final String position = _position;
            String input = "rx" + position + "g0";

            // FIXME: 2019-06-14 콜백 지옥 + 스레드 문제
            bluetoothModule.sendProtocol(input, new BluetoothModule.BluetoothWriteImpl() {
                @Override
                public void onSuccessWrite(int status, String data) {
                    if (data.contains("RR" + position.toUpperCase() + "CM")) { // 시작할 수 있는 상황
                        bluetoothModule.sendProtocol("rx" + position + "st", new BluetoothModule.BluetoothWriteImpl() {
                            @Override
                            public void onSuccessWrite(int status, String data) {
                                if (position.equals("l")) {

                                    if (leftThread == null) {
                                        leftThread = new RelaxLeftThread("l");
                                        leftThread.start();
                                    } else if (leftThread.isAlive()) {
                                        System.out.println("왼쪽 체크중입니다");
                                    } else {
                                        leftThread = new RelaxLeftThread("l");
                                        leftThread.start();
                                    }

                                } else {

                                    if (rightThread == null) {
                                        rightThread = new RelaxRightThread("r");
                                        rightThread.start();
                                    } else if (rightThread.isAlive()) {
                                        System.out.println("오른쪽 동작중입니다");
                                    } else {
                                        rightThread = new RelaxRightThread("r");
                                        rightThread.start();
                                    }
                                }
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Log.e(TAG, "onFailed: " + e.getMessage());
                            }
                        });

                    } else {
                        Toast.makeText(MainActivity.this, "다른 동작 중입니다", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailed(Exception e) {
                    Log.e(TAG, "onFailed: " + e.getMessage());
                }
            });

        });

        btn_stop.setOnClickListener(v -> {
            if (radio.getCheckedRadioButtonId() == R.id.right) {
                System.out.println("오른쪽 스레드 강제종료시도중");
                if (rightThread != null) {
                    System.out.println("오른쪽 스레드 강제종료");
                    rightThread.interrupt();
                    rightThread.setStarted(false);
                    rightThread = null;
                }
            } else {
                System.out.println("왼쪽 스레드 강제종료시도중");
                if (leftThread != null) {
                    System.out.println("왼쪽 스레드 강제종료");
                    leftThread.interrupt();
                    leftThread.setStarted(false);
                    leftThread = null;
                }
            }
        });

        btn_scan.setOnClickListener(v -> {
            itemFragment = ItemFragment.newInstance();
            itemFragment.show(getSupportFragmentManager(), "ItemFragment");

        });

        btn_write.setOnClickListener(v -> {

            String input = edit.getText().toString();
            if (TextUtils.isEmpty(input)) {
                return;
            }
            if (!bluetoothModule.isConnected()) {
                Toast.makeText(MainActivity.this, "블루투스 연결을 먼저해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            tv2.setText("");
            bluetoothModule.sendProtocol(input, new BluetoothModule.BluetoothWriteImpl() {
                @Override
                public void onSuccessWrite(int status, String data) {

                    tv2.setText(tv2.getText() + "\n" + data);
                }

                @Override
                public void onFailed(Exception e) {

                }
            });
        });
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

    public class RelaxLeftThread extends Thread {
        Boolean started = true;
        String position;

        public RelaxLeftThread(@NonNull String name) {
            super(name);
            position = name;

        }

        public Boolean getStarted() {
            return started;
        }

        public void setStarted(Boolean started) {
            this.started = started;
        }

        public void run() {
            while (started) {
                bluetoothModule.sendProtocol("rxlg0", new BluetoothModule.BluetoothWriteImpl() {
                    @Override
                    public void onSuccessWrite(int status, String data) {
                        if (data.contains("RRLCM")) {
                            System.out.println(position + "릴랙스 종료합니다");
                            started = false;
                            interrupt();
                            leftThread = null;
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class RelaxRightThread extends Thread {
        Boolean started = true;
        String position;

        public RelaxRightThread(@NonNull String name) {
            super(name);
            position = name;

        }

        public Boolean getStarted() {
            return started;
        }

        public void setStarted(Boolean started) {
            this.started = started;
        }

        public void run() {
            while (started) {

                bluetoothModule.sendProtocol("rxrg0", new BluetoothModule.BluetoothWriteImpl() {
                    @Override
                    public void onSuccessWrite(int status, String data) {
                        if (data.contains("RRRCM")) {
                            System.out.println(position + "릴랙스 종료합니다");
                            started = false;
                            interrupt();
                            rightThread = null;
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

