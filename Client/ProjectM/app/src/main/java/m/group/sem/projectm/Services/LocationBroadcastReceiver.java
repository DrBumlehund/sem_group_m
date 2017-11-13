package m.group.sem.projectm.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class LocationBroadcastReceiver extends BroadcastReceiver{

    private final static String tag = "BROADCAST_RECEIVER";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(tag, "onReceive : Location=" +
                intent.getDoubleExtra("projectm.LOCATION_LATITUDE", 0) +", " +
                intent.getDoubleExtra("projectm.LOCATION_LONGITUDE", 0));

        // Override this method and add stuff to do
        Toast.makeText(context,"Location= " + intent.getDoubleExtra("projectm.LOCATION_LATITUDE", 0) +", " +
                intent.getDoubleExtra("projectm.LOCATION_LONGITUDE", 0),Toast.LENGTH_LONG).show();
    }
}
