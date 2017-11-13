package m.group.sem.projectm.Activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import Model.User;
import m.group.sem.projectm.R;
import m.group.sem.projectm.Services.LocationBroadcastReceiver;
import m.group.sem.projectm.Services.TipLocationService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final String tag = "MAIN_ACTIVITY";

    private double receivedLatitude;
    private double receivedLongitude;

    private User mUser;

    // map variables
    private float zoom;

    // UI variables
    private TextView mUsernameView;
    private TextView mUserIdView;

    private LocationBroadcastReceiver mReceiver;
    private Intent locationServiceIntent;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //The system calls this to deliver the IBinder returned by the service's onBind() method. Our returns null. What happens now?
            Log.i(tag, "onServiceConnected: Service Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermissions();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCreateReport();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mUser = (User) getIntent().getSerializableExtra(getString(R.string.i_user));

        Log.e(tag, "Received user: " + mUser.toString());
        zoom = 13;

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        mUsernameView = findViewById(R.id.nav_user_name);
        mUserIdView = findViewById(R.id.nav_user_id);

        mUsernameView.setText(mUser.getUsername());
        mUserIdView.setText(String.format(String.valueOf(getString(R.string.nav_user_id)), mUser.getId()));

        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            goToSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_create_report) {
            goToCreateReport();
        } else if ((id == R.id.nav_show_leaderboard)) {
            goToLeaderboard();
        } else if (id == R.id.nav_manage) {
            goToSettings();
        } else if (id == R.id.nav_sign_out) {
            // sign out has been clicked
            signOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng pos = new LatLng(receivedLatitude, receivedLongitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoom));
        map.addMarker(new MarkerOptions()
                .position(pos)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.arrow))
                .draggable(false)
        );

    }

    private void goToSettings() {
        Toast.makeText(getApplicationContext(), "Settings, what settings?", Toast.LENGTH_SHORT).show();
    }

    private void goToLeaderboard() {
        Toast.makeText(getApplicationContext(), "Leaderboard, what leaderboard?", Toast.LENGTH_SHORT).show();
    }


    private void goToCreateReport() {
        Intent intent = new Intent(MainActivity.this, CreateReportActivity.class);
        intent.putExtra(getString(R.string.i_user), mUser);
        // TODO: figure out a better way of passing location to CreateReportActivity.
        intent.putExtra(getString(R.string.i_location), new double[]{receivedLatitude, receivedLongitude});
        startActivity(intent);
    }

    private void signOut() {
        // TODO: it would probably be good practice to check if the user is sure, before changing activity.
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra(getString(R.string.i_sign_out), true);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                Log.i(tag, "onRequestPermissionsResult : Permissions granted");
                receiveLocation();
                break;
            default:
                Log.i(tag, "onRequestPermissionsResult : Permissions request canceled");
                break;
        }
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.INTERNET}
                        , 10);
                return;
            }
            Log.i(tag, "checkPermissions : Permissions not granted");

        }
        //TODO: implement some button on mainactivity to resync/recheck permission to start location service
        receiveLocation();
    }

    private void receiveLocation() {
        Log.i(tag, "receiveLocation : Create Receiver");
        mReceiver = new LocationBroadcastReceiver() {
            @Override
            protected void onLocationReceived(Intent intent) {

                receivedLatitude = intent.getDoubleExtra("projectm.LOCATION_LATITUDE", 0);
                receivedLongitude = intent.getDoubleExtra("projectm.LOCATION_LONGITUDE", 0);
                Log.e(tag, "receiveLocation : " + receivedLatitude + ", " + receivedLongitude);

            }
        };
        Log.i(tag, "receiveLocation : Register Receiver");
        registerReceiver(mReceiver, new IntentFilter("projectM.LOCATION_BROADCAST"));
        locationServiceIntent = new Intent(this, TipLocationService.class);
        Log.i(tag, "receiveLocation: Starting Service");
        startService(locationServiceIntent);
        Log.i(tag, "receiveLocation: Binding service");
        bindService(locationServiceIntent, mConnection, BIND_NOT_FOREGROUND);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, new IntentFilter("projectM.LOCATION_BROADCAST"));
    }
}

