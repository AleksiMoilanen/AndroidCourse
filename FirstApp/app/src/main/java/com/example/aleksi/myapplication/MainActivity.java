package com.example.aleksi.myapplication;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static SeekBar seek_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sliderFunction();
    }

    public void toastMe(View view){
        // Toast myToast = Toast.makeText(this, message, duration);
        Toast myToast = Toast.makeText(this, R.string.toastInfo,
                Toast.LENGTH_SHORT);
        myToast.show();
    }

    public void countMe (View view) {
        // Get the text view.
        TextView showCountTextView = (TextView)
                findViewById(R.id.textView);

        // Get the value of the text view.
        String countString = showCountTextView.getText().toString();

        // Convert value to a number and increment it.
        Integer count = Integer.parseInt(countString);

        if(count < 100)
        {
            count++;
        }

        // Display the new value in the text view.
        showCountTextView.setText(count.toString());

        SeekBar seekBar = (SeekBar) findViewById(R.id.valueSelector); // initiate the progress bar
        seekBar.setProgress(count); // 50 default progress value
    }

    public void clearMe (View view) {
        // Get the text view.
        TextView showCountTextView = (TextView)
                findViewById(R.id.textView);

        Integer count = 0;

        seek_bar = (SeekBar)findViewById(R.id.valueSelector);
        seek_bar.setProgress(count);

        // Display the new value in the text view.
        showCountTextView.setText(count.toString());
    }

    private static final String TOTAL_COUNT = "total_count";

    public void randomMe (View view) {

        // Create an Intent to start the second activity
        Intent randomIntent = new Intent(this, SecondActivity.class);

        // Get the text view that shows the count.
        TextView showCountTextView = (TextView) findViewById(R.id.textView);

        // Get the value of the text view.
        String countString = showCountTextView.getText().toString();

        // Convert the count to an int
        int count = Integer.parseInt(countString);

        // Add the count to the extras for the Intent.
        randomIntent.putExtra(TOTAL_COUNT, count);

        // Start the new activity.
        startActivity(randomIntent);
    }

    public void sliderFunction( ){
        seek_bar = (SeekBar)findViewById(R.id.valueSelector);

        seek_bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        TextView showCountTextView = (TextView)
                                findViewById(R.id.textView);
                        Integer value = progress;
                        showCountTextView.setText(value.toString());
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        Log.d("jaa", "ok");
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        TextView showCountTextView = (TextView)
                                findViewById(R.id.textView);
                        Integer value = seekBar.getProgress();
                        showCountTextView.setText(value.toString());
                    }
                }
        );
    }
}
