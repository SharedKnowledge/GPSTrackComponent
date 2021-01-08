package net.gpstrackapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

//TODO onDestroy is called when device enters sleep mode
public class LocationService extends Service {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private NotificationChannel channel;

    private long updateMinTime = 0;
    private float updateMinDistance = 0;
    // accuracy is the radius of 68% confidence
    private float maxHorizontalAccuracy = Float.MAX_VALUE;

    @Override
    public void onCreate() {
        super.onCreate();
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startInForeground();
        }
        */
        Log.d(getLogStart(), "onCreate");
    }

    /*
    private void startInForeground() {
        String NOTIFICATION_CHANNEL_ID = "location_service";
        String channelName = "Location Service";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(1, notification);
    }
     */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new GpsLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return START_STICKY;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateMinTime, updateMinDistance, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, updateMinTime, updateMinDistance, locationListener);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setUpdateMinTime(long updateMinTime) {
        this.updateMinTime = updateMinTime;
    }

    public void setUpdateMinDistance(float updateMinDistance) {
        this.updateMinDistance = updateMinDistance;
    }

    public void setMaxHorizontalAccuracy(float maxHorizontalAccuracy) {
        this.maxHorizontalAccuracy = maxHorizontalAccuracy;
    }

    public long getUpdateMinTime() {
        return updateMinTime;
    }

    public float getUpdateMinDistance() {
        return updateMinDistance;
    }

    public float getMaxHorizontalAccuracy() {
        return maxHorizontalAccuracy;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(getLogStart(), "onDestroy");
        locationManager.removeUpdates(locationListener);
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }

    public class GpsLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location.getAccuracy() <= maxHorizontalAccuracy) {
                Intent intent = new Intent("LOCATION UPDATE");
                intent.putExtra("location", location);
                sendBroadcast(intent);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}