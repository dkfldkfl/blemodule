package com.example.a1.blemodule;

import android.os.Parcel;
import android.os.Parcelable;

public class BTModel implements Parcelable {
    public static final Creator<BTModel> CREATOR = new Creator<BTModel>() {
        @Override
        public BTModel createFromParcel(Parcel in) {
            return new BTModel(in);
        }

        @Override
        public BTModel[] newArray(int size) {
            return new BTModel[size];
        }
    };
    private String name;
    private String mac;
    private String rssi;

    public BTModel(String name, String mac, String rssi) {
        this.name = name;
        this.mac = mac;
        this.rssi = rssi;
    }

    protected BTModel(Parcel in) {
        name = in.readString();
        mac = in.readString();
        rssi = in.readString();
    }

    @Override
    public String toString() {
        return "BTModel{" +
                "name='" + name + '\'' +
                ", mac='" + mac + '\'' +
                ", rssi='" + rssi + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(mac);
        dest.writeString(rssi);
    }
}
