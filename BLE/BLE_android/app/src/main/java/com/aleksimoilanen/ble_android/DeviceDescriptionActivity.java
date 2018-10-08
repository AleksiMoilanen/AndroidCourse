package com.aleksimoilanen.ble_android;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class DeviceDescriptionActivity extends AppCompatActivity {

    String TAG = "DEV_DEBUG_DDA";

    private BluetoothGatt mBluetoothGatt;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private TextView descriptionMac;
    private TextView descriptionServices;
    private TextView descriptionCharacteristic;
    private Button buttonReadCharacteristic;

    private UUID servUUID = convertFromInteger(0xaaa0);
    private UUID charUUID = convertFromInteger(0xaaa1);
    private UUID descUUID = convertFromInteger(0x2901);

    public UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    public final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            switch (newState) {
                case STATE_CONNECTED:
                    Log.i(TAG, "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case STATE_DISCONNECTED:
                    Log.e(TAG, "STATE_DISCONNECTED");
                    break;
                case STATE_CONNECTING:
                    Log.i(TAG, "STATE_CONNECTING");
                    break;
                default:
                    Log.e(TAG, "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            Log.i(TAG, "SERVICE_DISCOVERED");

            BluetoothGattCharacteristic chara = gatt.getService(servUUID).getCharacteristic(charUUID);
            Log.i(TAG, String.valueOf(chara));

            gatt.setCharacteristicNotification(chara, true);

            BluetoothGattDescriptor desc = chara.getDescriptor(descUUID);
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(desc);

            /*
            final List<BluetoothGattService> services = gatt.getServices();

            for (int i = 0; i < services.size(); i++) {
                final int localIndex = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        descriptionServices.append(services.get(localIndex).getUuid().toString() + "\n");
                    }
                });
            }*/

            buttonReadCharacteristic.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(TAG, "CHARACTERISTIC_READ");

            readCounterCharacteristic(characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(TAG, "CHARACTERISTIC_CHANGED");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(TAG, "CHARACTERISTIC_WRITE");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            if (descUUID.equals(descriptor.getUuid())) {
                BluetoothGattCharacteristic characteristic = gatt
                        .getService(servUUID)
                        .getCharacteristic(charUUID);
                gatt.readCharacteristic(characteristic);
            }
        }
    };

    private void readCounterCharacteristic(BluetoothGattCharacteristic
                                                   characteristic) {
        Log.i(TAG, "read");
        if (charUUID.equals(characteristic.getUuid())) {

            String asd = characteristic.getStringValue(0);
            int value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
            byte[] data = characteristic.getValue();
            Log.i(TAG, String.valueOf(data));
            Log.i(TAG, String.valueOf(value));
            Log.i(TAG, asd);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothGatt.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_description);

        BluetoothDevice device = getIntent().getExtras().getParcelable("Bluetooth_Device");

        descriptionMac = (TextView) findViewById(R.id.device_description_mac);
        descriptionServices = (TextView) findViewById(R.id.device_description_services);
        descriptionCharacteristic = (TextView) findViewById(R.id.device_description_characteristics);
        buttonReadCharacteristic = (Button) findViewById(R.id.readCharacteristicsButton);
        buttonReadCharacteristic.setVisibility(View.INVISIBLE);

        descriptionMac.setText(device.getAddress());
        descriptionServices.setText("");
        descriptionCharacteristic.setText("");

        connect(device);
    }


    public void connect(BluetoothDevice device) {
        if (mBluetoothGatt == null) {
            mBluetoothGatt = device.connectGatt(DeviceDescriptionActivity.this, false, mGattCallback);
        }
    }

    public void onClickReadCharacteristic(View button) {

        BluetoothGattCharacteristic Characteristic = mBluetoothGatt.getService(servUUID).getCharacteristic(charUUID);

        Log.i(TAG, "Read Characteristic");

        if (charUUID.equals(Characteristic.getUuid())) {

            byte[] data = Characteristic.getValue();
            Log.i(TAG, String.valueOf(data));

            int value = Characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
            Log.i(TAG, String.valueOf(value));
        }

        /*
        final List<BluetoothGattCharacteristic> characteristics = mBluetoothGatt.getService(serviceUUID).getCharacteristics();
        descriptionServices.append("Reading Characteristics: \n");
        for (int i = 0; i < characteristics.size(); i++) {
            final int localIndex = i;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    descriptionServices.append(characteristics.get(localIndex).getUuid().toString() + "\n");
                }
            });
        }
        */
    }

    public void onClickWriteCharacteristic(View view){

        EditText editText = findViewById(R.id.editText);
        int val = Integer.valueOf(String.valueOf(editText.getText()));

        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return;
        }

        BluetoothGattService Service = mBluetoothGatt.getService(servUUID);
        if (Service == null) {
            Log.e(TAG, "service not found!");
            return;
        }
        BluetoothGattCharacteristic Characteristic = Service.getCharacteristic(charUUID);
        if (Characteristic == null) {
            Log.e(TAG, "char not found!");
            return;
        }

        byte[] value = new byte[1];
        value[0] = (byte) (val & 0xFF);

        Characteristic.setValue(value);
        mBluetoothGatt.writeCharacteristic(Characteristic);
        return;
    }
}
