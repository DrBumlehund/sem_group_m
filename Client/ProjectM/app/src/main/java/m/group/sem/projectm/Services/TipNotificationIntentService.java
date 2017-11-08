package m.group.sem.projectm.Services;


import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;

import java.io.Serializable;
import java.util.Date;

public class TipNotificationIntentService extends IntentService {

    public TipNotificationIntentService() {
        super("TipNotificationIntentService");
    }

    public TipNotificationIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)){
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            int type = result.getMostProbableActivity().getType();
            int activityConfidence = result.getMostProbableActivity().getConfidence();
            String activityName = getType(type);

            Log.i("Mine", activityName);

            ActivityRecognitionContainer activityRecognitionContainer = new ActivityRecognitionContainer();
            activityRecognitionContainer.type = type;
            activityRecognitionContainer.confidence = activityConfidence;
            activityRecognitionContainer.activityName = activityName;
            activityRecognitionContainer.timestamp = new Date().toString();
        }
    }

    private String getType(int type){
        if(type == DetectedActivity.UNKNOWN)
            return "UNKNOWN";
        else if(type == DetectedActivity.IN_VEHICLE)
            return "IN_VEHICLE";
        else if(type == DetectedActivity.ON_BICYCLE)
            return "ON_BICYCLE";
        else if(type == DetectedActivity.ON_FOOT)
            return "ON_FOOT";
        else if(type == DetectedActivity.STILL)
            return "STILL";
        else if(type == DetectedActivity.TILTING)
            return "TILTING";
        else
            return "";
    }

    private class ActivityRecognitionContainer implements Serializable {
        public int type;
        public int confidence;
        public String activityName;
        public String timestamp;
    }
}
