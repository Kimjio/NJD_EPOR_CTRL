package com.nojunjae.epor;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.lang.ref.WeakReference;

import androidx.core.content.ContextCompat;

import static android.content.Context.LOCATION_SERVICE;

@Deprecated
public class GPSTask {

    static boolean isGPSEnabled = false;
    // 네트워크 사용유무
    static boolean isNetworkEnabled = false;
    // GPS 상태값
    static boolean isGetLocation = false;
    static Location location;
    static double lat;
    static double lon;
    private static LocationManager locationManager;
    private Context context;

    public GPSTask(Context context) {
        this.context = context;
        new GetLocation(context)
                .setTaskListener(
                        new GetLocation.TaskListener() {
                            @Override
                            public void onTaskFinished(Location location) {
                                GPSTask.location = location;
                            }
                        })
                .execute();
    }

    public double getLatitude() {
        if (location != null) lat = location.getLatitude();
        return lat;
    }

    /*public void StopUsingGPS()
    {
        if(locationManager!= null)
            locationManager.removeUpdates(GPSTask.this);
    }*/

    public double getLongitude() {
        if (location != null) lon = location.getLongitude();
        return lon;
    }

    private static class GetLocation extends AsyncTask<Void, Integer, Location>
            implements LocationListener {

        private final WeakReference<Context> contextWeakReference;

        private TaskListener taskListener;

        public GetLocation(Context context) {
            this.contextWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Location doInBackground(Void... voids) {
            if (Build.VERSION.SDK_INT >= 23
                    && ContextCompat.checkSelfPermission(
                    contextWeakReference.get(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                    contextWeakReference.get(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                return null;
            }
            try {
                locationManager =
                        (LocationManager)
                                contextWeakReference.get().getSystemService(LOCATION_SERVICE);

                // 현재 네트워크 상태 값 알아오기
                isNetworkEnabled =
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (isNetworkEnabled) {
                    isGetLocation = true;
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, 60000, 10, GetLocation.this);
                    Log.d("TAG", "NETWORKENABLED");

                    if (locationManager != null) {
                        location =
                                locationManager.getLastKnownLocation(
                                        LocationManager.NETWORK_PROVIDER);
                        Log.d("TAG", "a");
                        if (location != null) {
                            lat = location.getLatitude();
                            lon = location.getLongitude();
                            Log.d("TAG", "getLocation: ");
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return location;
        }

        GetLocation setTaskListener(TaskListener taskListener) {
            this.taskListener = taskListener;
            return this;
        }

        @Override
        protected void onPostExecute(Location location) {
            super.onPostExecute(location);
            if (taskListener != null) taskListener.onTaskFinished(location);
        }

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }

        public interface TaskListener {
            void onTaskFinished(Location location);
        }
    }
}
