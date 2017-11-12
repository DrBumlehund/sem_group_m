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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

import Model.Report;
import Model.User;
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
    private RequestQueue mRequestQueue;
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

        mRequestQueue = Volley.newRequestQueue(this);
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

        final String url = "http://51.254.127.173:8080/api/reports?latitude=" + mPosition.latitude + "&longitude=" + mPosition.longitude + "&comment=" + description + "&user-id=" + mUser.getId();

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mRequestRunning = false;
                showProgress(false);

                Log.i(tag, "Received response: " + response);
                try {
                    Report report = mMapper.readValue(response, Report.class);
                    if (report != null) {
                        Intent intent = new Intent(CreateReportActivity.this, MainActivity.class);
                        intent.putExtra(getString(R.string.i_user), mUser);
                        startActivity(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(tag, "Error in network call with url: " + url);
                mRequestRunning = false;
                showProgress(false);
                error.printStackTrace();
                if (error.networkResponse != null) {
                    Toast.makeText(getApplicationContext(), String.format("Networking error, error code %d", error.networkResponse.statusCode), Toast.LENGTH_LONG).show();
                }
            }
        });
        mRequestRunning = true;
        showProgress(true);
        mRequestQueue.add(request);


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
