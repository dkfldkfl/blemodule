package com.example.a1.blemodule;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.io.IOException;

public class Test extends AppCompatActivity {

    RelaxLeftThread leftThread;
    RelaxRightThread rightThread;
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
                if (leftThread == null) {
                    leftThread = new RelaxLeftThread("L");
                    leftThread.start();
                } else {
                    System.out.println("L 스레드 동작중이다");
                }

            } else {
                if (rightThread == null) {
                    rightThread = new RelaxRightThread("R");
                    rightThread.start();
                } else {
                    System.out.println("R 스레드 동작중이다");
                }
            }

        });

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
}
