package edu.rosehulman.wangf.fengy2.rosuber.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import edu.rosehulman.wangf.fengy2.rosuber.MainActivity;
import edu.rosehulman.wangf.fengy2.rosuber.R;
import edu.rosehulman.wangf.fengy2.rosuber.Trip;
import edu.rosehulman.wangf.fengy2.rosuber.User;

/**
 * Created by wangf on 1/16/2017.
 */

public class TripDetailFragment extends Fragment {

    private static final String ARG_TRIP = "ARG_TRIP";
    private Trip mTrip;
    private OnJoinListener mListener;
    private DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child("users");
    private String passengers = "";

    public TripDetailFragment() {

    }

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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Trip Detail");
        getActivity().findViewById(R.id.fab).setVisibility(View.GONE);
        View view = inflater.inflate(R.layout.fragment_trip_detail, container, false);
        //time
        TextView timeView = (TextView) view.findViewById(R.id.detail_time_input_text_view);
        timeView.setText(mTrip.getTime());
        //origin
        TextView originView = (TextView) view.findViewById(R.id.detail_origin_input_text_view);
        originView.setText(mTrip.getOrigin());
        //destination
        TextView destinationView = (TextView) view.findViewById(R.id.detail_destination_input_text_view);
        destinationView.setText(mTrip.getDestination());
        //driver
        final TextView driverView = (TextView) view.findViewById(R.id.detail_driver_input_text_view);
        if (mTrip.getDriverKey() != null) {
            mUserRef.child(mTrip.getDriverKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    driverView.setText(user.getName());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        //passenger
        final TextView passengerView = (TextView) view.findViewById(R.id.detail_passenger_input_text_view);
        if (mTrip.getPassengerKey().size() != 0) {
            for (String key : mTrip.getPassengerKey().keySet()) {
                mUserRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        User user = dataSnapshot.getValue(User.class);

                        passengers += user.getName() + ",  ";
                        passengers = passengers.substring(0, passengers.length() - 1);
                        passengerView.setText(passengers);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        //price
        TextView priceView = (TextView) view.findViewById(R.id.detail_price_input_text_view);
        priceView.setText(mTrip.getPrice() + "");

        //seats left
        TextView seatView = (TextView) view.findViewById(R.id.detail_capacity_input_text_view);
        long seats = mTrip.getCapacity() - mTrip.getPassengerKey().keySet().size();
        if (seats < 0) {
            seats = 0;
        }
        seatView.setText(seats + "");


        //join button
        Button joinButton = (Button) view.findViewById(R.id.join_trip_buton);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onJoinButtonPressed(mTrip);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnJoinListener) {
            mListener = (OnJoinListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnContactListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnJoinListener {
        void onJoinButtonPressed(Trip trip);
    }
}
