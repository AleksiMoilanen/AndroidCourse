package com.example.aleksi.inssilaskuri;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void button1Clicked(View view){

        int count = 0;
        EditText plateCount = (EditText) findViewById(R.id.editText);

        try{
             count = Integer.parseInt(plateCount.getText().toString());
        } catch (NumberFormatException nfe) {
            Toast myToast = Toast.makeText(this, "Syötä numero", Toast.LENGTH_LONG);
        }


        if(count != 0)
        {
            Intent intent = new Intent(this, countActivity.class);

            Bundle extras = new Bundle();

            extras.putInt("groupSize", 3);
            extras.putInt("plateCount", count);

            intent.putExtras(extras);
            startActivity(intent);
        }
    }

    public void button2Clicked(View view){
        int count = 0;
        EditText plateCount = (EditText) findViewById(R.id.editText);

        try{
            count = Integer.parseInt(plateCount.getText().toString());
        } catch (NumberFormatException nfe) {
            Toast myToast = Toast.makeText(this, "Syötä numero", Toast.LENGTH_LONG);
        }


        if(count != 0)
        {
            Intent intent = new Intent(this, countActivity.class);

            Bundle extras = new Bundle();

            extras.putInt("groupSize", 4);
            extras.putInt("plateCount", count);

            intent.putExtras(extras);
            startActivity(intent);
        }
    }

    public void button3Clicked(View view){
        int count = 0;
        EditText plateCount = (EditText) findViewById(R.id.editText);

        try{
            count = Integer.parseInt(plateCount.getText().toString());
        } catch (NumberFormatException nfe) {
            Toast myToast = Toast.makeText(this, "Syötä numero", Toast.LENGTH_LONG);
        }


        if(count != 0)
        {
            Intent intent = new Intent(this, countActivity.class);

            Bundle extras = new Bundle();

            extras.putInt("groupSize", 5);
            extras.putInt("plateCount", count);

            intent.putExtras(extras);
            startActivity(intent);
        }
    }

    public void minusCount(View view){
        EditText plateCount = (EditText) findViewById(R.id.editText);

        try{
            Integer count = Integer.parseInt(plateCount.getText().toString());
            if( count > 0){
                count--;
            }

            plateCount.setText(count.toString());
        } catch (NumberFormatException nfe) {
            Toast myToast = Toast.makeText(this, "Syötä numero", Toast.LENGTH_LONG);
        }
    }

    public void plusCount(View view){
        EditText plateCount = (EditText) findViewById(R.id.editText);

        try{
            Integer count = Integer.parseInt(plateCount.getText().toString());

            count ++;
            plateCount.setText(count.toString());

        } catch (NumberFormatException nfe) {
            Toast myToast = Toast.makeText(this, "Syötä numero", Toast.LENGTH_LONG);
        }


    }
}
