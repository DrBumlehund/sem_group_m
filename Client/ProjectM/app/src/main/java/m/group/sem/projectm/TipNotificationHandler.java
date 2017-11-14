package m.group.sem.projectm;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

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

    public final static int notificationId = 1;
    private final static String tag = "TipNotificationHandler";
    private static TipNotificationHandler instance;
    private double radius = 1000000000;
    private long lastReportUpdate = 0;
    // Every 2 hours
    private long reportUpdateInterval = 1000;
    private Report[] reports = new Report[0];
    private Context context;
    private double latitude = 0;
    private double longitude = 0;
    // Async rest calls
    private SyncHttpClient mHttpClient = new SyncHttpClient();
    private ObjectMapper mMapper = new ObjectMapper();

    private TipNotificationHandler() {

    }

    public static TipNotificationHandler getInstance() {
        if (instance == null)
            instance = new TipNotificationHandler();
        return instance;
    }

    public void ActivityDetected(ActivityRecognitionContainer activityRecognitionContainer, Context context) {
        this.context = context;
        if (activityRecognitionContainer.isOnFoot()) {
            if (new java.util.Date().getTime() > lastReportUpdate + reportUpdateInterval) {
                getReports();
                lastReportUpdate = new java.util.Date().getTime();
            } else {
                checkReportProximity();
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
        // TODO: Get reports here
        //checkReportProximity(radius);
    }

    private void checkReportProximity() {
        Log.d(tag, "checkReportProximity");

        if (reports.length > 0) {
            for (Report report : reports) {
                double distance = meterDistanceToMyLocation(report.getLatitude(), report.getLongitude());
                Log.d(tag, String.format("distance to report %d : %1$s has been calculated to %f meters", report.getId(), report.getComment(), distance));
                if (distance < radius) {
                    showNotification(report);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void showNotification(Report report) {

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = "notification_channel_1";
        // The user-visible name of the channel.
        CharSequence name = context.getString(R.string.notification_channel_name);
        // The user-visible description of the channel.
        String description = context.getString(R.string.notification_channel_desc);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mNotificationManager.createNotificationChannel(mChannel);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, id)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setSmallIcon(R.drawable.ic_stat_hvid_uden_tekst)
                        .setContentTitle("There's an issue in your area")
                        .setContentText(String.format("Can you confirm that: %s", report.getComment()));

        Intent yepIntent = new Intent(context, TipNotificationIntentService.class);
        yepIntent.putExtra("foo", true);
        yepIntent.putExtra("bar", "more info");
        PendingIntent yepPendingIntent = PendingIntent.getService(context, notificationId, yepIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.addAction(R.drawable.ic_menu_gallery, "Confirm", yepPendingIntent);
        builder.addAction(R.drawable.ic_menu_camera, "Deny", yepPendingIntent);
        builder.addAction(R.drawable.ic_menu_send, "Comment", yepPendingIntent);

        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }

    public void setLocation(double latitude, double longitude) {
        Log.d(tag, String.format("Location updated to : %1$f, %2$f", latitude, longitude));
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private double meterDistanceToMyLocation(double lat, double lon) {
        float pk = (float) (180.f / Math.PI);

        double a1 = latitude / pk;
        double a2 = longitude / pk;
        double b1 = lat / pk;
        double b2 = lon / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }
}
