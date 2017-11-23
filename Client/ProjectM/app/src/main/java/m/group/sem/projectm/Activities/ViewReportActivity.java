package m.group.sem.projectm.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.io.Serializable;

import Model.Report;
import m.group.sem.projectm.Fragments.ViewReportFragment;
import m.group.sem.projectm.R;

public class ViewReportActivity extends AppCompatActivity {

    private Report mReport = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mReport = (Report) savedInstanceState.getSerializable(getString(R.string.i_report));

        ViewReportFragment viewReportFragment = (ViewReportFragment) getSupportFragmentManager().findFragmentById(R.id.view_report_fragment);
        viewReportFragment.setReport(mReport, false);
    }

}
