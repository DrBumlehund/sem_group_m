package m.group.sem.projectm.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by simon on 10-11-2017.
 */

public class TipLocationService extends Service {
    private final IBinder mBinder = new TipLocationBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class TipLocationBinder extends Binder {
        TipLocationBinder getService() {
            // Return this instance of LocalService so clients can call public methods
            return TipLocationBinder.this;
        }
    }
}
