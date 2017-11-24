package m.group.sem.projectm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    public final static int notificationId = 1;
    private final static String tag = "TipDistanceHandler";
    private static TipDistanceHandler instance;
    private double notificationRadius = -1;
    private double precisionRadius = -1;
    private long lastReportUpdate = 0;
    private long reportUpdateInterval;

    private Report[] reports = new Report[0];
    private Context context;
    private double latitude = 0;
    private double longitude = 0;
    // Async rest calls
    private SyncHttpClient mHttpClient = new SyncHttpClient();
    private ObjectMapper mMapper = new ObjectMapper();

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
                        SharedPreferences prefs = context.getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
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
        //checkReportProximity(notificationRadius);
    }

    private void checkReportProximity() {
        Log.d(tag, "checkReportProximity");
        if (reports.length > 0) {
            for (Report report : reports) {
                double distance = meterDistanceToMyLocation(report.getLatitude(), report.getLongitude());
                Log.d(tag, String.format("distance to report %d : %s has been calculated to %f meters", report.getId(), report.getComment(), distance));
                if (distance < notificationRadius) {
                    boolean b = TipNotificationHandler.getInstance().showNotification(report, context);
                }
            }
        }
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

    private String getString(int id) {
        return context != null ? context.getString(id) : null;
    }

    private int getInteger(int id) {
        return context != null ? context.getResources().getInteger(id) : 0;
    }
}
