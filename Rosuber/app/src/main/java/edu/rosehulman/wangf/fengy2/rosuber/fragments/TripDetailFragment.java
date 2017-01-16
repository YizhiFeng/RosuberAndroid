package edu.rosehulman.wangf.fengy2.rosuber.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.rosehulman.wangf.fengy2.rosuber.R;
import edu.rosehulman.wangf.fengy2.rosuber.Trip;

/**
 * Created by wangf on 1/16/2017.
 */

public class TripDetailFragment extends Fragment {

    private static final String ARG_TRIP = "ARG_TRIP";
    private Trip mTrip;

    public TripDetailFragment(){

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param trip trip.
     * @return A new instance of fragment TripDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TripDetailFragment newInstance(Trip trip) {
        TripDetailFragment fragment = new TripDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRIP, trip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTrip = getArguments().getParcelable(ARG_TRIP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trip_detail, container, false);
        TextView timeView = (TextView) view.findViewById(R.id.detail_time_input_text_view);
//        titleView.setText();

        TextView bodyView = (TextView) view.findViewById(R.id.detail_origin_input_text_view);
//        bodyView.setText(mTrip.getText());

        return view;
    }
}
