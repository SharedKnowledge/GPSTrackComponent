package net.gpstrackapp.location;

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
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class LocationService extends Service {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private NotificationManager notificationManager;
    private NotificationChannel locationChannel;

    private static boolean askedUserPermission = false;
    private static boolean startInForeground = true;

    // in milliseconds
    private long updateMinTime = 3000;
    // in meters
    private float updateMinDistance = 5;
    // accuracy is the radius of 68% confidence
    private float maxHorizontalAccuracy = 30;

    @Override
    public void onCreate() {
        super.onCreate();

        if (startInForeground && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // this is necessary because startForeground() has to be called within 5 seconds of startForegroundService()
            // see https://developer.android.com/about/versions/oreo/android-8.0-changes.html for more infos
            startInForeground();
        }

        Log.d(getLogStart(), "onCreate");
    }

    public static void setAskedUserPermission(boolean askedUserPermission) {
        LocationService.askedUserPermission = askedUserPermission;
    }

    public static boolean hasAskedUserPermission() {
        return askedUserPermission;
    }

    public static void setStartInForeground(boolean startInForeground) {
        LocationService.startInForeground = startInForeground;
    }

    public static boolean shouldStartInForeground() {
        return startInForeground;
    }

    public static void showStartInForegroundDialog(Intent serviceIntent, Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage("Run location service in foreground? Recording can only continue in doze mode if you select "
                + "\'Yes\', but it will drain the battery more. In case this is not needed select \'No\'.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    LocationService.setStartInForeground(true);
                    startLocationService(serviceIntent, ctx);
                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    LocationService.setStartInForeground(false);
                    startLocationService(serviceIntent, ctx);
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    public static void startLocationService(Intent serviceIntent, Context ctx) {
        if (LocationService.shouldStartInForeground() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(serviceIntent);
            Log.d(getLogStart(), "start location service as foreground service");
        } else {
            ctx.startService(serviceIntent);
            Log.d(getLogStart(), "start location service as normal service");
        }
    }

    private void startInForeground() {
        // In newer SDK versions the notification channel may not be needed anymore.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "location_service";
            String channelName = "Location Service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            locationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(locationChannel);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setContentTitle("Location service is running")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSound(null)
                    .build();

            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(getLogStart(), "onStartCommand");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new GpsLocationListener();
        // check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(getLogStart(), "Start sticky");
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
        if (locationChannel != null && notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.deleteNotificationChannel(locationChannel.getId());
            }
        }
    }

    private static String getLogStart() {
        return LocationService.class.getSimpleName();
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
