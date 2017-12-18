package m.group.sem.projectm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import Model.Report;
import cz.msebera.android.httpclient.Header;
import m.group.sem.projectm.Services.ActivityRecognitionContainer;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Simon on 08-11-2017.
 */

public class TipDistanceHandler {

    private final static String tag = "TipDistanceHandler";
    private static TipDistanceHandler instance;
    private double notificationRadius = -1;
    private double precisionRadius = -1;
    private long lastReportUpdate = 0;
    private long reportUpdateInterval;

    private Report[] reports = new Report[0];
    private Context context;
    // Async rest calls
    private SyncHttpClient mHttpClient = new SyncHttpClient();
    private ObjectMapper mMapper = new ObjectMapper();
    private Handler handler;

    private Location location;

    private TipDistanceHandler() {
        reportUpdateInterval = 2L * 60L * 60L * 1000L; // 2 hours
    }

    public static TipDistanceHandler getInstance() {
        if (instance == null)
            instance = new TipDistanceHandler();
        return instance;
    }

    public void ActivityDetected(ActivityRecognitionContainer activityRecognitionContainer, Context context) {
        this.context = context;
        if (notificationRadius < 0) {
            SharedPreferences preferences = context.getSharedPreferences(getString(R.string.sp_key), MODE_PRIVATE);
            notificationRadius = preferences.getInt(getString(R.string.sp_set_distance), getInteger(R.integer.set_dist_def));
            precisionRadius = preferences.getInt(getString(R.integer.distance_threshold), 150);
        }
        if (activityRecognitionContainer.isOnFoot()) {
            if (new java.util.Date().getTime() > lastReportUpdate + reportUpdateInterval) {
                getReports();
                lastReportUpdate = new java.util.Date().getTime();
            } else {
                checkReportProximity();
            }
        } else {
            Bundle b = new Bundle();
            b.putBoolean(Constants.PRECISION, activityRecognitionContainer.isOnFoot());
            Message msg = new Message();
            msg.setData(b);
            handler.dispatchMessage(msg);
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
                        SharedPreferences prefs = context.getSharedPreferences(getString(R.string.sp_key), MODE_PRIVATE);
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
    }

    private void checkReportProximity() {
        Log.d(tag, "checkReportProximity");

        boolean notificationTriggered = false;
        boolean increasePrecision = false;

        if (reports.length > 0) {
            for (Report report : reports) {
                float distance = meterDistanceToMyLocation(report);
                Log.d(tag, String.format("distance to report %d : %s has been calculated to %f meters, report coordinates = [%f, %f]", report.getId(), report.getComment(), distance, report.getLatitude(), report.getLongitude()));
                if (distance < precisionRadius) {
                    if (!increasePrecision) {
                        // to ensure that it won't change from true to false,
                        // as we have to increase the precision if at least
                        // one report is within the precision range.
                        increasePrecision = true;
                    }
                    if (distance < notificationRadius) {
                        if (!notificationTriggered) {
                            // to ensure that only one notification is fired pr. check
                            // (so that you don't get 10 notifications in 2 seconds if you are within range).
                            notificationTriggered = TipNotificationHandler.getInstance().showNotification(report, context);
                        }
                    }
                }
            }
        }
        if (hasHandler()) {
            Bundle b = new Bundle();
            b.putBoolean(Constants.PRECISION, increasePrecision);
            Message msg = new Message();
            msg.setData(b);
            handler.dispatchMessage(msg);
        }

    }


    public void setLocation(Location location) {
        this.location = location;
        Log.d(tag, String.format("Location updated to : %1$f, %2$f", this.location.getLatitude(), this.location.getLongitude()));
    }

    private float meterDistanceToMyLocation(Report report) {
        float dist;

        // check if we actually have a location, else we need to use the last known location
        if (location != null) {
            Location reportLocation = new Location(report.getComment());
            reportLocation.setLatitude(report.getLatitude());
            reportLocation.setLongitude(report.getLongitude());
            dist = this.location.distanceTo(reportLocation);
        } else {
            // calculate distance to the last known location
            SharedPreferences prefs = context.getSharedPreferences(getString(R.string.sp_key), MODE_PRIVATE);
            double latitude = Utilities.getDouble(prefs, getString(R.string.last_known_lat), 0);
            double longitude = Utilities.getDouble(prefs, getString(R.string.last_known_long), 0);

            double theta = longitude - report.getLongitude();
            dist = (float) (Math.sin(deg2rad(latitude))
                    * Math.sin(deg2rad(report.getLatitude()))
                    + Math.cos(deg2rad(latitude))
                    * Math.cos(deg2rad(report.getLatitude()))
                    * Math.cos(deg2rad(theta)));
            dist = (float) Math.acos(dist);
            dist = (float) rad2deg(dist);
            dist = (float) (dist * 60 * 1.1515);
        }
        return dist;
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private String getString(int id) {
        return context != null ? context.getString(id) : null;
    }

    private int getInteger(int id) {
        return context != null ? context.getResources().getInteger(id) : 0;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public boolean hasHandler() {
        return handler != null;
    }
}
