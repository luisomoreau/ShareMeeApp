package com.sharemee.app.sharemee.util;

/**
 * Created by Marin on 18/03/2015.
 */
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.text.DecimalFormat;

public class MyLocationListener implements LocationListener {

    public static double latitudePhone;
    public static double longitudePhone;
    private LocationManager lManager;
    private Location location1;

    @Override
    public void onLocationChanged(Location location)
    {
        lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        latitudePhone=location1.getLatitude();
        longitudePhone=location1.getLongitude();
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        // "Currently GPS is Disabled";
    }
    @Override
    public void onProviderEnabled(String provider)
    {
        //print "GPS got Enabled";
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    public static String calculerDistance(Double lat1, Double long1, Double lat2, Double long2){
        Double rayonTerrestre = 6378137.0;
        Double distanceFinale;

        Double radLat1=Math.toRadians(lat1);
        Double radLong1=Math.toRadians(long1);
        Double radLat2=Math.toRadians(lat2);
        Double radLong2=Math.toRadians(long2);
        Long distanceRound;
        String distanceString;


        distanceFinale=rayonTerrestre*Math.acos(Math.sin(radLat1)*Math.sin(radLat2)+Math.cos(radLat1)*Math.cos(radLat2)*Math.cos(radLong1-radLong2));

        distanceRound=(Math.round(distanceFinale)/1000);
        if(distanceRound<=0.5){
            distanceString="0.5";
        }
        else{
            distanceString=distanceRound.toString();}
        return distanceString;


    }

    public static double getLongitudePhone() {
        return longitudePhone;
    }

    public static void setLongitudePhone(double longitudePhone) {
        MyLocationListener.longitudePhone = longitudePhone;
    }

    public static double getLatitudePhone() {
        return latitudePhone;
    }

    public static void setLatitudePhone(double latitudePhone) {
        MyLocationListener.latitudePhone = latitudePhone;
    }
}
