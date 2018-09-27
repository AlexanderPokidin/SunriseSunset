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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SunriseSunsetFinder {
    private static final String TAG = "SunriseSunsetFinder";

    private String getSunriseSunsetData(String urlStr) throws IOException {
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
            Log.d(TAG, "getSunriseSunsetData. !!!: " + new String(out.toByteArray()));
            return new String(out.toByteArray());
        } finally {
            urlConnection.disconnect();
        }
    }

    public void getSunriseSunsetTime() {

        try {
            String url = Uri.parse("https://api.sunrise-sunset.org/json").buildUpon()
                    .appendQueryParameter("lat", "" + PlaceItem.getPlaceItem().getLatLocation())
                    .appendQueryParameter("lng", "" + PlaceItem.getPlaceItem().getLngLocation())
                    .appendQueryParameter("date", "today")
                    .build().toString();
            String jsonString = getSunriseSunsetData(url);
            Log.d(TAG, "getSunriseSunset. URL: " + url);
            Log.d(TAG, "getSunriseSunset. Received JSON: " + jsonString);

            JSONObject object = new JSONObject(jsonString);
            parseSunriseSunsetData(object);

        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch items", e);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse time", e);
        }
    }

    private void parseSunriseSunsetData(JSONObject object) throws ParseException, JSONException {
        JSONObject jsonObject = object.getJSONObject("results");
        PlaceItem.getPlaceItem().setSunrise(timeFixer(jsonObject.getString("sunrise")));
        PlaceItem.getPlaceItem().setSunset(timeFixer(jsonObject.getString("sunset")));
        Log.d(TAG, "parseSunriseSunsetData. Check! Sunrise: " + PlaceItem.getPlaceItem().getSunrise()
                + ". Sunset: " + PlaceItem.getPlaceItem().getSunset());
    }

    //Convert a sunrise and sunset times to local time

    private String timeFixer(String utcTime) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm:ss a");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = simpleDateFormat.parse(utcTime);
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        String formattedTime = simpleDateFormat.format(date);
        Log.d(TAG, "timeFixer: " + formattedTime);
        return formattedTime;
    }
}
