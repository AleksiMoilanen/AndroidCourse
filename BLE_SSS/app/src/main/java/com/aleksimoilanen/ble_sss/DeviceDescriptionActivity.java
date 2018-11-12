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

    private TextView descriptionMac;
    private TextView descriptionServices;
    private TextView descriptionCharacteristic;

    private Spinner gainSpinner;
    private Spinner spsSpinner;

    private UUID servUUID = convertFromInteger(0xaaa0);

    private UUID charUUID = convertFromInteger(0xaaa1);
    private UUID temperatureCharUUID = convertFromInteger(0xaaa2);
    private UUID humidityCharUUID = convertFromInteger(0xaaa3);

    private UUID descUUID = convertFromInteger(0x2901);

    //Notify descriptor UUID
    private UUID cccdUUID = convertFromInteger(0x2902);

    private UUID ledService_UUID = convertFromInteger(0xfff0);
    private UUID ledChar_UUID = convertFromInteger(0xfff1);

    public final static String ACTION_GATT_CONNECTED = "blereceiver.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "blereceiver.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "blereceiver.ACTION_GATT_SERVICES_DISCOVERED";

    public final static String ACTION_TEMPERATURERE_UPDATE = "blereceiver.ACTION_TEMPERATURERE_UPDATE";

    public final static String ACTION_MESSAGE_SERVICE_ONLINE = "blereceiver.ACTION_MESSAGE_SERVICE_ONLINE";

    public final static String EXTRA_TEMPERATURERE_DATA = "blereceiver.EXTRA_TEMPERATURERE_DATA";

    public final static String NOT_SUPPORT_TEMPERATURE_SERVICE = "blereceiver.NOT_SUPPORT_TEMPERATURE_SERVICE";

    private SeekBar seekBar;

    private boolean notifySubState = false;

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
            printharacteristic(characteristic);
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
            case "3":
                value[0] += (byte) (4);
                break;
            case "4":
                value[0] += (byte) (5);
                break;
            case "5":
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
        byte[] value = new byte[1];
        value[0] = (byte) (64);

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

    public void printharacteristic(BluetoothGattCharacteristic characteristic){
        byte[] data;

        //if (charUUID.equals(characteristic.getUuid())) {



        //int value = Integer.valueOf(wrap.get());

        Log.i(TAG, "PrintCharacteristic: " + String.valueOf(characteristic.getUuid()));
        //Log.d(TAG, String.format(HexUtils.displayHex(characteristic.getValue())));

        data = characteristic.getValue();
        ByteBuffer wrap = ByteBuffer.wrap(data);

        //double value = ByteBuffer.wrap(data).getDouble();

        //double kakka = Double.valueOf(wrap.get());
        //int value = Integer.valueOf(wrap.get());


        try{
            double kakka = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1);
            double value = ByteBuffer.wrap(data).getDouble();
            Log.i(TAG, String.valueOf(value));
            //Log.i(TAG, String.valueOf(kakka));
        } catch (Exception e){
            Log.i(TAG, e.toString());
        }


        /*
        final UUID uuid = characteristic.getUuid();

        Log.d(TAG, String.format("Received TX: %s", uuid + HexUtils.displayHex(characteristic.getValue())));

        try {
            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
            if (temperatureCharUUID.equals(uuid)) {
                double value = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1);
                //startNotificationForeground(value);

                final Intent intent = new Intent(ACTION_TEMPERATURERE_UPDATE);
                intent.putExtra("UUID", uuid);
                intent.putExtra(EXTRA_TEMPERATURERE_DATA, value);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            } else {
                Log.v(TAG, "[" + currentDateTimeString + "] UUID: " + uuid.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
            //Log.i(TAG, String.valueOf(value));
            //charValue.setText(String.valueOf(value));

            //new UpdateThingspeakTask(value, 1.1f).execute();
        //}*/
    }

    // Write custom characteristic value (from editText) to default service and characteristic
    public void onClickWriteCharacteristic(View view){

        EditText editValue = findViewById(R.id.editText);

        try {
            int val = Integer.valueOf(String.valueOf(editValue.getText()));

            writeCharacteristic(val, true);
        }
        catch (IllegalArgumentException e){
            Toast.makeText(this, "Syötä lähetettävä arvo", Toast.LENGTH_SHORT).show();
        }
    }

    // Write custom characteristic value (from editText) to custom service (editText3) and characteristic (editText2)
    public void onClickWriteCustomCharacteristic(View view){

        EditText editValue = findViewById(R.id.editText);
        EditText editService = findViewById(R.id.editText3);
        EditText editCharacteristic = findViewById(R.id.editText2);

        String serviceHex, characteristicHex;

        UUID tempServiceUUID, tempCharacteristicUUID;


        try
        {
            serviceHex = String.valueOf(editService.getText());
            int value = Integer.parseInt(serviceHex, 16);
            tempServiceUUID = UUID.fromString("000" + serviceHex + "-0000-1000-8000-00805f9b34fb");
            Log.d(TAG, "TOIMII");
        }
        catch(NumberFormatException nfe)
        {
            Log.d(TAG, "EI TOIMI");
            Toast.makeText(DeviceDescriptionActivity.this, R.string.invalid_serviceHex, Toast.LENGTH_LONG).show();
            return;
        }

        try
        {
            characteristicHex = String.valueOf(editCharacteristic.getText());
            int value = Integer.parseInt(characteristicHex, 16);
            tempCharacteristicUUID = UUID.fromString("000" + characteristicHex + "-0000-1000-8000-00805f9b34fb");
            Log.d(TAG, "TOIMII");
        }
        catch(NumberFormatException nfe)
        {
            Log.d(TAG, "EI TOIMI");
            Toast.makeText(DeviceDescriptionActivity.this, R.string.invalid_characteristicHex, Toast.LENGTH_LONG).show();
            return;
        }

        try {

            int val = Integer.valueOf(String.valueOf(editValue.getText()));
            writeCharacteristic(val, true, tempServiceUUID, tempCharacteristicUUID);

        }
        catch (IllegalArgumentException e){
            Toast.makeText(this, "Syötä lähetettävä arvo", Toast.LENGTH_SHORT).show();
        }
    }
}