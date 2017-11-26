package m.group.sem.projectm.Fragments;


import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;

import Model.Report;
import Model.User;
import m.group.sem.projectm.Activities.CreateReportActivity;
import m.group.sem.projectm.BroadcastReceivers.LocationBroadcastReceiver;
import m.group.sem.projectm.BroadcastReceivers.ReportsBroadcastReceiver;
import m.group.sem.projectm.Constants;
import m.group.sem.projectm.R;
import m.group.sem.projectm.Utilities;

import static android.content.Context.MODE_PRIVATE;

public class ReportsMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String tag = "REPORTS_MAP_FRAGMENT";
    private static final String USER_PARAM = "user";

    // map variables
    private float zoom = 15f;
    private Marker currentLocationMarker;
    private HashMap<Marker, Report> reportMarkers = new HashMap<>();
    private GoogleMap mMap = null;

    private double receivedLatitude;
    private double receivedLongitude;
    private Report[] mReports = new Report[0];
    private User mUser;
    private BottomSheetBehavior<View> mBottomSheetBehavior;
    private ViewReportFragment mViewReportFragment;
    private ReportsBroadcastReceiver mReportsReceiver = new ReportsBroadcastReceiver() {
        @Override
        protected void onReportsReceived(Report[] reports) {
            mReports = reports;
            updateMapReports();
        }
    };
    private LocationBroadcastReceiver mReceiver = new LocationBroadcastReceiver() {
        @Override
        protected void onLocationReceived(Intent intent) {
            receivedLatitude = intent.getDoubleExtra(getString(R.string.i_latitude), 0);
            receivedLongitude = intent.getDoubleExtra(getString(R.string.i_longitude), 0);
            Log.d(tag, "receivedLocation : " + receivedLatitude + ", " + receivedLongitude);
            updateMapLocation();
        }
    };

    public ReportsMapFragment() {
    }

    public static ReportsMapFragment newInstance(User user) {
        ReportsMapFragment fragment = new ReportsMapFragment();
        Bundle args = new Bundle();
        args.putSerializable(USER_PARAM, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = (User) getArguments().getSerializable(USER_PARAM);
        }


        checkPermissions();

        // Get saved reports, if any
        SharedPreferences prefs = getContext().getSharedPreferences(getString(R.string.sp_key), MODE_PRIVATE);
        String reportsSerialized = prefs.getString(Constants.REPORTS_ONLY_COORDINATES, null);
        receivedLatitude = Utilities.getDouble(prefs,getString(R.string.last_known_lat), 0);
        receivedLongitude = Utilities.getDouble(prefs,getString(R.string.last_known_long), 0);

        if (reportsSerialized != null && !reportsSerialized.isEmpty()) {
            try {
                mReports = (Report[])Utilities.fromString(reportsSerialized);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mViewReportFragment = (ViewReportFragment) getChildFragmentManager().findFragmentById(R.id.view_report);
        mapFragment.getMapAsync(this);


        final FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCreateReport();
            }
        });

        // Setup the bottom sheet
        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset < 0) {
                    slideOffset = 0;
                }
                fab.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return onMarkerClicked(marker);
            }
        });
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                mapInteraction();
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapInteraction();
            }
        });

        LatLng pos = new LatLng(receivedLatitude, receivedLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoom));
        updateMapLocation();
        updateMapReports();
    }

    private void mapInteraction() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private boolean onMarkerClicked(Marker marker) {
        Object tag = marker.getTag();
        if (!(tag instanceof Integer)){
            // This marker does not have an integer in its tag. Probably not a report then.
            return false;
        }
        try {
            int reportId = (int) tag;
            Report report = null;
            for (Report _report : mReports) {
                if (_report.getId() == reportId){
                    report = _report;
                    break;
                }
            }
            if (report == null) {
                throw new Exception("No report found with id " + reportId);
            }

            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            mViewReportFragment.setReport(report, false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void updateMapLocation() {
        if (mMap != null) {
            LatLng pos = new LatLng(receivedLatitude, receivedLongitude);

            if (currentLocationMarker != null) {
                currentLocationMarker.remove();
            }
            currentLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.arrow))
                    .draggable(false)
            );
        }
    }

    private void updateMapReports() {
        if (mMap != null) {
            for (Marker marker : reportMarkers.keySet()) {
                marker.remove();
            }
            reportMarkers.clear();
            for (Report report : mReports) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(report.getLatitude(), report.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.maps_marker_x30))
                        .title(report.getComment())
                );
                marker.setTag(report.getId());
                reportMarkers.put(marker, report);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                Log.i(tag, "onRequestPermissionsResult : Permissions granted");
                break;
            default:
                Log.i(tag, "onRequestPermissionsResult : Permissions request canceled");
                break;
        }
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
    }

    private void goToCreateReport() {
        Intent intent = new Intent(getContext(), CreateReportActivity.class);
        intent.putExtra(getString(R.string.i_user), mUser);
        // TODO: figure out a better way of passing location to CreateReportActivity.
        intent.putExtra(getString(R.string.i_location), new double[]{receivedLatitude, receivedLongitude});
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(mReceiver);
        getContext().unregisterReceiver(mReportsReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getContext().registerReceiver(mReceiver, new IntentFilter(getString(R.string.action_location_broadcast)));
        // Register for new reports
        getContext().registerReceiver(mReportsReceiver, new IntentFilter("projectM.REPORTS_BROADCAST"));
    }


}
