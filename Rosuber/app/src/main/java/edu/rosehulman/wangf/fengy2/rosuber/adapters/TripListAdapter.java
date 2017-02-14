package edu.rosehulman.wangf.fengy2.rosuber.adapters;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
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
import java.util.Calendar;

import edu.rosehulman.wangf.fengy2.rosuber.MyFirebaseMessagingService;
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

    public TripListAdapter(Context context, TripListFragment.TripListCallback callback) {
        mContext = context;
        mCallback = callback;
        mTripRef = FirebaseDatabase.getInstance().getReference().child("trips");
        mListener = new TripsChildEventListner();
        mTripRef.addChildEventListener(mListener);
        mTripRef.keepSynced(true);
    }

    public void toggle(String from, String to) {
        if (from != null) {
            mSearchListener = new SearchTripsChildEventListner(from, to);
        }
        if (from == null) {
            mTripRef.removeEventListener(mSearchListener);
            mTrips.clear();
            mTripRef.addChildEventListener(mListener);
            return;
        }
        mTripRef.removeEventListener(mListener);
        mTrips.clear();
        mTripRef.addChildEventListener(mSearchListener);
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

    private boolean isExpired(String tripTime) {
        Calendar currentTime = Calendar.getInstance();
        int currentYear = currentTime.get(Calendar.YEAR);
        int currentMonth = currentTime.get(Calendar.MONTH)+1;
        int currentDay = currentTime.get(Calendar.DAY_OF_MONTH);
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentTime.get(Calendar.MINUTE);

//        Log.d("Current: ", currentDay + "/" + currentMonth + "/" + currentYear + " " + currentHour + ":" + currentMinute);
//        Log.d("Set: ", tripTime);

        String[] s = tripTime.split(" ");
        String date = s[0];
        String[] ddmmyy = date.split("/");
        int day = Integer.parseInt(ddmmyy[0]);
        int month = Integer.parseInt(ddmmyy[1]);
        int year = Integer.parseInt(ddmmyy[2]);

        String time = s[1];
        String[] hhmm = time.split(":");
        int hour = Integer.parseInt(hhmm[0]);
        int min = Integer.parseInt(hhmm[1]);

        if(year>= currentYear){
            if(month >= currentMonth){
                if(day >= currentDay){
                    return  false;
                }else{
                    return true;
                }
            }else{
                return true;
            }
        }else{
            return  true;
        }
    }


    class TripsChildEventListner implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Trip trip = dataSnapshot.getValue(Trip.class);
            trip.setKey(dataSnapshot.getKey());
            if (!isExpired(trip.getTime())) {
                mTrips.add(0, trip);
            }
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
        String origin;

        public SearchTripsChildEventListner(String origin, String dest) {
            this.origin = origin.toLowerCase();
            this.dest = dest.toLowerCase();
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Trip trip = dataSnapshot.getValue(Trip.class);
            if (!isExpired(trip.getTime())) {
                if (trip.getDestination().toLowerCase().contains(this.dest) || trip.getOrigin().toLowerCase().contains(this.origin)) {
                    trip.setKey(dataSnapshot.getKey());
                    mTrips.add(0, trip);
                    notifyDataSetChanged();
                }
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
