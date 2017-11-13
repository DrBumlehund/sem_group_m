package m.group.sem.projectm.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public abstract class LocationBroadcastReceiver extends BroadcastReceiver{

    private final static String tag = "BROADCAST_RECEIVER";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(tag, "onReceive : Location=" +
                intent.getDoubleExtra("projectm.LOCATION_LATITUDE", 0) +", " +
                intent.getDoubleExtra("projectm.LOCATION_LONGITUDE", 0));
        onLocationReceived(intent);
    }

    protected abstract void onLocationReceived(Intent intent);
}
