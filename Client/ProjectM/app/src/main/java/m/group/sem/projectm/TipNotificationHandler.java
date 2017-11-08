package m.group.sem.projectm;

import android.os.health.TimerStat;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;

import Model.Report;
import m.group.sem.projectm.Services.ActivityRecognitionContainer;

/**
 * Created by Simon on 08-11-2017.
 */

public class TipNotificationHandler {

    private static TipNotificationHandler instance;
    private double radius;
    private Timestamp lastReportUpdate;
    // Every 2 hours
    private long reportUpdateInterval = 1000 * 60 * 60 * 2;
    private ArrayList<Report> reports = new ArrayList<>();

    public static TipNotificationHandler getInstance() {
        if (instance == null)
            instance = new TipNotificationHandler();
        return instance;
    }

    private TipNotificationHandler () {

    }

    public void ActivityDetected(ActivityRecognitionContainer activityRecognitionContainer) {
        if (activityRecognitionContainer.isOnFoot()) {
            if (lastReportUpdate.getTime() - new java.util.Date().getTime() > reportUpdateInterval) {
                getReports();
            } else {
                checkNearestReport(radius);
            }
        }
    }

    private void getReports() {
        checkNearestReport(radius);
    }

    private void checkNearestReport(double radius) {

    }
}
