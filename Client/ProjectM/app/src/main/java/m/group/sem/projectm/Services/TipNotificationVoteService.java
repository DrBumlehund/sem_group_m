package m.group.sem.projectm.Services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import java.io.IOException;

import Model.User;
import cz.msebera.android.httpclient.Header;
import m.group.sem.projectm.Constants;
import m.group.sem.projectm.R;

// handles input answers from the notification
public class TipNotificationVoteService extends IntentService {

    private String tag = "TipNotificationAnswerReceiver";

    private SyncHttpClient mHttpClient = null;
    private User mUser = null;

    public TipNotificationVoteService() {
        super(String.valueOf(TipNotificationVoteService.class));
    }

    public TipNotificationVoteService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (mHttpClient == null) {
            mHttpClient = new SyncHttpClient();
        }
        if (mUser == null) {
            ObjectMapper mapper = new ObjectMapper();
            SharedPreferences sp = getSharedPreferences(getString(R.string.sp_key), MODE_PRIVATE);
            String serializedUser = sp.getString(getString(R.string.sp_user_login), null);
            try {
                mUser = mapper.readValue(serializedUser, User.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int reportId = intent.getIntExtra(getString(R.string.i_notification_report_id), 0);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(reportId);

        boolean vote = intent.getBooleanExtra(getString(R.string.i_notification_vote), false);
        final String url = Constants.getBaseUrl() + String.format("/votes?report-id=%1$s&vote=%2$s&user-id=%3$s", reportId, vote, mUser.getId());

        mHttpClient.post(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(tag, "Successfully dispatched notification answer: response : " + new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                Log.e(tag, String.format("Unsuccessfully dispatched notification answer, error code : %d, URL : %s response body : %s", statusCode, url, new String(responseBody)));
            }
        });
    }

}