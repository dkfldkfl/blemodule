package com.example.a1.blemodule;


import android.app.Application;

import java.util.UUID;

/**
 * 공용 자원
 */
public class IOBEDApplication extends Application {

    public static final UUID IOBED_CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

//    public static final UUID IOBED_SERIVCE = UUID.fromString("0000ff00-0000-1000-8000-00805f9b34fb");
//    public static final UUID IOBED_CHARACTERISTIC_READ_WRITE = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
//    public static final UUID IOBED_CHARACTERISTIC_NOTY = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");

    //WRITE 용
    public static final UUID IOBED_SERIVCE = UUID.fromString("1d14d6ee-fd63-4fa1-bfa4-8f47b42119f0");
    public static final UUID IOBED_CHARACTERISTIC_READ_WRITE = UUID.fromString("f7bf3564-fb6d-4e53-88a4-5e37e0326063");

    //UUID 용
    public static final UUID IOBED_NOTI_SERIVCE = UUID.fromString("4880c12c-fdcb-4077-8920-a450d7f9b907");
    public static final UUID IOBED_CHARACTERISTIC_NOTY = UUID.fromString("fec26ec4-6d71-4442-9f81-55bc21d658d6");

    public static UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static UUID DIS_HWREV_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");

    public static IOBEDApplication instance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
