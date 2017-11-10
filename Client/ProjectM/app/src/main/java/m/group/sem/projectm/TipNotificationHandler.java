package m.group.sem.projectm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.health.TimerStat;
import android.support.v4.app.NotificationCompat;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;

import Model.Report;
import m.group.sem.projectm.Services.ActivityRecognitionContainer;
import m.group.sem.projectm.Services.TipNotificationIntentService;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Simon on 08-11-2017.
 */

public class TipNotificationHandler {

    private static TipNotificationHandler instance;
    private double radius;
    private long lastReportUpdate = 0;
    // Every 2 hours
    private long reportUpdateInterval = 1000 * 60 * 60 * 2;
    private ArrayList<Report> reports = new ArrayList<>();
    private Context context;
    public final static int notificationId = 1;

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
            if (lastReportUpdate - new java.util.Date().getTime() < reportUpdateInterval) {
                getReports();
                lastReportUpdate = new java.util.Date().getTime();
            } else {
                checkNearestReport(radius);
            }
        }
    }

    private void getReports() {
        // TODO: Get reports here
        //checkNearestReport(radius);
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
