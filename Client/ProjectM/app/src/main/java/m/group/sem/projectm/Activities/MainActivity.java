package m.group.sem.projectm.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final String tag = "MAIN_ACTIVITY";

    // TODO: remove this dummy data, whenever location data is available.
    private double lat = 55.367397;
    private double lon = 10.430401;

    private User mUser;

    // map variables
    private float zoom;

    // UI variables
    private TextView mUsernameView;
    private TextView mUserIdView;

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
        LatLng pos = new LatLng(lat, lon);
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

    private void goToCreateReport() {
        Intent intent = new Intent(MainActivity.this, CreateReportActivity.class);
        intent.putExtra(getString(R.string.i_user), mUser);
        // TODO: figure out a better way of passing location to CreateReportActivity.
        intent.putExtra(getString(R.string.i_location), new double[]{lat, lon});
        startActivity(intent);
    }

    private void signOut() {
        // TODO: it would probably be good practice to check if the user is sure, before changing activity.
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra(getString(R.string.i_sign_out), true);
        startActivity(intent);
    }
}
