package com.pokidin.a.sunrisesunsetapp;

import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SunriseSunsetFinder {
    private static final String TAG = "SunriseSunsetFinder";


    private String getUrl(String urlStr) throws IOException {
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

    public void timeFinder() {
        try {
            String url = Uri.parse("https://api.sunrise-sunset.org/json").buildUpon()
                    .appendQueryParameter("lat", "" + PlaceItem.getPlaceItem().getLatLocation())
                    .appendQueryParameter("lng", "" + PlaceItem.getPlaceItem().getLngLocation())
                    .build().toString();
            String jsonString = getUrl(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject object = new JSONObject(jsonString);
            parseItem(object);
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch items", e);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse time", e);
        }
    }

    private void parseItem(JSONObject object) throws ParseException, JSONException {
        JSONObject jsonObject = object.getJSONObject("results");

        PlaceItem.getPlaceItem().setSunrise(timeFixer(jsonObject.getString("sunrise")));
        PlaceItem.getPlaceItem().setSunset(timeFixer(jsonObject.getString("sunset")));

        Log.i(TAG, "Sunrise: " + jsonObject.getString("sunrise") + ". Sunset: " + jsonObject.getString("sunset"));
    }

    //Formatting a sunrise and sunset time for the device settings

    private String timeFixer(String time) throws ParseException {
        String formattedDate;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            df.setTimeZone(android.icu.util.TimeZone.getTimeZone("UTC"));
            Date date = df.parse(time);
            df.setTimeZone(android.icu.util.TimeZone.getDefault());
            formattedDate = df.format(date);
        } else {
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = df.parse(time);
            df.setTimeZone(TimeZone.getDefault());
            formattedDate = df.format(date);
        }
        return formattedDate;
    }
}
