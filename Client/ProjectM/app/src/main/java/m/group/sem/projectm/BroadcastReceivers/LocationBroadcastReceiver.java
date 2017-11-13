package m.group.sem.projectm.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class LocationBroadcastReceiver extends BroadcastReceiver {

    private final static String tag = "BROADCAST_RECEIVER";


    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.e(tag, "onReceive : Location=" +
//                intent.getDoubleExtra(context.getString(R.string.i_latitude), 0) + ", " +
//                intent.getDoubleExtra(context.getString(R.string.i_longitude), 0));
        onLocationReceived(intent);
    }

    protected abstract void onLocationReceived(Intent intent);
}
