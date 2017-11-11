package m.group.sem.projectm.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import Model.User;
import m.group.sem.projectm.R;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String tag = "MainActivity";

    // TODO: remove this dummy data, whenever location data is available.
    private double lat = 55.367397;
    private double lon = 10.430401;

    // map variables
    private float zoom;


    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mUser = (User) getIntent().getSerializableExtra("user");

        Log.d(tag, "Received user: " + mUser.toString());
        zoom = 13;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng pos = new LatLng(lat, lon);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoom));
        map.addMarker(new MarkerOptions()
                        .position(pos)
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher_foreground))
                        .draggable(false)
        );

    }
}
