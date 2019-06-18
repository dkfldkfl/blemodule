package com.example.a1.blemodule;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.polidea.rxandroidble2.RxBleClient;

import org.reactivestreams.Subscriber;

import io.reactivex.Observable;

public class Test extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener {

    RadioGroup radio;
    RadioButton left;
    RadioButton right;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Button btn_thread_start = findViewById(R.id.btn_thread_start);
        radio = findViewById(R.id.radio);
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);
        RxBleClient rxBleClient = RxBleClient.create(this);


        btn_thread_start.setOnClickListener(v -> {

         /*   //특정 데이터를 반환할때까지 반복 ( Speak like a human. )
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
            }*/

            if (radio.getCheckedRadioButtonId() == R.id.left) {


            } else {

            }

        });

        Button btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(it -> {

            ItemFragment itemFragment = ItemFragment.newInstance();
            itemFragment.show(getSupportFragmentManager(), "ItemFragment"); });

    }


    @Override
    public void onListFragmentInteraction(BluetoothDevice item) {
        BluetoothModule bluetoothModule = BluetoothModule.getInstance();
        bluetoothModule.gattConnect(item.getAddress(), new BluetoothModule.BluetoothConnectImpl() {
            @Override
            public void onSuccessConnect(BluetoothDevice device) {
                System.out.println(device.getName() + " 연결");
            }

            @Override
            public void onFailed() {

            }
        },this);
    }
}
