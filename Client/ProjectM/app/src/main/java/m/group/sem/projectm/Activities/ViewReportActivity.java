package m.group.sem.projectm.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import Model.Report;
import m.group.sem.projectm.Fragments.ViewReportFragment;
import m.group.sem.projectm.R;

public class ViewReportActivity extends AppCompatActivity {

    private Report mReport = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mReport = (Report) this.getIntent().getExtras().getSerializable(getString(R.string.i_report));

//        Toast.makeText(getApplicationContext(), "received report id : " + mReport.getId(), Toast.LENGTH_SHORT).show(); // Test toast

        ViewReportFragment viewReportFragment = (ViewReportFragment) getSupportFragmentManager().findFragmentById(R.id.view_report_fragment);
        viewReportFragment.setReport(mReport, false);
    }

}
