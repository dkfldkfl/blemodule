package com.example.a1.blemodule;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "TEST";
    TextView tv1, tv2;
    EditText edit;
    Button btn_scan, btn_write;
    BluetoothModule bluetoothModule = BluetoothModule.getInstance();
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

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeviceListBTActivity.class);
                startActivityForResult(intent, DEVCIE_LIST_BT_ACTIVITY);
            }
        });

        btn_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv2.setText("");
                String input = edit.getText().toString();
                if (input.isEmpty()) {
                    return;
                }

                bluetoothModule.sendProtocol(input, new BluetoothModule.BluetoothWriteImpl() {
                    @Override
                    public void onSuccessWrite(int status, String data) throws IOException {
                        tv2.setText("받은 값 " + data);
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

            bluetoothModule.gattConnect(deviceAddress, new BluetoothModule.BluetoothConnectImpl() {
                @Override
                public void onSuccessConnect(BluetoothDevice device) {
                    tv1.setText("연결됐습니다" + device.getName() + " / " + device.getAddress());
                }

                @Override
                public void onFailed() {
                    tv1.setText("연결 실패 다시 연결중입니다.");
                }
            });
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}

