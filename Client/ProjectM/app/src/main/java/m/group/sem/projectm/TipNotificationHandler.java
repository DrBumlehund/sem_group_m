package m.group.sem.projectm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.Date;

import Model.Report;
import m.group.sem.projectm.Activities.ViewReportActivity;
import m.group.sem.projectm.Services.TipNotificationCommentService;
import m.group.sem.projectm.Services.TipNotificationVoteService;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by drbum on 24-Nov-17.
 */

public class TipNotificationHandler {

    private final static String tag = "TipNotificationHandler";
    private static final TipNotificationHandler ourInstance = new TipNotificationHandler();
    private static String contribKey = "CONTRIBUTED";
    private static String notificationTimeKey = "TIME";
    private long notificationInterval;

    private TipNotificationHandler() {

//        notificationInterval = 30L * 24L * 60L * 60L * 1000L; // thirty days in milliseconds
        notificationInterval = 20L * 1000L; // thirty days in milliseconds
    }

    static TipNotificationHandler getInstance() {
        return ourInstance;
    }

    protected boolean showNotification(Report report, Context context) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.sp_key), MODE_PRIVATE);

        Long lastTimeThisReportWasNotified = sp.getLong(String.valueOf(report.getId() + notificationTimeKey), Long.MIN_VALUE);
        Long time = new Date().getTime();

        if (sp.getBoolean(String.valueOf(report.getId() + contribKey), false)) {
            // TODO: Save boolean for user contribution;
            // the user has contributed to the report
            return false;
        }

        if (lastTimeThisReportWasNotified != Long.MIN_VALUE) {

            if (lastTimeThisReportWasNotified < time) {
                // Notification has been shown,
                // and we don't want to keep showing the same notification to the user
                Log.d(tag, String.format("timeLast: %d - timeNow: %d = %d, condition < %d = %b", lastTimeThisReportWasNotified, time, time - lastTimeThisReportWasNotified, notificationInterval, time - lastTimeThisReportWasNotified < notificationInterval));
                // Unless the notification is older than notificationInterval
                if (time - lastTimeThisReportWasNotified < notificationInterval) {
                    // The notification has been notified within the last month
                    return false;
                }
            }

        } else {
            Log.d(tag, "lastTimeThisReportWasNotified was equals to Long.MIN_VALUE, report was never shown before");
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
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, id)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setGroup(context.getString(R.string.notification_channel_name))
                        .setSmallIcon(R.drawable.ic_stat_hvid_uden_tekst)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(String.format(context.getString(R.string.notification_content_text), report.getComment()));

        // Create Intent for confirm vote
        Intent confirmIntent = new Intent(context, TipNotificationVoteService.class);
        confirmIntent.putExtra(context.getString(R.string.i_notification_vote), true);
        confirmIntent.setAction(context.getString(R.string.notification_vote));
        confirmIntent.putExtra(context.getString(R.string.i_notification_report_id), report.getId());
        PendingIntent confirmPendingIntent = PendingIntent.getService(context, R.string.notification_confirm, confirmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action actionConfirm =
                new NotificationCompat.Action.Builder(R.drawable.ic_menu_gallery, context.getString(R.string.notification_confirm), confirmPendingIntent)
                        .build();

        builder.addAction(actionConfirm);

        // create Intent for deny vote
        Intent denyIntent = new Intent(context, TipNotificationVoteService.class);
        denyIntent.putExtra(context.getString(R.string.i_notification_vote), false);
        denyIntent.setAction(context.getString(R.string.notification_vote));
        denyIntent.putExtra(context.getString(R.string.i_notification_report_id), report.getId());
        PendingIntent denyPendingIntent = PendingIntent.getService(context, R.string.notification_deny, denyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//        builder.addAction(R.drawable.ic_menu_camera, context.getString(R.string.notification_deny), denyPendingIntent);

        NotificationCompat.Action actionDeny =
                new NotificationCompat.Action.Builder(R.drawable.ic_menu_camera, context.getString(R.string.notification_deny), denyPendingIntent)
                        .build();

        builder.addAction(actionDeny);

        // create Intent for comment
        Intent commentIntent = new Intent(context, TipNotificationCommentService.class);
        commentIntent.putExtra(context.getString(R.string.i_notification_report_id), report.getId());
        commentIntent.putExtra(context.getString(R.string.i_notification_comment_boolean), true);
        commentIntent.setAction(context.getString(R.string.notification_comment));
        PendingIntent commentPendingIntent = PendingIntent.getService(context, R.string.notification_comment, commentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteInput remoteInput = new RemoteInput.Builder(context.getString(R.string.i_notification_comment))
                .setLabel(context.getString(R.string.notification_comment))
                .build();

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.ic_menu_send, context.getString(R.string.notification_comment), commentPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        builder.addAction(action);


        Intent contentIntent = new Intent(context, ViewReportActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(context.getString(R.string.i_report), report);
        contentIntent.putExtras(bundle);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ViewReportActivity.class);
        stackBuilder.addNextIntent(contentIntent);

        PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(contentPendingIntent);

        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // The id = 0 prevents multiple notifications to appear,
        // it will only show one in the drawer at a time,
        // but will still notify whenever a new notification appears
        mNotificationManager.notify(0, builder.build());

        SharedPreferences.Editor spEditor = sp.edit();

        spEditor.putLong(String.valueOf(report.getId() + notificationTimeKey), time);

        spEditor.apply();
        return true;
    }
}
