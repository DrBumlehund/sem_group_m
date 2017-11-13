package m.group.sem.projectm.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.IOException;

import Model.Report;
import Model.User;
import cz.msebera.android.httpclient.Header;
import m.group.sem.projectm.Constants;
import m.group.sem.projectm.R;

public class CreateReportActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String tag = "CREATE_REPORT_ACTIVITY";

    // data
    private User mUser;
    private LatLng mPosition;
    private float mZoom;

    // constraints
    private int mMinDescLength = 1;
    private int mMaxDescLength = 280;

    // ui views
    private View mReportFormView;
    private View mProgressView;
    private TextView mDescription;
    private Button mSubmit;
    private TextView mLatView;
    private TextView mLonView;

    // request related stuff
    private AsyncHttpClient mHttpClient;
    private boolean mRequestRunning;
    private ObjectMapper mMapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_report);

        mUser = (User) getIntent().getSerializableExtra(getString(R.string.i_user));
        double[] pos = getIntent().getDoubleArrayExtra(getString(R.string.i_location));
        mPosition = new LatLng(pos[0], pos[1]);
        mZoom = 18;

        // ui initialization
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.create_r_map);
        mapFragment.getMapAsync(this);

        mReportFormView = findViewById(R.id.create_r_form);
        mProgressView = findViewById(R.id.create_r_progress);
        mDescription = findViewById(R.id.create_r_desc);
        mSubmit = findViewById(R.id.create_r_submit_btn);
        mLatView = findViewById(R.id.create_r_lat);
        mLonView = findViewById(R.id.create_r_lon);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSubmitReport();
            }
        });

        mHttpClient = new AsyncHttpClient();
        mRequestRunning = false;
        mMapper = new ObjectMapper();

    }

    @Override
    public void onMapReady(final GoogleMap map) {
        updateMap(map);
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng newPosition) {
                mPosition = newPosition;
                updateMap(map);
            }
        });
    }

    private void updateMap(GoogleMap map) {
        map.clear();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mPosition, mZoom));
        map.addMarker(new MarkerOptions()
                .draggable(false)
                .position(mPosition)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.arrow))
        );
        map.getUiSettings().setAllGesturesEnabled(false);
        mLatView.setText(String.valueOf(mPosition.latitude));
        mLonView.setText(String.valueOf(mPosition.longitude));
    }

    private void attemptSubmitReport() {
        if (mRequestRunning) {
            return;
        }
        if (!isValidDesc()) {
            mDescription.setError(String.format(String.valueOf(getString(R.string.create_r_desc_size_err)), mMinDescLength, mMaxDescLength));
            return;
        }

        String description = String.valueOf(mDescription.getText()).replaceAll(" ", "%20");

        final String url = Constants.getBaseUrl() + "/reports?latitude=" + mPosition.latitude + "&longitude=" + mPosition.longitude + "&comment=" + description + "&user-id=" + mUser.getId();

        mHttpClient.post(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                mRequestRunning = true;
                showProgress(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(tag, String.format("Request Successful: status code %d received response : %s", statusCode, new String(responseBody)));
                try {
                    Report report = mMapper.readValue(responseBody, Report.class);
                    if (report != null) {
                        continueToMainActivity(mUser);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), getText(R.string.connection_err) + "\nerror code: " + statusCode, Toast.LENGTH_LONG).show();
                Log.e(tag, String.format("Received error response : %s", new String(responseBody)));
                try {
                    String errMessage = mMapper.readTree(responseBody).get("message").asText();
                    Log.e(tag, errMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                mRequestRunning = false;
                showProgress(false);
            }
        });
    }

    private void continueToMainActivity(User user) {
        Intent intent = new Intent(CreateReportActivity.this, MainActivity.class);
        intent.putExtra(getString(R.string.i_user), user);
        startActivity(intent);
    }

    private boolean isValidDesc() {
        return mDescription.getText().length() >= mMinDescLength && mDescription.getText().length() <= mMaxDescLength;
    }

    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mReportFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mReportFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mReportFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mReportFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
