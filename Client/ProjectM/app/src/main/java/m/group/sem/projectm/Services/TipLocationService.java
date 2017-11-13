package m.group.sem.projectm.Services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
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

public class TipLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String tag = "LOCATION_SERVICE";
    private final static int SECOND = 1000;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient provider;
    private LocationRequest locationRequest;
    private Intent locationIntent;
    private LocationCallback locationCallback;
    private Location newestLocation;


    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(tag , "onCreate : Service Created");
        try {
            locationIntent = new Intent("projectM.LOCATION_BROADCAST");
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

                    locationIntent.putExtra("projectm.LOCATION_LATITUDE", newestLocation.getLatitude());
                    locationIntent.putExtra("projectm.LOCATION_LONGITUDE", newestLocation.getLongitude());

                    Log.i(tag, "onLocationResult : New location:" +
                            locationIntent.getDoubleExtra("projectm.LOCATION_LATITUDE", 0) +", " +
                            locationIntent.getDoubleExtra("projectm.LOCATION_LONGITUDE", 0));

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
        Log.i(tag , "onStartCommand : Service started");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(tag , "onBind : Service bind");
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(tag , "onConnected : Service connected");
        requestUpdates();
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
    public void onConnectionSuspended(int i) {Log.i(tag , "onConnectionSuspended : Connection suspended");}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {Log.i(tag , "onConnectionFailed : Connection failed");}
}
