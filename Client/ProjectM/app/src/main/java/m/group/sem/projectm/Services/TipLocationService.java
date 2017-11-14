package m.group.sem.projectm.Services;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import m.group.sem.projectm.R;
import m.group.sem.projectm.Utilities;

public class TipLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String tag = "LOCATION_SERVICE";
    private final static int SECOND = 1000;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient provider;
    private LocationRequest locationRequest;
    private Intent locationIntent;
    private LocationCallback locationCallback;
    private Location newestLocation;

    //Method to chance precision of the location service
    public void changeLocationRequest(int accuracy, int interval, int fastestInterval) {

        try {
            locationRequest.setInterval(interval);
            locationRequest.setFastestInterval(fastestInterval);
            switch (accuracy) {
                case 0:
                    // No locations will be returned unless a different client has requested location
                    // updates in which case this request will act as a passive listener to those locations.
                    locationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
                    break;
                case 1:
                    // City level accuracy is considered to be about 10km accuracy. Using a coarse accuracy
                    // such as this often consumes less power.
                    locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                    break;
                case 2:
                    // Block level accuracy is considered to be about 100 meter accuracy. Using a coarse
                    // accuracy such as this often consumes less power.
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    break;
                case 3:
                    //This will return the finest location available.
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                default:
                    Log.e(tag, "changeLocationRequest : Incorrect accuracy parameter");
                    break;
            }
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
        //Start updating location with new request
        requestUpdates();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(tag, "onCreate : Service Created");
        try {
            locationIntent = new Intent(getString(R.string.action_location_broadcast));
            provider = LocationServices.getFusedLocationProviderClient(this);

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(SECOND * 10)
                    .setFastestInterval(SECOND * 5);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    newestLocation = locationResult.getLastLocation();

                    locationIntent.putExtra(getString(R.string.i_latitude), newestLocation.getLatitude());
                    locationIntent.putExtra(getString(R.string.i_longitude), newestLocation.getLongitude());

                    // Save to shared prefs - will be used as a last known location in case of activity restart
                    SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit = Utilities.putDouble(edit, getString(R.string.last_known_lat), newestLocation.getLatitude());
                    edit = Utilities.putDouble(edit, getString(R.string.last_known_long), newestLocation.getLongitude());
                    edit.apply();

                    Log.i(tag, "onLocationResult : New location:" +
                            locationIntent.getDoubleExtra(getString(R.string.i_latitude), 0) + ", " +
                            locationIntent.getDoubleExtra(getString(R.string.i_longitude), 0));

                    sendBroadcast(locationIntent);
                }
            };

            googleApiClient.connect();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(tag, "onStartCommand : Service started");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(tag, "onBind : Service bind");
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(tag, "onConnected : Service connected");
        requestUpdates();
    }

    // You can use this method to tell if an activity is in the foreground. Might be a good method
    // to use when we want to manage the battery consumption
    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }

    private void requestUpdates() {
        Log.i(tag, "requestUpdates : Checking permissions");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(tag, "requestUpdates : Permissions not granted");
            return;
        }
        Log.i(tag, "requestUpdates : Permissions granted");
        provider.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(tag, "onConnectionSuspended : Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(tag, "onConnectionFailed : Connection failed");
    }
}
