package m.group.sem.projectm.Services;

import android.os.Parcelable;

/**
 * Created by simon on 12-11-2017.
 */

public interface LocationCallback extends Parcelable {
    void newLocation (double latitude, double longitude);
}
