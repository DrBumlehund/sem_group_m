package m.group.sem.projectm;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;

import Model.Report;
import m.group.sem.projectm.Services.ActivityRecognitionContainer;
import m.group.sem.projectm.Services.TipNotificationIntentService;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Simon on 08-11-2017.
 */

public class TipNotificationHandler {

    public final static int notificationId = 1;
    private static TipNotificationHandler instance;
    double latitude;
    double longitude;
    private double radius;
    private long lastReportUpdate = 0;
    // Every 2 hours
    private long reportUpdateInterval = 1000 * 60 * 60 * 2;
    private ArrayList<Report> reports = new ArrayList<>();
    private Context context;

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
            if (lastReportUpdate - new java.util.Date().getTime() < reportUpdateInterval) {
                getReports();
                lastReportUpdate = new java.util.Date().getTime();
            } else {
                checkReportProximity();
            }
        }
    }

    private void getReports() {
        // TODO: Get reports here
        //checkReportProximity(radius);
    }

    private void checkReportProximity() {
        for (Report report : reports) {

            showNotification(report);
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
                        .setContentText("Can you confirm that: {report short description}");

        Intent yepIntent = new Intent(context, TipNotificationIntentService.class);
        yepIntent.putExtra("foo", true);
        yepIntent.putExtra("bar", "more info");
        PendingIntent yepPendingIntent = PendingIntent.getService(context, notificationId, yepIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.addAction(R.drawable.ic_menu_gallery, "Confirm", yepPendingIntent);
        builder.addAction(R.drawable.ic_menu_camera, "Disconfirm", yepPendingIntent);
        builder.addAction(R.drawable.ic_menu_send, "Comment", yepPendingIntent);

        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
