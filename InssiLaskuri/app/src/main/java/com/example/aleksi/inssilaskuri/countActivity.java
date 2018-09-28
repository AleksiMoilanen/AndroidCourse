package com.example.aleksi.inssilaskuri;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class countActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count);

        Bundle extras = getIntent().getExtras();

        double points = 0;
        int groupSize = extras.getInt("groupSize");
        int plateCount = extras.getInt("plateCount");

        switch (groupSize){
            case 3: points = plateCount * 5;
                break;
            case 4: points = plateCount * 3.75;
                break;
            case 5: points = plateCount * 3;
                break;
            default: break;
        }

        Log.d("KAKKA", String.valueOf(points));

        TextView printPoint = (TextView) findViewById(R.id.textView1);

        printPoint.setText(String.valueOf(points));
    }
}
