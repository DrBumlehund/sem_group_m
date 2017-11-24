package m.group.sem.projectm.Services;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.Task;

import m.group.sem.projectm.BroadcastReceivers.LocationBroadcastReceiver;
import m.group.sem.projectm.Constants;
import m.group.sem.projectm.R;
import m.group.sem.projectm.TipDistanceHandler;

public class TipNotificationService extends Service {

    private final static String tag = "TipNotificationService";
    private TipLocationService mService;
    private boolean mBound;
    @SuppressLint("HandlerLeak")
    Handler locationPrecisionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle reply = msg.getData();
            if (mBound) {
                if (reply.getBoolean(Constants.PRECISION)) {
                    if (mService.getPrecision() != LocationRequest.PRIORITY_HIGH_ACCURACY) {
                        Log.d(String.valueOf(this.getClass()), "INCREASING LOCATION PRECISION");
                        mService.changeLocationRequest(LocationRequest.PRIORITY_HIGH_ACCURACY, 0, 0);
                    }
                } else {
                    if (mService.getPrecision() != LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY) {
                        Log.d(String.valueOf(this.getClass()), "DECREASING LOCATION PRECISION TO PRESERVE POWER");
                        mService.changeLocationRequest(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, 0, 0);
                    }
                }
            }
        }
    };
    private final BroadcastReceiver receiver = new LocationBroadcastReceiver() {
        @Override
        protected void onLocationReceived(Intent intent) {
            String action = intent.getAction();
            if (action.equals(getString(R.string.action_location_broadcast))) {
                Log.d(tag, "Received Location from LocationService");
                double lat = intent.getDoubleExtra(getString(R.string.i_latitude), 0);
                double lon = intent.getDoubleExtra(getString(R.string.i_longitude), 0);
                try {
                    TipDistanceHandler.getInstance().setLocation(lat, lon);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!TipDistanceHandler.getInstance().hasHandler()) {
                    TipDistanceHandler.getInstance().setHandler(locationPrecisionHandler);
                    Log.d(tag, "Successfully tried to add MessageHandler to LocationPrecisionHandler");
                } else {
                    Log.e(tag, "Unsuccessfully tried to add MessageHandler to LocationPrecisionHandler");
                }
            }
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBound = true;
            mService = ((TipLocationService.LocalBinder) service).getService();
            if (mService != null) {
                Log.d(tag, "Successfully bound to LocationService");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(String.valueOf(this.getClass()), "disconnected: ");
            mBound = false;
            mService = null;
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent activityRecognitionIntent = new Intent(this, TipNotificationIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, activityRecognitionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(this);
        Task task = activityRecognitionClient.requestActivityUpdates(5000L, pendingIntent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.action_location_broadcast));
        registerReceiver(receiver, new IntentFilter(getString(R.string.action_location_broadcast)));


        Log.d(String.valueOf(this.getClass()), "try to bind: ");
        Intent locationIntent = new Intent(this, TipLocationService.class);
        //startService(locationIntent);
        bindService(locationIntent, mConnection, Context.BIND_AUTO_CREATE);

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        unregisterReceiver(receiver);
        mBound = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public class LocalBinder extends Binder {
        TipNotificationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TipNotificationService.this;
        }
    }
}
