package com.example.aleksi.sensorlist;

import android.app.Activity;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.text.method.ScrollingMovementMethod;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.TextView;

import java.util.List;
import android.hardware.Sensor;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private SensorManager hrSensorManager;

    private Sensor hrSensor;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv1 = (TextView) findViewById(R.id.textView2);
        tv1.setMovementMethod(new ScrollingMovementMethod());
        tv1.setVisibility(View.GONE);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> mList= mSensorManager.getSensorList(Sensor.TYPE_ALL);

        hrSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        hrSensor = hrSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        for (int i = 1; i < mList.size(); i++) {
            tv1.setVisibility(View.VISIBLE);
            tv1.append("\n \n " + mList.get(i).getName() + " \n " + mList.get(i).getVendor() + "\n Version: " + mList.get(i).getVersion());
        }
    }

    @Override
    public final void onSensorChanged(SensorEvent event){
        float heartRate = event.values[0];

        TextView tv3 = (TextView) findViewById(R.id.textView3);
        Log.d("Sensor", String.valueOf(heartRate));
        tv3.setText(String.valueOf(heartRate));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume(){
        super.onResume();
        hrSensorManager.registerListener(this, hrSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        hrSensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}
        return super.onOptionsItemSelected(item);
    }
}