package m.group.sem.projectm.Services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.Task;

public class TipNotificationService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent activityRecognitionIntent = new Intent(this, TipNotificationIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, activityRecognitionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(this);
        Task task = activityRecognitionClient.requestActivityUpdates(5000L, pendingIntent);

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
