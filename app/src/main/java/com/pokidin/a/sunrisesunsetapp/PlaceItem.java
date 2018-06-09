package com.pokidin.a.sunrisesunsetapp;

public class PlaceItem {
    private static PlaceItem sPlaceItem;

    private PlaceItem() {

    }

    public static PlaceItem getPlaceItem() {
        if (sPlaceItem == null) {
            sPlaceItem = new PlaceItem();
        }
        return sPlaceItem;
    }

    private String mName;
    private Double mLatLocation;
    private Double mLngLocation;
    private String mSunrise;
    private String mSunset;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Double getLatLocation() {
        return mLatLocation;
    }

    public void setLatLocation(Double latLocation) {
        mLatLocation = latLocation;
    }

    public Double getLngLocation() {
        return mLngLocation;
    }

    public void setLngLocation(Double lngLocation) {
        mLngLocation = lngLocation;
    }

    public String getSunrise() {
        return mSunrise;
    }

    public void setSunrise(String sunrise) {
        mSunrise = sunrise;
    }

    public String getSunset() {
        return mSunset;
    }

    public void setSunset(String sunset) {
        mSunset = sunset;
    }
}
