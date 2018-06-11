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
    private static String timeZoneId;

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

    //Getting and calculating local time

    public void getLocalTime() {

        try {
            String url = Uri.parse("https://api.sunrise-sunset.org/json").buildUpon()
                    .appendQueryParameter("lat", "" + PlaceItem.getPlaceItem().getLatLocation())
                    .appendQueryParameter("lng", "" + PlaceItem.getPlaceItem().getLngLocation())
                    .appendQueryParameter("formatted", "0")
                    .build().toString();

            Log.d(TAG, "getLocalTime. lat:" + "" + PlaceItem.getPlaceItem().getLatLocation()
                    + "!lng:" + "" + PlaceItem.getPlaceItem().getLngLocation());
            String jsonString = getUrl(url);
            Log.i(TAG, "getLocalTime. Received JSON: " + jsonString);
            JSONObject object = new JSONObject(jsonString);
            getTimeZone();
            parseUtcTime(object);
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch items", e);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse time", e);
        }
    }

    private void parseUtcTime(JSONObject object) throws ParseException, JSONException {
        JSONObject jsonObject = object.getJSONObject("results");

        PlaceItem.getPlaceItem().setSunrise(timeFixer(jsonObject.getString("sunrise")));
        PlaceItem.getPlaceItem().setSunset(timeFixer(jsonObject.getString("sunset")));

        Log.i(TAG, "parseUtcTime. Check! Sunrise: " + PlaceItem.getPlaceItem().getSunrise()
                + ". Sunset: " + PlaceItem.getPlaceItem().getSunset());
    }

    //Getting timezone for local time calculation

    private void getTimeZone() {
        try {
            String url = Uri.parse("https://maps.googleapis.com/maps/api/timezone/json").buildUpon()
                    .appendQueryParameter("location", "" + PlaceItem.getPlaceItem().getLatLocation()
                            + "," + PlaceItem.getPlaceItem().getLngLocation())
                    .appendQueryParameter("timestamp", getTimestamp())
                    .build().toString();
            String jsonString = getUrl(url);
            Log.i(TAG, "getTimeZone. Received JSON: " + jsonString);
            JSONObject object = new JSONObject(jsonString);
            parseTimeZone(object);
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch items", e);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse timezone", e);
        }
    }

    private void parseTimeZone(JSONObject object) throws ParseException, JSONException {
        object.getString("dstOffset");
        object.getString("rawOffset");
        timeZoneId = object.getString("timeZoneId");

        Log.d(TAG, "parseTimeZone. timeZoneId: " + object.getString("timeZoneId"));
    }

    //Formatting a sunrise and sunset time for the local time

    private String timeFixer(String utcTime) throws ParseException {
        String formattedTime;
        String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
        java.text.SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = sdf.parse(utcTime);
        sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone(timeZoneId));
        formattedTime = sdf.format(date);

        Log.d(TAG, "timeFixer. timeFixer: " + timeZoneId + ", formattedTime: " + formattedTime);

        return formattedTime;
    }

    private String getTimestamp() {
        Long tsLong = System.currentTimeMillis() / 1000;
        return tsLong.toString();
    }
}
