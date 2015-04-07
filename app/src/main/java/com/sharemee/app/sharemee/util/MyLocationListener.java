package com.sharemee.app.sharemee.util;

/**
 * Created by Marin on 18/03/2015.
 */
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import java.text.DecimalFormat;

public class MyLocationListener implements LocationListener {

    public static double latitude;
    public static double longitude;

    @Override
    public void onLocationChanged(Location loc)
    {
        loc.getLatitude();
        loc.getLongitude();
        latitude=loc.getLatitude();
        longitude=loc.getLongitude();
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        //print "Currently GPS is Disabled";
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
        Double distanceInter;
        Double radLat1=Math.toRadians(lat1);
        Double radLong1=Math.toRadians(long1);
        Double radLat2=Math.toRadians(lat2);
        Double radLong2=Math.toRadians(long2);
        Double distanceScaled;
        Double distanceRound;
        String distanceString;

        Double distLat=(radLat2-radLat1)/2;
        Double distLong=(radLong2-radLong1)/2;

        distanceInter=(Math.sin(distLat)*Math.sin(distLat))+ Math.cos(radLat1)*Math.cos(radLat2)*(Math.sin(distLong)*Math.sin(distLong));
        distanceFinale=2*Math.atan2(Math.sqrt(distanceInter),Math.sqrt(1-distanceInter));

        distanceScaled=(distanceFinale*rayonTerrestre);
        distanceRound=(Math.round(distanceScaled*100))/100.0;
        distanceString=distanceRound.toString();
        return distanceString;


    }
}
