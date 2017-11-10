package m.group.sem.projectm.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by simon on 10-11-2017.
 */

public class TipLocationService extends Service {
    private final IBinder mBinder = new TipLocationBinder();
    private ArrayList<ExampleCallbackInterface> callbackImplementations = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.d("Mine", "bind: ");
        // Simulate that the location service makes a location update
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Call any location update listeners
                onLocationServiceUpdatedOurLocation(123);
                Log.d("Mine", "run: ");
            }
        }, 2000);
        return mBinder;
    }

    public void exampleCallbackImplementation(ExampleCallbackInterface callbackImplementation) {
        callbackImplementations.add(callbackImplementation);
    }

    public void onLocationServiceUpdatedOurLocation (double someVar) {
        for (ExampleCallbackInterface impl : callbackImplementations) {
            impl.newLocationReceived(someVar);
        }
    }

    public class TipLocationBinder extends Binder {
        public TipLocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TipLocationService.this;
        }
    }

    public interface ExampleCallbackInterface {
        void newLocationReceived(double someVar);
    }
}
