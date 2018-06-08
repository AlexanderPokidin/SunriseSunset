package com.pokidin.a.sunrisesunsetapp;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SunriseSunsetFinder {
    private static final String TAG = "SunriseSunsetFinder";

    public String getUrl(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = urlConnection.getInputStream();
            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(urlConnection.getResponseMessage() + ": with " + urlStr);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return new String(out.toByteArray());
        } finally {
            urlConnection.disconnect();
        }
    }

    public void findItem() {
        try {
            String url = Uri.parse("https://api.sunrise-sunset.org/json").buildUpon()
                    .appendQueryParameter("lat", "50.4501")
                    .appendQueryParameter("lng", "30.5234")
                    .build().toString();
            String jsonString = getUrl(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject object = new JSONObject(jsonString);
            parseItem(object);
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch items", e);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        }
    }

    private void parseItem(JSONObject object) throws IOException, JSONException {
        JSONObject jsonObject = object.getJSONObject("results");
        String strSunrise = jsonObject.getString("sunrise");
        String strSunset = jsonObject.getString("sunset");

        Log.i(TAG, "Sunrise: " + jsonObject.getString("sunrise") + ". Sunset: " + jsonObject.getString("sunset"));

    }
}
