package com.aleksi.mapapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final String IntentMessage = "com.aleksi.mapapp.msg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Sets screen rotation locked
    }


    public void openMapMe(View view){
        Intent intent = new Intent(this, MapsActivity.class);

        Bundle extras = new Bundle();
        extras.putChar("OPERATOR", 'M');

        intent.putExtras(extras);
        startActivity(intent);
    }

    public void openMapPos(View view){
        Intent intent = new Intent(this, MapsActivity.class);

        Bundle extras = new Bundle();
        extras.putChar("OPERATOR", 'L');

        intent.putExtras(extras);
        startActivity(intent);
    }

    public void openMapCoord(View view){
        Intent intent = new Intent(this, MapsActivity.class);

        EditText CoordLat = (EditText) findViewById(R.id.CoordLat);
        EditText CoordLong = (EditText) findViewById(R.id.CoordLong);


        if(!CoordLat.getText().toString().isEmpty() || !CoordLong.getText().toString().isEmpty()){
            Double latCoord = Double.parseDouble(CoordLat.getText().toString());
            Double longCoord = Double.parseDouble(CoordLong.getText().toString());

            Log.d("COORD", latCoord.toString() + longCoord.toString());

            if (longCoord < 180 && longCoord > -180) {
                if (latCoord < 85 && latCoord > -85) {
                    Bundle extras = new Bundle();
                    extras.putChar("OPERATOR", 'C');
                    extras.putDouble("CoordLat", latCoord);
                    extras.putDouble("CoordLong", longCoord);

                    intent.putExtras(extras);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Lat value have to be between 85 and -85", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Long value have to be between 180 and -180", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Insert both values", Toast.LENGTH_LONG).show();
            Log.d("COORD", CoordLat.getText().toString() + CoordLong.getText().toString());
        }
    }


}
