package com.aleksimoilanen.ble_android;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class UpdateThingspeakTask extends AsyncTask<Void, Void, String> {

    String TAG = "paska";

    private Exception exception;
    private double m_Temp = 0;
    private double m_Hum = 0;

    public UpdateThingspeakTask(double temp, double hum) {
        m_Temp = temp;
        m_Hum = hum;
    }

    //GET https://api.thingspeak.com/update?api_key=Q1OYD2IWGGK634DQ&field1=0

    protected void onPreExecute() {
    }

    protected String doInBackground(Void... urls) {
        try {
            URL url = new URL( "https://api.thingspeak.com/update?api_key=Q1OYD2IWGGK634DQ&field1=" + String.valueOf(m_Temp) + "&field2=" + String.valueOf(m_Hum));

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                bufferedReader.close();
                return stringBuilder.toString();
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            Log.i(TAG, e.getMessage(), e);
            return null;
        }
    }

    protected void onPostExecute(String response) {
        // We completely ignore the response
        // Ideally we should confirm that our update was successful
    }
}

