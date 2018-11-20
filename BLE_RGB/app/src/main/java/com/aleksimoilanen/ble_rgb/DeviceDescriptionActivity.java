package com.aleksimoilanen.ble_rgb;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;

import java.util.UUID;

public class DeviceDescriptionActivity extends AppCompatActivity {

    String TAG = "paska";

    private BluetoothGatt mBluetoothGatt;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private UUID servUUID = convertFromInteger(0xaaa0);
    private UUID charUUID = convertFromInteger(0xaaa1);

    private UUID descUUID = convertFromInteger(0x2901);

    //Notify descriptor UUID
    private UUID cccdUUID = convertFromInteger(0x2902);


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
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(TAG, "CHARACTERISTIC_READ");
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
            Log.i(TAG, "Data" + String.valueOf(data));
            Log.i(TAG, "Value" + String.valueOf(value));
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

        ColorPickerView CPV = findViewById(R.id.color_picker_view);

        CPV.addOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int selectedColor) {
                int r = (selectedColor >> 16) & 0xFF;
                int g = (selectedColor >> 8) & 0xFF;
                int b = (selectedColor >> 0) & 0xFF;
                Log.d(TAG, "R [" + r + "] - G [" + g + "] - B [" + b + "]");
                sendColor(r,g,b);

                writeCharacteristic(selectedColor, true);
            }
        });

        connect(device);
    }


    public void connect(BluetoothDevice device) {
        if (mBluetoothGatt == null) {
            mBluetoothGatt = device.connectGatt(DeviceDescriptionActivity.this, false, mGattCallback);
        }
    }

    public void colorPickerOnClick(View view){
        Log.i(TAG, "PASKA");

    }

    public void sendColor(int r, int g, int b){
        Log.i(TAG, String.valueOf(r) + String.valueOf(g) + String.valueOf(b));
    }

    // Write characteristic to bluetooth device (default service and characteristic)
    // servUUID, charUUID = default
    public void writeCharacteristic(int val, boolean print){
        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return;
        }

        BluetoothGattService Service = mBluetoothGatt.getService(servUUID);
        if (Service == null) {
            Log.e(TAG, "service not found!");
            if (print) Toast.makeText(DeviceDescriptionActivity.this, R.string.wrong_service, Toast.LENGTH_LONG).show();
            return;
        }
        BluetoothGattCharacteristic Characteristic = Service.getCharacteristic(charUUID);
        if (Characteristic == null) {
            Log.e(TAG, "char not found!");
            if (print) Toast.makeText(DeviceDescriptionActivity.this, R.string.wrong_chara, Toast.LENGTH_LONG).show();
            return;
        }

        byte[] value = new byte[1];
        value[0] = (byte) (val & 0xFF);

        Characteristic.setValue(value);
        mBluetoothGatt.writeCharacteristic(Characteristic);
    }

    // Write custom characteristic to custom service and characteristic
    // serviceUUID, charaUUID = custom
    public void writeCharacteristic(int val, boolean print, UUID serviceUUID, UUID charaUUID){

        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return;
        }

        BluetoothGattService Service = mBluetoothGatt.getService(serviceUUID);
        if (Service == null) {
            Log.e(TAG, "service not found!");
            if (print) Toast.makeText(DeviceDescriptionActivity.this, R.string.wrong_service, Toast.LENGTH_LONG).show();
            return;
        }

        BluetoothGattCharacteristic Characteristic = Service.getCharacteristic(charaUUID);
        if (Characteristic == null) {
            Log.e(TAG, "char not found!");
            if (print) Toast.makeText(DeviceDescriptionActivity.this, R.string.wrong_chara, Toast.LENGTH_LONG).show();
            return;
        }

        byte[] value = new byte[1];
        value[0] = (byte) (val & 0xFF);

        Characteristic.setValue(value);
        mBluetoothGatt.writeCharacteristic(Characteristic);

        if (print) Toast.makeText(DeviceDescriptionActivity.this, R.string.write_ok, Toast.LENGTH_LONG).show();

    }
}
