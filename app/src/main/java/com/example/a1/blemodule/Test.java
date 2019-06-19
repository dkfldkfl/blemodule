package com.example.a1.blemodule;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.MaybeSubject;


public class Test extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener {

    RadioGroup radio;
    RadioButton left;
    RadioButton right;
    BluetoothModule bluetoothModule = BluetoothModule.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Button btn_thread_start = findViewById(R.id.btn_thread_start);
        radio = findViewById(R.id.radio);
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);

        btn_thread_start.setOnClickListener(v -> {

            bluetoothModule.sendProtocol("rxlg0", new BluetoothModule.BluetoothWriteImpl() {
                @Override
                public void onSuccessWrite(int status, String data) throws IOException {
                    if (!data.contains("RRLCM")) {
                        System.out.println("지금 바쁜 상태입니다");
                    }

                    String protocol = "rxlst";
                    Observable.just(protocol)
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



        });

        Button btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(it -> {

            ItemFragment itemFragment = ItemFragment.newInstance();
            itemFragment.show(getSupportFragmentManager(), "ItemFragment"); });

    }


    @Override
    public void onListFragmentInteraction(BluetoothDevice item) {
        bluetoothModule.gattConnect(item.getAddress(), new BluetoothModule.BluetoothConnectImpl() {
            @Override
            public void onSuccessConnect(BluetoothDevice device) {
                System.out.println(device.getName() + " 연결");
                Toast.makeText(Test.this, "연결완료", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {

            }
        },this);
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
