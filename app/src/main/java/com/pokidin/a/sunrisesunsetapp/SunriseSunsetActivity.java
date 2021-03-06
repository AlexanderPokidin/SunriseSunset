package com.pokidin.a.sunrisesunsetapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SunriseSunsetActivity extends AppCompatActivity {

    private static final String TAG = "SunriseSunsetActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private boolean mLocationPermissionsGranted = false;
    private PlaceAutocompleteFragment mAutocompleteFragment;

    private TextView mTvCity;
    private TextView mTvSunrise;
    private TextView mTvSunset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sunrise_sunset);

        mAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager()
                .findFragmentById(R.id.place_autocomplete_fragment);
        mTvCity = findViewById(R.id.tvCity);
        mTvSunrise = findViewById(R.id.tvSunriseTime);
        mTvSunset = findViewById(R.id.tvSunsetTime);

        if (PlaceItem.getPlaceItem().getName() != null){
            mTvCity.setText(PlaceItem.getPlaceItem().getName());
        }
        if (PlaceItem.getPlaceItem().getSunrise() != null){
            mTvSunrise.setText(PlaceItem.getPlaceItem().getSunrise());
        }
        if (PlaceItem.getPlaceItem().getSunset() != null){
            mTvSunset.setText(PlaceItem.getPlaceItem().getSunset());
        }

        getLocationPermission();
        if (mLocationPermissionsGranted) {
            getDeviseLocation();
        }

        getPlaceLocation();
    }

    //Select the place and get a location

    private void getPlaceLocation() {
        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mTvCity.setText(place.getName().toString());
                PlaceItem.getPlaceItem().setName(place.getName().toString());
                Log.d(TAG, "CityName: " + PlaceItem.getPlaceItem().getName());
                Log.d(TAG, "getPlaceLocation. Place: " + place.getName() + ", LatLng: " + place.getLatLng());

                PlaceItem.getPlaceItem().setLatLocation(place.getLatLng().latitude);
                PlaceItem.getPlaceItem().setLngLocation(place.getLatLng().longitude);

                Log.d(TAG, "getPlaceLocation. latitude: " + place.getLatLng().latitude + ", longitude: " + place.getLatLng().longitude);

                new SunriseSunsetAsyncTask().execute();
            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "An error occurred: " + status);
            }
        });
    }

    //Get current device location

    private void getDeviseLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        final FusedLocationProviderClient fusedLocationProviderClient
                = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {
                                Log.d(TAG, "Found location: lat " + currentLocation.getLatitude()
                                        + ", lng " + currentLocation.getLongitude());

                                PlaceItem.getPlaceItem().setLatLocation(currentLocation.getLatitude());
                                PlaceItem.getPlaceItem().setLngLocation(currentLocation.getLongitude());

                                new SunriseSunsetAsyncTask().execute();
                            } else {
                                Log.d(TAG, "onComplete (interior): current location is null");
                                Toast.makeText(SunriseSunsetActivity.this,
                                        "Unable to get current location",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "onComplete (external): current location is null");
                            Toast.makeText(SunriseSunsetActivity.this,
                                    "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (SecurityException se) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + se.getMessage());
        }
    }

    //Get permissions to access the device location

    private void getLocationPermission() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                }
        }
    }

    //Request and receive a sunrise and sunset times from the server

    private class SunriseSunsetAsyncTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            new SunriseSunsetFinder().getSunriseSunsetTime();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mTvSunrise.setText(PlaceItem.getPlaceItem().getSunrise());
            mTvSunset.setText(PlaceItem.getPlaceItem().getSunset());
            Log.d(TAG, "SunriseSunsetAsyncTask: onPostExecute was done");
        }
    }
}
