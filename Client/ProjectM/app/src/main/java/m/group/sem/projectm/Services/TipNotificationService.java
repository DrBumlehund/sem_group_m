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

import m.group.sem.projectm.R;

public class TipNotificationService extends Service {

    private final BroadcastReceiver receiver = new LocationBroadcastReceiver() {
        @Override
        protected void onLocationReceived(Intent intent) {
            String action = intent.getAction();
            Log.d("HANS", "received broadcast");
            if (action.equals(getString(R.string.action_location_broadcast))) {
                /** TODO: fix this crash issue :D
                 *  I have some oddities, where uncommenting this causes
                 *  the application to crash this we have to find some way of
                 *  getting the location to the TipNotificationHandler
                 *  I have narrowed it down to the singleton call, for the
                 *  causation of the crash
                 */
//                double lat = intent.getDoubleExtra(getString(R.string.i_latitude), 0d);
//                double lon = intent.getDoubleExtra(getString(R.string.i_longitude), 0d);
//                TipNotificationHandler.getInstance().setLocation(lat, lon);
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
        bindService(locationIntent, mConnection, Context.BIND_AUTO_CREATE);

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        mBound = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
