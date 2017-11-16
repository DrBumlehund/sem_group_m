package m.group.sem.projectm.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import m.group.sem.projectm.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewReportFragment extends Fragment {



    public ViewReportFragment() {
        // Required empty public constructor
    }

    public static ViewReportFragment newInstance(String param1, String param2) {
        ViewReportFragment fragment = new ViewReportFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_report, container, false);
    }

}
