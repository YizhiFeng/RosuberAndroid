package edu.rosehulman.wangf.fengy2.rosuber.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.rosehulman.wangf.fengy2.rosuber.Constants;
import edu.rosehulman.wangf.fengy2.rosuber.adapters.MyTripContactAdapter;
import edu.rosehulman.wangf.fengy2.rosuber.R;

public class MyTripContactFragment extends Fragment {

    private OnContactListener mListener;
    private TextView driverPhoneTextView;
    private TextView driverEmailTextView;
    private String mDriverKey;
    private String[] mPassengerKeys;
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

    public MyTripContactFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDriverKey = getArguments().getString(Constants.DRIVER_KEY);
        mPassengerKeys = getArguments().getStringArray(Constants.PASSENGER_KEYS);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_map_ok).setVisible(false);
        menu.findItem(R.id.action_map_search).setVisible(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Contact Info");
        getActivity().findViewById(R.id.fab).setVisibility(View.GONE);

        View rootView = inflater.inflate(R.layout.fragment_my_trip_contact, container, false);

        Log.d(Constants.TAG,mPassengerKeys.toString());
        if(mPassengerKeys!=null||mPassengerKeys.length>0) {
            RecyclerView view = (RecyclerView) rootView.findViewById(R.id.recycler_view);
            view.setLayoutManager(new LinearLayoutManager(getContext()));
            MyTripContactAdapter adapter = new MyTripContactAdapter(getContext(), mListener, mPassengerKeys);
            view.setAdapter(adapter);
        }

        driverPhoneTextView = (TextView) rootView.findViewById(R.id.driver_phone_input_text_view);
        driverEmailTextView = (TextView) rootView.findViewById(R.id.driver_email_input_text_view);
        updateDriverInfo();

        (rootView.findViewById(R.id.driver_phone_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onMessage(driverPhoneTextView.getText().toString());
            }
        });
        (rootView.findViewById(R.id.driver_email_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onEmail(driverEmailTextView.getText().toString(), false);
            }
        });
        return rootView;
    }

    public void updateDriverInfo() {
        userRef.child(mDriverKey + "/phoneNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long phone = dataSnapshot.getValue(Long.class);
                driverPhoneTextView.setText((phone == null) ? "N/A" : (phone + ""));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userRef.child(mDriverKey + "/email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String email = dataSnapshot.getValue(String.class);
                driverEmailTextView.setText((email==null) ? "N/A" : (email));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnContactListener) {
            mListener = (OnContactListener) context;
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

    public interface OnContactListener {
        void onMessage(String receiver);

        void onEmail(String receiver, boolean toDevelopers);
    }
}
