package m.group.sem.projectm.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import m.group.sem.projectm.R;

public class SettingsActivity extends AppCompatActivity {

    // Ui views
    private SeekBar mDistanceSlider;
    private TextView mDistanceText;

    private int mDistanceVal;
    private int mMinDistance;
    private int mMaxDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        toolbar.setTitle(R.string.title_activity_settings);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(getString(R.string.sp_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(getString(R.string.set_dist_val), mDistanceVal);

        editor.apply();

    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = getApplicationContext().getSharedPreferences(getString(R.string.sp_key), MODE_PRIVATE);
        mDistanceVal = preferences.getInt(getString(R.string.set_dist_val), getApplicationContext().getResources().getInteger(R.integer.set_dist_def));
        mMinDistance = getApplicationContext().getResources().getInteger(R.integer.set_dist_min);
        mMaxDistance = getApplicationContext().getResources().getInteger(R.integer.set_dist_max);

        mDistanceSlider = findViewById(R.id.distance_slider);
        mDistanceSlider.setMax(mMaxDistance);

        mDistanceSlider.setProgress(mDistanceVal);

        mDistanceText = findViewById(R.id.distance_slider_val);
        mDistanceText.setText(String.format(getString(R.string.set_dist_val), mDistanceVal));

        mDistanceSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mDistanceVal = i;
                mDistanceText.setText(String.format(getString(R.string.set_dist_val), mDistanceVal));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() < mMinDistance) {
                    seekBar.setProgress(mMinDistance);
                    Toast.makeText(getApplicationContext(), String.format(getString(R.string.set_distance_err), mMinDistance, mMaxDistance), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
