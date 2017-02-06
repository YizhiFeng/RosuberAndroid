package edu.rosehulman.wangf.fengy2.rosuber.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

import edu.rosehulman.wangf.fengy2.rosuber.R;
import edu.rosehulman.wangf.fengy2.rosuber.Trip;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.TripListFragment;

/**
 * Created by wangf on 1/16/2017.
 */

public class TripListAdapter extends RecyclerView.Adapter<TripListAdapter.ViewHolder> {

    private ArrayList<Trip> mTrips = new ArrayList<>();
    private Context mContext;
    private TripListFragment.TripListCallback mCallback;
    private DatabaseReference mTripRef;
    private TripsChildEventListner mListener;
    private SearchTripsChildEventListner mSearchListener;
    private Query mQuery;

    public TripListAdapter(Context context, TripListFragment.TripListCallback callback) {
        mContext = context;
        mCallback = callback;
        mTripRef = FirebaseDatabase.getInstance().getReference().child("trips");
        mListener = new TripsChildEventListner();
        mTripRef.addChildEventListener(mListener);
        mTripRef.keepSynced(true);
    }

    public void toggle(String from, String to) {
        mSearchListener = new SearchTripsChildEventListner(to);
        mQuery = mTripRef.orderByChild("origin").equalTo(from);
        if (from == null) {
            mQuery.removeEventListener(mSearchListener);
            mTrips.clear();
            mTripRef.addChildEventListener(mListener);
            return;
        }
        mTripRef.removeEventListener(mListener);
        mTrips.clear();
        mQuery.addChildEventListener(mSearchListener);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_list_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Trip trip = mTrips.get(position);
        holder.mDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onTripSelected(trip);
            }
        });
        holder.mFromToTextView.setText(String.format("From %s To %s", trip.getOrigin(), trip.getDestination()));
        holder.mTimeTextView.setText(String.format("Time: %s", trip.getTime()));
    }


    @Override
    public int getItemCount() {
        return mTrips.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTimeTextView;
        TextView mFromToTextView;
        ImageButton mDetailButton;

        public ViewHolder(View itemView) {
            super(itemView);
            mTimeTextView = (TextView) itemView.findViewById(R.id.trip_time_text_view);
            mFromToTextView = (TextView) itemView.findViewById(R.id.trip_from_to_text_view);
            mDetailButton = (ImageButton) itemView.findViewById(R.id.to_trip_detail_button);
        }
    }


    public void addTrip(Trip trip) {
        mTripRef.push().setValue(trip);
    }

    public void removeTrip(int pos) {
        mTripRef.child(mTrips.get(pos).getKey()).removeValue();
    }

    public void editTrip(Trip trip) {
        mTripRef.child(trip.getKey()).setValue(trip);
    }

    class TripsChildEventListner implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Trip trip = dataSnapshot.getValue(Trip.class);
            trip.setKey(dataSnapshot.getKey());
            mTrips.add(0, trip);
            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String key = dataSnapshot.getKey();
            Trip trip = dataSnapshot.getValue(Trip.class);
            for (Trip t : mTrips) {
                if (key.equals(t.getKey())) {
                    t.setValues(trip);
                    notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            for (Trip trip : mTrips) {
                if (key.equals(trip.getKey())) {
                    mTrips.remove(trip);
                    notifyDataSetChanged();
                    return;
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e("TAG", "DB error " + databaseError);
        }
    }

    class SearchTripsChildEventListner implements ChildEventListener {
        String dest;

        public SearchTripsChildEventListner(String dest) {
            this.dest = dest;
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Trip trip = dataSnapshot.getValue(Trip.class);
            if (trip.getDestination().equals(this.dest)) {
                trip.setKey(dataSnapshot.getKey());
                mTrips.add(0, trip);
                notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String key = dataSnapshot.getKey();
            Trip trip = dataSnapshot.getValue(Trip.class);
            for (Trip t : mTrips) {
                if (key.equals(t.getKey())) {
                    t.setValues(trip);
                    notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            for (Trip trip : mTrips) {
                if (key.equals(trip.getKey())) {
                    mTrips.remove(trip);
                    notifyDataSetChanged();
                    return;
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e("TAG", "DB error " + databaseError);
        }
    }
}
