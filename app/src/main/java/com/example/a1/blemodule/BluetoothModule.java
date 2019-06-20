package com.example.a1.blemodule;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;


public class BluetoothModule {
    //BluetoothGatt 객체로 Connect 해주고, writeCharacter 해주는 클래스
    public static final String TAG = "BluetoothModule";
    public BluetoothGattCharacteristic writeGattCharacteristic;
    private BluetoothGatt bluetoothGatt;
    private BluetoothConnectImpl btConnectCallback;
    private BluetoothWriteImpl btWriteCallback;
    private Context context;
    /**
     * 블루투스 콜백
     */
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        final Handler handler = new Handler(Looper.getMainLooper());

        /**
         * 연결상태가 변화 할때 마다 (연결, 끊김) 호출
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Log.d(TAG, "onConnectionStateChange: " + status + " " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                bluetoothGatt.discoverServices(); // onServicesDiscovered() 호출 (서비스 연결 위해 꼭 필요)
                handler.post(() -> Toast.makeText(context, "연결됐어요", Toast.LENGTH_SHORT).show());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                handler.post(() -> Toast.makeText(context, "연결이 끊어졌습니다\n다시 연결중입니다기다려주세요", Toast.LENGTH_SHORT).show());

            }
        }

        /**
         * 서비스 연결 후 ( Notification 설정 ) cf) setCharacteristicNotification 까지만 해도  Notification이 되지만 이 메소드의 콜백을 받지 못한다
         * (setCharacteristicNotification이 비동기로 완료되기 전에 통신을 한다면 에러가 난다) -> writeDescriptor가 완료 된 순간부터 통신이 가능하다
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                BluetoothGattCharacteristic ch = gatt.getService(IOBEDApplication.IOBED_NOTI_SERIVCE).getCharacteristic(IOBEDApplication.IOBED_CHARACTERISTIC_NOTY);
                gatt.setCharacteristicNotification(ch, true);

                BluetoothGattDescriptor descriptor = ch.getDescriptor(IOBEDApplication.IOBED_CCCD);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(descriptor);

            }
        }

        @Override
        public void onDescriptorWrite(final BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    btConnectCallback.onSuccessConnect(gatt.getDevice()); // 통신 준비 완료
                }
            });
        }


        //전송시 호출
        @Override
        public void onCharacteristicWrite(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicWrite: 보낸 값 : " + characteristic.getStringValue(0));
        }

        /**
         * 가장 중요한 메소드, ble 기기의 값을 받아온다
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String value = characteristic.getStringValue(1);
                    value = value.replaceAll(" ", "");
                    value = value.replaceAll(">", "");
                    if(value.equals(">")){
                        return;
                    }
                    try {
                        Log.d(TAG, "run: " + value);
                        btWriteCallback.onSuccessWrite(0, value);
                    } catch (IOException e) {
                        e.printStackTrace();
                        btWriteCallback.onFailed(e);
                    }
                }
            });
        }
    };

    public static BluetoothModule getInstance() {
        return BluetoothModuleHolder.instance;
    }

    public boolean isConnected() {
        return bluetoothGatt != null && bluetoothGatt.connect();
    }

    public BluetoothGatt getGatt() {
        return bluetoothGatt;
    }

    public void disconnect() {

        if (isConnected()) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }

    /**
     * 맥주소를 받아서 connect
     */
    public void gattConnect(String macAddress, BluetoothConnectImpl btConnectCallback, Context context) {

        this.btConnectCallback = btConnectCallback;
//        context = IOBEDApplication.instance;
        this.context = context;
        final BluetoothManager bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bm.getAdapter();

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
        bluetoothGatt = device.connectGatt(context, true, gattCallback);

    }

    /**
     * 프로토콜 보내기 write 를 하고 ble장치로부터 값을 받으면 onCharacteristicChanged() 메소드가 호출 된다
     */
    public void sendProtocol(String protocol, BluetoothWriteImpl btWriteCallback) {
        if (isConnected()) {

            BluetoothGattService service = bluetoothGatt.getService(IOBEDApplication.IOBED_NOTI_SERIVCE);
            writeGattCharacteristic = service.getCharacteristic(IOBEDApplication.IOBED_CHARACTERISTIC_NOTY);

            this.btWriteCallback = btWriteCallback;
            protocol = "<" + protocol.toUpperCase() + ">";
            writeGattCharacteristic.setValue(protocol);
            bluetoothGatt.writeCharacteristic(writeGattCharacteristic);
        }
    }

    public interface BluetoothConnectImpl {
        void onSuccessConnect(BluetoothDevice device);

        void onFailed();
    }

    public interface BluetoothWriteImpl {
        void onSuccessWrite(int status, String data) throws IOException;

        void onFailed(Exception e);
    }

    private static class BluetoothModuleHolder {
        private static final BluetoothModule instance = new BluetoothModule();
    }

}
