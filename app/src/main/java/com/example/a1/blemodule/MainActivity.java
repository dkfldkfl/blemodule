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

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.angmarch.views.NiceSpinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //스레드 시작
        btn_start.setOnClickListener(v -> {

            //특정 데이터를 반환할때까지 반복 ( Speak like a human. )
            Observable.timer(1, TimeUnit.SECONDS)
                    .map(it -> "https://api.github.com/zen")
                    .map(OkHttpHelper::get)
//                    .map(leftOb::get)
                    .repeat()
//                    .takeUntil()
                    .subscribe(res -> System.out.println(res));

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            if (radio.getCheckedRadioButtonId() == R.id.left) {
//                if (leftThread == null) {
//                    leftThread = new RelaxLeftThread("L");
//                    leftThread.start();
//                } else {
//                    System.out.println("L 스레드 동작중이다");
//                }
//
//            } else {
//                if (rightThread == null) {
//                    rightThread = new RelaxRightThread("R");
//                    rightThread.start();
//                } else {
//                    System.out.println("R 스레드 동작중이다");
//                }
//            }

        });

        btn_stop.setOnClickListener(v -> {

            if (radio.getCheckedRadioButtonId() == R.id.left) {

                leftThread.setStarted(false);
                leftThread.interrupt();
                leftThread = null;

            } else {

                rightThread.setStarted(false);
                rightThread.interrupt();
                rightThread = null;
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

    public static class leftOb {
        public static int cnt = 0;

        public static String get() {
            if (cnt++ > 9) {
                return "LEFT 카운트 종료합니다";
            }
            return "left 카운트 중 :" + cnt;

        }
    }

    public static class rightOb {
        public static int cnt = 0;

        public static String get() {
            if (cnt++ > 9) {
                return "Right 카운트 종료합니다";

            }
            return "right 카운트 중 :" + cnt;


        }

    }

    public class RelaxLeftThread extends Thread {
        Boolean started = true;
        String position;
        int cnt = 0;

        public RelaxLeftThread(@NonNull String name) {
            super(name);
            position = name;

        }

        public void setStarted(Boolean started) {
            this.started = started;
        }

        public void run() {
            while (started) {

                System.out.println(position + " 카운트 중 :" + cnt);
                if (cnt++ > 9) {
                    System.out.println(position + " 카운트 종료합니다");
                    started = false;
                    interrupt();
                    leftThread = null;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class RelaxRightThread extends Thread {
        Boolean started = true;
        String position;
        int cnt = 0;

        public RelaxRightThread(@NonNull String name) {
            super(name);
            position = name;

        }

        public void setStarted(Boolean started) {
            this.started = started;
        }

        public void run() {
            while (started) {

                System.out.println(position + " 카운트 중 : " + cnt);
                if (cnt++ > 9) {
                    System.out.println(position + "릴랙스 종료합니다");
                    started = false;
                    interrupt();
                    rightThread = null;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public static class OkHttpHelper {
        private static OkHttpClient client = new OkHttpClient();

        public static String get(String url) throws IOException {

            Request request = new Request.Builder().url(url).build();
            try {

                Response res = client.newCall(request).execute();
                return res.body().string();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw e;
            }
        }

    }
}

