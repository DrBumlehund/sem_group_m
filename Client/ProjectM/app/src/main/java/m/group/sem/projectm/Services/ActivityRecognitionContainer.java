package m.group.sem.projectm.Services;

import java.io.Serializable;

import m.group.sem.projectm.BuildConfig;

/**
 * Created by Simon on 08-11-2017.
 */
public class ActivityRecognitionContainer implements Serializable {
    public ActivityType type;
    public int confidence;
    public String timestamp;

    public boolean isOnFoot() {
        // TODO REMOVE STILL, THIS IS ONLY FOR TESTING PURPOSES!
        if (BuildConfig.DEBUG) {
            // Because f**k activity recognition when we are testing.
            return true;
        }
        return type == ActivityType.ON_FOOT || type == ActivityType.RUNNING || type == ActivityType.WALKING;
    }

    public enum ActivityType {
        IN_VEHICLE,
        ON_BICYCLE,
        ON_FOOT,
        RUNNING,
        STILL,
        TILTING,
        UNKNOWN,
        WALKING
    }
}
