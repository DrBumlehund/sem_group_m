package m.group.sem.projectm.Services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.Task;

import m.group.sem.projectm.BroadcastReceivers.LocationBroadcastReceiver;
import m.group.sem.projectm.R;
import m.group.sem.projectm.TipNotificationHandler;

public class TipNotificationService extends Service {

    private final BroadcastReceiver receiver = new LocationBroadcastReceiver() {
        @Override
        protected void onLocationReceived(Intent intent) {
            String action = intent.getAction();
            if (action.equals(getString(R.string.action_location_broadcast))) {
                double lat = intent.getDoubleExtra(getString(R.string.i_latitude), 0);
                double lon = intent.getDoubleExtra(getString(R.string.i_longitude), 0);
                try {
                    TipNotificationHandler.getInstance().setLocation(lat, lon);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private TipLocationService mService;
    private boolean mBound;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("Mine", "connected: ");

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("Mine", "disconnected: ");
            mBound = false;
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


        Log.d("Mine", "try to bind: ");
        Intent locationIntent = new Intent(this, TipLocationService.class);
        // startService(locationIntent);
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
