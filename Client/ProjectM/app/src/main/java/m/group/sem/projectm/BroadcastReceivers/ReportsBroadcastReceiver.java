package m.group.sem.projectm.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.IOException;

import Model.Report;
import m.group.sem.projectm.Constants;
import m.group.sem.projectm.Utilities;

/**
 * Created by Simon on 13-11-2017.
 */

public abstract class ReportsBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String reportsSerialized = intent.getExtras().getString(Constants.REPORTS_ONLY_COORDINATES);
        Report[] reports = null;
        try {
            reports = (Report[]) Utilities.fromString(reportsSerialized);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        onReportsReceived(reports);
    }

    protected abstract void onReportsReceived(Report[] reports);
}
