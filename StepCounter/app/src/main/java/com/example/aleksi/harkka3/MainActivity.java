package com.example.aleksi.harkka3;

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener, StepListener {

    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;

    TextView stepview;
    private int numSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Sets screen rotation locked

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        Button BtnStart = (Button) findViewById(R.id.startButton);
        Button BtnStop = (Button) findViewById(R.id.stopButton);

        BtnStop.setEnabled(false);

        BtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                stepview = (TextView) findViewById(R.id.stepView);
                numSteps = 0;

                stepview.setText(String.valueOf(numSteps));

                Button startButton = (Button) findViewById(R.id.startButton);
                startButton.setEnabled(false);
                Button stopButton = (Button) findViewById(R.id.stopButton);
                stopButton.setEnabled(true);

                sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
            }
        });

        BtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sensorManager.unregisterListener(MainActivity.this);

                Button startButton = (Button) findViewById(R.id.startButton);
                startButton.setEnabled(true);
                Button stopButton = (Button) findViewById(R.id.stopButton);
                stopButton.setEnabled(false);
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        stepview = (TextView) findViewById(R.id.stepView);

        numSteps = Integer.parseInt(stepview.getText().toString());
        numSteps++;

        stepview.setText(String.valueOf(numSteps));
    }
}