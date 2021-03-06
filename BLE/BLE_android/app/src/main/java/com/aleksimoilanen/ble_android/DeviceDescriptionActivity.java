package com.aleksimoilanen.ble_android;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class DeviceDescriptionActivity extends AppCompatActivity {

    String TAG = "paska";

    private BluetoothGatt mBluetoothGatt;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private TextView descriptionMac;
    private TextView descriptionServices;
    private TextView descriptionCharacteristic;

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

        seekBar = (SeekBar) findViewById(R.id.seekBar);

        descriptionMac = (TextView) findViewById(R.id.device_description_mac);
        descriptionServices = (TextView) findViewById(R.id.device_description_services);
        descriptionCharacteristic = (TextView) findViewById(R.id.device_description_characteristics);

        descriptionMac.setText(device.getAddress());
        descriptionServices.setText("");
        descriptionCharacteristic.setText("");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                writeCharacteristic(progress, false, ledService_UUID, ledChar_UUID);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        connect(device);
    }


    public void connect(BluetoothDevice device) {
        if (mBluetoothGatt == null) {
            mBluetoothGatt = device.connectGatt(DeviceDescriptionActivity.this, false, mGattCallback);
        }
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


    // Read characteristic from device
    // servUUID, charUUID
    public void onClickReadCharacteristic(View button) {

        BluetoothGattCharacteristic Characteristic = mBluetoothGatt.getService(servUUID).getCharacteristic(charUUID);
        byte[] data;

        Log.i(TAG, "Read Characteristic");

        if (charUUID.equals(Characteristic.getUuid())) {

            data = Characteristic.getValue();
            ByteBuffer wrap = ByteBuffer.wrap(data);

            int value = Integer.valueOf(wrap.get());

            Log.i(TAG, String.valueOf(value));
            Toast.makeText(DeviceDescriptionActivity.this, "Arduinon arvo: " + value, Toast.LENGTH_LONG).show();
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

    //Change notify state between enabled/disabled (with notifyButton)
    public void onClickChangeNotifyState(View view) throws InterruptedException {

        Button notifyButton = (Button) findViewById(R.id.notifyButton);
        final BluetoothGattCharacteristic tempCharacteristic = mBluetoothGatt.getService(servUUID).getCharacteristic(temperatureCharUUID);
        BluetoothGattCharacteristic humCharacteristic = mBluetoothGatt.getService(servUUID).getCharacteristic(humidityCharUUID);

        if(notifySubState){
            notifyButton.setText("Start notify");
            setNotify(tempCharacteristic, false);
            TimeUnit.MILLISECONDS.sleep(100);
            setNotify(humCharacteristic, false);
        } else {
            notifyButton.setText("Stop notify");
            setNotify(tempCharacteristic, true);
            TimeUnit.MILLISECONDS.sleep(100);
            setNotify(humCharacteristic, true);
        }

        /*BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(servUUID).getCharacteristic(charUUID);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(cccdUUID);

        if(charUUID.equals(characteristic.getUuid())){
            if(notifySubState){
                notifyButton.setText("Start notify");
                mBluetoothGatt.setCharacteristicNotification(characteristic, false);
                notifySubState = false;

                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
                Log.d(TAG, "Don't notify pls");
            }
            else {
                notifyButton.setText("Stop notify");
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                notifySubState = true;

                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
                Log.d(TAG, "Notify pls");
            }
        }*/

        Log.i(TAG, String.valueOf(notifySubState));
    }

    private void setNotify(BluetoothGattCharacteristic characteristic, boolean state) {

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(cccdUUID);

        if (!state) {
            Log.i(TAG, "Set notify false");
            mBluetoothGatt.setCharacteristicNotification(characteristic, false);
            notifySubState = false;

            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        } else {
            Log.i(TAG, "Set notify true");
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            notifySubState = true;

            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    public void printharacteristic(BluetoothGattCharacteristic characteristic){
        TextView charValue = (TextView) findViewById(R.id.charValue);

        byte[] data;

        //if (charUUID.equals(characteristic.getUuid())) {



            //int value = Integer.valueOf(wrap.get());

            Log.i(TAG, "PrintCharacteristic: " + String.valueOf(characteristic.getUuid()));
            Log.d(TAG, String.format(HexUtils.displayHex(characteristic.getValue())));

            data = characteristic.getValue();
            ByteBuffer wrap = ByteBuffer.wrap(data);

            //double value = ByteBuffer.wrap(data).getDouble();

            //double kakka = Double.valueOf(wrap.get());
            //int value = Integer.valueOf(wrap.get());


            try{
                double kakka = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1);
                double value = ByteBuffer.wrap(data).getDouble();
                charValue.setText(String.valueOf(value));
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
