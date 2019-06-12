package com.example.a1.blemodule;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener {

    private static final String TAG = "TEST";
    public ItemFragment itemFragment;
    TextView tv1, tv2;
    EditText edit;
    Button btn_scan, btn_write, btn_start, btn_stop;
    BluetoothModule bluetoothModule = BluetoothModule.getInstance();
    Thread thread;
    Runnable runnable;
    Handler handler = new Handler();
    boolean started = true;
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

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int Num = 1;
                        while (started) {

                            bluetoothModule.sendProtocol("rxlg0", new BluetoothModule.BluetoothWriteImpl() {
                                @Override
                                public void onSuccessWrite(int status, String data) {
                                    tv2.setText(tv2.getText() + "\n" + data);
                                }

                                @Override
                                public void onFailed(Exception e) {

                                }
                            });

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                started=true;
                th.start();

//                runnable = new Runnable() {
//                    @Override
//                    public void run() {
//
//                        while (started) {
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    bluetoothModule.sendProtocol("rxlg0", new BluetoothModule.BluetoothWriteImpl() {
//                                        @Override
//                                        public void onSuccessWrite(int status, String data) throws IOException {
//                                            tv2.setText(tv2.getText() + "\n" + data);
//                                        }
//
//                                        @Override
//                                        public void onFailed(Exception e) {
//
//                                        }
//                                    });
//                                }
//                            });
//
//                            try {
//                                Thread.sleep(3000); //10초
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                };


                thread = new Thread(runnable);
                thread.start();
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                started = false;
//                thread.interrupt();
                thread.interrupt();
            }
        });


        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemFragment = ItemFragment.newInstance();
                itemFragment.show(getSupportFragmentManager(), "ItemFragment");

            }
        });

        btn_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String input = edit.getText().toString();
                if (TextUtils.isEmpty(input)) {
                    return;
                }

                tv2.setText("");

                bluetoothModule.sendProtocol(input, new BluetoothModule.BluetoothWriteImpl() {
                    @Override
                    public void onSuccessWrite(int status, String data) throws IOException {
                        tv2.setText(tv2.getText() + "\n" + data);
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DEVCIE_LIST_BT_ACTIVITY && resultCode == RESULT_OK) {

            Log.d(TAG, "onActivityResult: " + data.getStringExtra(BluetoothDevice.EXTRA_DEVICE));
            String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
            bluetoothModule.disconnect();

            bluetoothModule.gattConnect(deviceAddress, new BluetoothModule.BluetoothConnectImpl() {
                @Override
                public void onSuccessConnect(BluetoothDevice device) {
                    tv1.setText("연결됐습니다" + device.getName() + " / " + device.getAddress());
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

        itemFragment.dismiss();
        bluetoothModule.disconnect();
        bluetoothModule.gattConnect(item.getAddress(), new BluetoothModule.BluetoothConnectImpl() {
            @Override
            public void onSuccessConnect(BluetoothDevice device) {
                tv1.setText("연결됐습니다" + device.getName() + " / " + device.getAddress());
            }

            @Override
            public void onFailed() {
                tv1.setText("연결 실패 다시 연결중입니다.");
            }
        }, MainActivity.this);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

