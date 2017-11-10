package m.group.sem.projectm.Services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.Task;

public class TipNotificationService extends Service {

    private TipLocationService mService;
    private boolean mBound;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent activityRecognitionIntent = new Intent(this, TipNotificationIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, activityRecognitionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(this);
        Task task = activityRecognitionClient.requestActivityUpdates(5000L, pendingIntent);

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

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("Mine", "connected: ");

            try {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                Log.d("Mine", "onServiceConnected: " + service.getClass());
                TipLocationService.TipLocationBinder binder = (TipLocationService.TipLocationBinder) service;
                mService = binder.getService();
                mBound = true;

                mService.exampleCallbackImplementation(new TipLocationService.ExampleCallbackInterface() {
                    @Override
                    public void newLocationReceived(double someeVar) {
                        Log.d("Mine", "newLocationReceived: " + someeVar);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Mine", "onServiceConnected: " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("Mine", "disconnected: ");
            mBound = false;
        }
    };
}
