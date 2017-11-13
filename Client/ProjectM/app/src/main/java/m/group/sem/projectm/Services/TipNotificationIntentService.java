package m.group.sem.projectm.Services;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.Date;

import m.group.sem.projectm.TipNotificationHandler;

public class TipNotificationIntentService extends IntentService {

    public TipNotificationIntentService() {
        super("TipNotificationIntentService");
    }

    public TipNotificationIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            int type = result.getMostProbableActivity().getType();
            int activityConfidence = result.getMostProbableActivity().getConfidence();

            Log.i("Mine", getType(type));

            ActivityRecognitionContainer activityRecognitionContainer = new ActivityRecognitionContainer();
            activityRecognitionContainer.type = ActivityRecognitionContainer.ActivityType.values()[type];
            activityRecognitionContainer.confidence = activityConfidence;
            activityRecognitionContainer.timestamp = new Date().toString();

            TipNotificationHandler.getInstance().ActivityDetected(activityRecognitionContainer, this);
        }
    }

    private String getType(int type) {
        if (type == DetectedActivity.UNKNOWN)
            return "UNKNOWN";
        else if (type == DetectedActivity.IN_VEHICLE)
            return "IN_VEHICLE";
        else if (type == DetectedActivity.ON_BICYCLE)
            return "ON_BICYCLE";
        else if (type == DetectedActivity.ON_FOOT)
            return "ON_FOOT";
        else if (type == DetectedActivity.STILL)
            return "STILL";
        else if (type == DetectedActivity.TILTING)
            return "TILTING";
        else
            return "";
    }

}
