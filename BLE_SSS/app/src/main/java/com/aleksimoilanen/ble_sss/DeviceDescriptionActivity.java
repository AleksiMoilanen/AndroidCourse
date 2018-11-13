package com.aleksimoilanen.ble_sss;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DeviceDescriptionActivity extends AppCompatActivity {

    String TAG = "paska";

    private BluetoothGatt mBluetoothGatt;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;



    private Spinner gainSpinner;
    private Spinner spsSpinner;
    private Spinner readReqSpinner;

    private UUID servUUID = convertFromInteger(0xfff0);

    private UUID charUUID = convertFromInteger(0xfff1);
    private UUID adcValueCharUUID = convertFromInteger(0xfff2);

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
                    finish();
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

            BluetoothGattCharacteristic chara = gatt.getService(servUUID).getCharacteristic(adcValueCharUUID);
            Log.i(TAG, String.valueOf(chara));

            gatt.setCharacteristicNotification(chara, true);

            BluetoothGattDescriptor desc = chara.getDescriptor(cccdUUID);
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(desc);
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
            printCharacteristic(characteristic);
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

        gainSpinner = findViewById(R.id.gain_spinner);
        ArrayAdapter<CharSequence> gainAdapter = ArrayAdapter.createFromResource(this,
                R.array.gain_array, android.R.layout.simple_spinner_item);
        gainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gainSpinner.setAdapter(gainAdapter);

        spsSpinner = findViewById(R.id.sps_spinner);
        ArrayAdapter<CharSequence> spsAdapter = ArrayAdapter.createFromResource(this,
                R.array.sps_array, android.R.layout.simple_spinner_item);
        spsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spsSpinner.setAdapter(spsAdapter);

        readReqSpinner = findViewById(R.id.read_spinner);
        ArrayAdapter<CharSequence> readReqAdapter = ArrayAdapter.createFromResource(this,
                R.array.readReq_array, android.R.layout.simple_spinner_item);
        readReqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        readReqSpinner.setAdapter(readReqAdapter);

        connect(device);
    }


    public void connect(BluetoothDevice device) {
        if (mBluetoothGatt == null) {
            mBluetoothGatt = device.connectGatt(DeviceDescriptionActivity.this, false, mGattCallback);
        }
    }

    //TODO: send characteristics
    public void onClickChangeSettings(View view){
        String selectedGain = gainSpinner.getSelectedItem().toString();
        String selectedSPS = spsSpinner.getSelectedItem().toString();

        Log.i(TAG, String.valueOf(selectedGain) + " " + String.valueOf(selectedSPS));


        //write characteristic
        byte[] value = new byte[1];
        value[0] = (byte) (0x00);

        switch (selectedGain){
            case "2/3 (default)":
                value[0] += (byte) (1);
                break;
            case "1":
                value[0] += (byte) (2);
                break;
            case "2":
                value[0] += (byte) (3);
                break;
            case "4":
                value[0] += (byte) (4);
                break;
            case "8":
                value[0] += (byte) (5);
                break;
            case "16":
                value[0] += (byte) (6);
                break;
            default:
                break;
        }

        switch (selectedSPS){
            case "1600 (default)":
                value[0] += (byte) (40);
                break;
            case "128":
                value[0] += (byte) (8);
                break;
            case "250":
                value[0] += (byte) (16);
                break;
            case "490":
                value[0] += (byte) (24);
                break;
            case "920":
                value[0] += (byte) (32);
                break;
            case "2400":
                value[0] += (byte) (48);
                break;
            case "3300":
                value[0] += (byte) (56);
                break;
            default:
                break;
        }

        Log.d(TAG, String.valueOf(value[0]));
        writeCharacteristic(value[0], true);
    }

    public void onClickReadValue(View view){
        String selectedChannel = readReqSpinner.getSelectedItem().toString();

        byte[] value = new byte[1];
        value[0] = (byte) (0x00);

        switch (selectedChannel){
            case "1":
                value[0] += (byte) (64);
                break;
            case "2":
                value[0] += (byte) (128);
                break;
            case "3":
                value[0] += (byte) (192);
                break;
            case "4":
                value[0] += (byte) (255);
                break;
            default:
                break;
        }

        writeCharacteristic(value[0], true);
    }


    public void writeCharacteristic(byte[] value, boolean print){
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

        Characteristic.setValue(value);
        mBluetoothGatt.writeCharacteristic(Characteristic);
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

    public void printCharacteristic(BluetoothGattCharacteristic characteristic){
        byte[] data;
        data = characteristic.getValue();
        Log.i(TAG, "Size: " + data.length + " PrintCharacteristic: " + String.valueOf(characteristic.getUuid()));

        //Log.i(TAG, "PASKA: " + String.valueOf(data[1]) + " : " + String.valueOf(data[0]));

        int kakka = data[0];
        if (kakka < 0)
            kakka = 256 + kakka;
        kakka |= data[1] << 8;

        TextView valueText = (TextView) findViewById(R.id.adcValueView);

        try{
            valueText.setText(String.valueOf(kakka));
        }catch (Exception e){
            Log.i(TAG, String.valueOf(e));
        }

        Log.i(TAG, String.valueOf(kakka));
    }
}