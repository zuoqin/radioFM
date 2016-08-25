package fm100.co.il.helpers;


import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;


import fm100.co.il.MainActivity;
import fm100.co.il.busEvents.DistanceBusEvent;

import org.greenrobot.eventbus.EventBus;
/************************************************
 * a Service that manufactor distance and speed
 ************************************************/

public class DistanceService extends Service {
    private LocationManager locManager;
    private LocationListener locListener = new myLocationListener();
    static final Double EARTH_RADIUS = 6371.00; // times 1000 to get in meters

    private boolean gps_enabled = false;
    private boolean network_enabled = false;

    private Handler handler = new Handler();
    Thread t;

    private double currentDistance;
    private double currentSpeed;
    // int to stop the starting thread
    int stop = 0;

    private int firstTime = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop = 1;
        stopSelf();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        firstTime = 0;
        currentDistance = 0;
        handler.post(r);

        return START_STICKY; //START_REDELIVER_INTENT , START_NOT_STICKY , START_STICKY
    }

    public void location() {

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            gps_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (gps_enabled) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
        }
        if (network_enabled) {
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
        }
    }

    private class myLocationListener implements LocationListener {
        double lat_old;
        double lon_old;
        double lat_new;
        double lon_new;
        double time = 2.5; // the delay between location "gets"
        double speed = 0.0;

        @Override
        public void onLocationChanged(Location location) {
            if (firstTime == 0){
                    lat_old = location.getLongitude();
                    lon_old = location.getLatitude();
                    firstTime = 1;
                }
                if (location != null) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.getMyApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.getMyApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request th
                        // e missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locManager.removeUpdates(locListener);
                    //String Speed = "Device Speed: " +location.getSpeed();
                    lat_new=location.getLongitude();
                    lon_new =location.getLatitude();
                    //String longitude = "Longitude: " +location.getLongitude();
                    //String latitude = "Latitude: " +location.getLatitude();

                    // method of calculating the distance between 2 points (*100 to show meters)
                    if (firstTime == 1){
                        double distance =CalculationByDistance(lat_new, lon_new, lat_old, lon_old)*100;
                        speed = ((distance/time)*18)/5; // km/h
                        // ------------ setting the data to pass on ----------------
                        currentDistance += distance;
                        currentSpeed = speed;
                        DistanceBusEvent event = new DistanceBusEvent(currentDistance , speed);
                        EventBus.getDefault().post(event);
                    }

                    //Toast.makeText(getApplicationContext(), longitude + "\n" + latitude + "\nDistance is: "
                    //      + distance + "\nSpeed is: " + speed, Toast.LENGTH_SHORT).show();

                    lat_old=lat_new;
                    lon_old=lon_new;

            }
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    public double CalculationByDistance(double lat1, double lon1, double lat2, double lon2) {


        double Radius = EARTH_RADIUS;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Radius * c;


        /*double R = EARTH_RADIUS; // km
        double dLat = (lat2-lat1)*Math.PI/180;
        double dLon = (lon2-lon1)*Math.PI/180;
        lat1 = lat1*Math.PI/180;
        lat2 = lat2*Math.PI/180;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c * 1000;

        return d;
       */

        /*
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
        */


    }
    private Runnable r = new Runnable() {
        public void run() {
            if (stop == 0) {
                location();
                handler.postDelayed(this, 2500);
            }
        }
    };

}
