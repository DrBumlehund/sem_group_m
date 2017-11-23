package m.group.sem.projectm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import java.util.Date;

import Model.Report;
import cz.msebera.android.httpclient.Header;
import m.group.sem.projectm.Services.ActivityRecognitionContainer;
import m.group.sem.projectm.Services.TipNotificationAnswerService;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Simon on 08-11-2017.
 */

public class TipNotificationHandler {

    public final static int notificationId = 1;
    private final static String tag = "TipNotificationHandler";
    private static String contribKey = "CONTRIBUTED";
    private static String notificationTimeKey = "TIME";
    private static TipNotificationHandler instance;
    private double radius = -1;
    private long lastReportUpdate = 0;
    private long reportUpdateInterval;
    private long notificationInterval;
    private Report[] reports = new Report[0];
    private Context context;
    private double latitude = 0;
    private double longitude = 0;
    // Async rest calls
    private SyncHttpClient mHttpClient = new SyncHttpClient();
    private ObjectMapper mMapper = new ObjectMapper();

    private TipNotificationHandler() {
        reportUpdateInterval = 2L * 60L * 60L * 1000L; // 2 hours
        notificationInterval = 30L * 24L * 60L * 60L * 1000L; // thirty days in milliseconds
    }

    public static TipNotificationHandler getInstance() {
        if (instance == null)
            instance = new TipNotificationHandler();
        return instance;
    }

    public void ActivityDetected(ActivityRecognitionContainer activityRecognitionContainer, Context context) {
        this.context = context;
        if (radius < 0) {
            SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.sp_key), MODE_PRIVATE);
            radius = preferences.getInt(context.getString(R.string.sp_set_distance), context.getResources().getInteger(R.integer.set_dist_def));
        }
        if (activityRecognitionContainer.isOnFoot()) {
            if (new java.util.Date().getTime() > lastReportUpdate + reportUpdateInterval) {
                getReports();
                lastReportUpdate = new java.util.Date().getTime();
            } else {
                checkReportProximity();
            }
        }
        // TODO REMOVE THIS TEST STUFF
        Report report = new Report();
        report.setId(163327);
        report.setComment("hey there ma dood");
        showNotification(report);
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
                Log.d(tag, String.format("distance to report %d : %s has been calculated to %f meters", report.getId(), report.getComment(), distance));
                if (distance < radius) {
                    showNotification(report);
                }
            }
        }
    }

    //    @TargetApi(Build.VERSION_CODES.O)
    private boolean showNotification(Report report) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.sp_key), MODE_PRIVATE);

        Long timeSinceLastTimeThisReportWasNotified = sp.getLong(String.valueOf(report.getId() + notificationTimeKey), Long.MIN_VALUE);
        Long time = new Date().getTime();

        if (sp.getBoolean(String.valueOf(report.getId() + contribKey), false)) {
            // TODO: Save boolean for user contribution;
            // the user has contributed to the report
            return false;
        }

        if (timeSinceLastTimeThisReportWasNotified != Long.MIN_VALUE) {

            if (timeSinceLastTimeThisReportWasNotified < time) {
                // Notification has been shown,
                // and we don't want to keep showing the same notification to the user
                Log.d(tag, String.format("timeLast: %d - timeNow: %d = %d, condition < %d = %b", timeSinceLastTimeThisReportWasNotified, time, time - timeSinceLastTimeThisReportWasNotified, notificationInterval, time - timeSinceLastTimeThisReportWasNotified < notificationInterval));
                // Unless the notification is older than notificationInterval
                if (time - timeSinceLastTimeThisReportWasNotified < notificationInterval) {
                    // The notification has been notified within the last month
                    return false;
                }
            }

        } else {
            Log.d(tag, "timeSinceLastTimeThisReportWasNotified was equals to Long.MIN_VALUE, report was never shown before");
        }

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = "notification_channel_1";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_name);
            // The user-visible description of the channel.
            String description = context.getString(R.string.notification_channel_desc);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = null;
            mChannel = new NotificationChannel(id, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, id)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setSmallIcon(R.drawable.ic_stat_hvid_uden_tekst)
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(String.format(context.getString(R.string.notification_content_text), report.getComment()));

        // Create Intent for confirm vote
        Intent confirmIntent = new Intent(context, TipNotificationAnswerService.class);
        confirmIntent.putExtra(context.getString(R.string.i_notification_vote), true);
        PendingIntent confirmPendingIntent = PendingIntent.getService(context, notificationId, confirmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.drawable.ic_menu_gallery, context.getString(R.string.notification_confirm), confirmPendingIntent);

        // create Intent for deny vote
        Intent denyIntent = new Intent(context, TipNotificationAnswerService.class);
        denyIntent.putExtra(context.getString(R.string.i_notification_vote), false);
        PendingIntent denyPendingIntent = PendingIntent.getService(context, notificationId, denyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.drawable.ic_menu_camera, context.getString(R.string.notification_deny), denyPendingIntent);

        // create Intent for comment
        Intent comment2Intent = new Intent(context, TipNotificationAnswerService.class);
        PendingIntent comment2PendingIntent = PendingIntent.getService(context, notificationId, comment2Intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
            RemoteInput remoteInput = new RemoteInput.Builder(context.getString(R.string.i_notification_comment))
                    .setLabel(context.getString(R.string.notification_comment))
                    .build();

            Notification.Action action =
                    new Notification.Action.Builder(R.drawable.ic_menu_send, context.getString(R.string.notification_comment), comment2PendingIntent)
                            .addRemoteInput(remoteInput)
                            .build();


//            builder.addAction(action);
        } else {
            // create Intent for comment
            Intent commentIntent = new Intent(context, TipNotificationAnswerService.class);
            commentIntent.putExtra(context.getString(R.string.i_notification_comment), "hek");
            PendingIntent commentPendingIntent = PendingIntent.getService(context, notificationId, commentIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            builder.addAction(R.drawable.ic_menu_send, context.getString(R.string.notification_comment), commentPendingIntent);
        }


        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());

        SharedPreferences.Editor spEditor = sp.edit();

        spEditor.putLong(String.valueOf(report.getId() + notificationTimeKey), time);

        spEditor.apply();
        return true;
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
