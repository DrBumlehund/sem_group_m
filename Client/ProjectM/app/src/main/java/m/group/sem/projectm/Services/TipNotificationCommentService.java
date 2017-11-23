package m.group.sem.projectm.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.RemoteInput;
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
public class TipNotificationCommentService extends IntentService {

    private String tag = "TipNotificationAnswerReceiver";

    private SyncHttpClient mHttpClient = null;
    private User mUser = null;

    public TipNotificationCommentService() {
        super(String.valueOf(TipNotificationCommentService.class));
    }

    public TipNotificationCommentService(String name) {
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
        Bundle b = RemoteInput.getResultsFromIntent(intent);
        String comment = b != null ? (String) b.getCharSequence(getString(R.string.i_notification_comment)) : "Comment Unavailable";
        comment = comment.replaceAll(" ", "%20");
        final String url = Constants.getBaseUrl() + String.format("/comments?report-id=%1$s&comment=%2$s&user-id=%3$s", reportId, comment, mUser.getId());
        int i = 0;

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