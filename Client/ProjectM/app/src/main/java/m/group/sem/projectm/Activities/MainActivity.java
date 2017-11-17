package m.group.sem.projectm.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import Model.User;
import m.group.sem.projectm.Fragments.ReportsMapFragment;
import m.group.sem.projectm.R;
import m.group.sem.projectm.Utilities;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String tag = "MAIN_ACTIVITY";

    private User mUser;

    // UI variables
    private TextView mUsernameView;
    private TextView mUserIdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUser = (User) getIntent().getSerializableExtra(getString(R.string.i_user));

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        onNavigationItemSelected(navigationView.getMenu().getItem(0));
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
        getMenuInflater().inflate(R.menu.main_activity, menu);
        mUsernameView = findViewById(R.id.nav_user_name);
        mUserIdView = findViewById(R.id.nav_user_id);
        mUsernameView.setText(mUser.getUsername());
        mUserIdView.setText(String.format(String.valueOf(getString(R.string.nav_user_id)), mUser.getId()));
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        if (id == R.id.nav_main) {
            fragment = ReportsMapFragment.newInstance(mUser);
        } else if (id == R.id.nav_create_report) {
            goToCreateReport();
        } else if ((id == R.id.nav_show_leaderboard)) {
            goToLeaderboard();
        } else if (id == R.id.nav_manage) {
            goToSettings();
        } else if (id == R.id.nav_sign_out) {
            signOut();
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void goToSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void goToLeaderboard() {
        Toast.makeText(getApplicationContext(), "Leaderboard, what leaderboard?", Toast.LENGTH_SHORT).show();
    }

    private void goToCreateReport() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        double lastKnownLatitude = Utilities.getDouble(prefs, getString(R.string.last_known_lat), 0);
        double lastKnownLongitude = Utilities.getDouble(prefs, getString(R.string.last_known_long), 0);

        Intent intent = new Intent(MainActivity.this, CreateReportActivity.class);
        intent.putExtra(getString(R.string.i_user), mUser);
        intent.putExtra(getString(R.string.i_location), new double[]{lastKnownLatitude, lastKnownLongitude});
        startActivity(intent);
    }

    private void signOut() {
        // TODO: it would probably be good practice to check if the user is sure, before changing activity.
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra(getString(R.string.i_sign_out), true);
        startActivity(intent);
        finish();
    }
}

