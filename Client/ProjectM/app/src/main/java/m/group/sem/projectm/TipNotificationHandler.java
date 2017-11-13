package m.group.sem.projectm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.health.TimerStat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;

import Model.Report;
import cz.msebera.android.httpclient.Header;
import m.group.sem.projectm.Services.ActivityRecognitionContainer;
import m.group.sem.projectm.Services.TipNotificationIntentService;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Simon on 08-11-2017.
 */

public class TipNotificationHandler {

    private static TipNotificationHandler instance;
    private double radius;
    private long lastReportUpdate = 0;
    // Every 2 hours
    private long reportUpdateInterval = 1000;
    private Report[] reports = new Report[0];
    private Context context;
    public final static int notificationId = 1;

    // Async rest calls
    private SyncHttpClient mHttpClient = new SyncHttpClient();
    private ObjectMapper mMapper = new ObjectMapper();

    public static TipNotificationHandler getInstance() {
        if (instance == null)
            instance = new TipNotificationHandler();
        return instance;
    }

    private TipNotificationHandler () {

    }

    public void ActivityDetected(ActivityRecognitionContainer activityRecognitionContainer, Context context) {
        this.context = context;
        if (activityRecognitionContainer.isOnFoot()) {
            if ( new java.util.Date().getTime() > lastReportUpdate + reportUpdateInterval) {
                getReports();
                lastReportUpdate = new java.util.Date().getTime();
            } else {
                checkNearestReport(radius);
            }
        }
    }

    private void getReports() {
        final String url = Constants.getBaseUrl() + "/reports?only-coordinates=true";

        mHttpClient.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("test", "onSuccess: ");
                try {
                    reports = mMapper.readValue(responseBody, Report[].class);
                    if (context != null) {
                        String reportsSerialized = Utilities.toString(reports);

                        // Broadcast new reports
                        Intent intent = new Intent("projectM.REPORTS_BROADCAST");
                        intent.putExtra(Constants.REPORTS_ONLY_COORDINATES, reportsSerialized);
                        context.sendBroadcast(intent);

                        // Save new reports to shared preferences - these can be read from activities
                        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString(Constants.REPORTS_ONLY_COORDINATES, reportsSerialized);
                        edit.apply();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("test", "onFailure: ");
                // If we don't have any reports and there is a server error, then we retry.
                // If not, then let's just wait for the next report update cycle
                if (statusCode > 500 && reports.length == 0) {
                    getReports();
                }
            }
        });
    }

    private void checkNearestReport(double radius) {
        showNotification();
    }

    private void showNotification () {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setSmallIcon(R.drawable.abc_ic_menu_share_mtrl_alpha)
                        .setContentTitle("There's an issue in your area")
                        .setContentText("Can you confirm that: {report short description}");

        Intent yepIntent = new Intent(context, TipNotificationIntentService.class);
        yepIntent.putExtra("foo", true);
        yepIntent.putExtra("bar", "more info");
        PendingIntent yepPendingIntent = PendingIntent.getService(context, notificationId, yepIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.addAction(R.drawable.ic_menu_gallery, "Confirm", yepPendingIntent);
        builder.addAction(R.drawable.ic_menu_camera, "Disconfirm", yepPendingIntent);
        builder.addAction(R.drawable.ic_menu_send, "Comment", yepPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }
}
