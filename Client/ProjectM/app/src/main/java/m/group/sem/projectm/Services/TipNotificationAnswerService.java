package m.group.sem.projectm.Services;

import android.app.IntentService;
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
import m.group.sem.projectm.Utilities;

// handles input answers from the notification
public class TipNotificationAnswerService extends IntentService {

    private String tag = "TipNotificationAnswerService";

    private SyncHttpClient mHttpClient = null;
    private User mUser = null;

    public TipNotificationAnswerService() {
        super(String.valueOf(TipNotificationAnswerService.class));
    }

    public TipNotificationAnswerService(String name) {
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
            String serializedUser = sp.getString(getString(R.string.i_user), null);
            try {
                mUser = (User) Utilities.fromString(serializedUser);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        int reportId = intent.getIntExtra(getString(R.string.i_notification_report_id), 0);

        String url;

        if (intent.hasExtra(getString(R.string.i_notification_comment))) {
            // the answer is a comment
            String comment = intent.getStringExtra(getString(R.string.i_notification_comment));
            url = Constants.getBaseUrl() + String.format("/comments?report-id=%1$s&comment=%2$s&user-id=%3$s", reportId, comment, mUser.getId());

        } else {
            // the answer is a vote
            boolean vote = intent.getBooleanExtra(getString(R.string.i_notification_vote), false);
            url = Constants.getBaseUrl() + String.format("/votes?report-id=%1$s&vote=%2$s&user-id=%3$s", reportId, vote, mUser.getId());
        }

        mHttpClient.post(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(tag, "Successfully dispatched notification answer: response : " + new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                Log.e(tag, String.format("Unsuccessfully dispatched notification, error code : %d, response body : %s", statusCode, new String(responseBody)));
            }
        });
    }

}