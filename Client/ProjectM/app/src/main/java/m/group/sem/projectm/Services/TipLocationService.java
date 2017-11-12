package m.group.sem.projectm.Services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class TipLocationService extends Service {

    private LocationListener locationListener;
    private LocationManager locationManager;
    private final IBinder mBinder = new TipLocationBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("LocationService", "onCreate : Service Created");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // configure intent as appropriate and broadcast
                Log.d("LocationService", "onLocationChanged : New location:" + location.getLatitude() + ", " + location.getLongitude());
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
        };

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("LocationService", "onStartCommand : Service Started");
        //for starters I put it here. Subject to change, could also be done in onBind or onCreate
        listenToLocation();

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.d("LocationService", "onBind: Service Bind");

        return mBinder;
    }

    public class TipLocationBinder extends Binder {
        public TipLocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TipLocationService.this;
        }
    }

    @SuppressLint("NewApi")
    public void listenToLocation(){

        // check for permissions
        if ( checkSelfPermission(android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ) {
            return;
        }
        // this code won't execute IF permissions are not allowed
        locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);

    }
}
