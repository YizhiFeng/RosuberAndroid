package edu.rosehulman.wangf.fengy2.rosuber;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.w3c.dom.Text;

import java.util.ArrayList;

import edu.rosehulman.wangf.fengy2.rosuber.fragments.TripHistoryFragment;

/**
 * Created by wangf on 1/29/2017.
 */

public class TripHistoryAdapter extends RecyclerView.Adapter<TripHistoryAdapter.ViewHolder> {

    private Context mContext;
    private TripHistoryFragment.TripHistoryCallback mCallback;
    private ArrayList<Trip> mTrips = new ArrayList<>();
    private DatabaseReference mTripRef;

    public TripHistoryAdapter(Context context, TripHistoryFragment.TripHistoryCallback callback){
        mContext = context;
        mCallback = callback;
        mTripRef = FirebaseDatabase.getInstance().getReference().child("trips");
        mTripRef.addChildEventListener(new TripsHistoryChildEventListener());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_history_list_view, parent, false);
        return new TripHistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Trip trip = mTrips.get(position);
        holder.mDestinationTextView.setText(trip.getDestination());
        holder.mTimeTextView.setText(trip.getTime());
        holder.mOriginTextView.setText(trip.getOrigin());
        holder.mDriverTextView.setText(trip.getDriverKey());
        holder.mPriceTextView.setText(trip.getPrice()+"");
        //to be changed
        holder.mPassengersTextView.setText(trip.getPassengerKey());

        // to be filled
        holder.mInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mTrips.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView mTimeTextView;
        TextView mDestinationTextView;
        TextView mOriginTextView;
        TextView mDriverTextView;
        TextView mPassengersTextView;
        TextView mPriceTextView;
        ImageView mInfoButton;

    public ViewHolder(View itemView) {
        super(itemView);
        mTimeTextView = (TextView) itemView.findViewById(R.id.time_input_view);
        mDestinationTextView = (TextView) itemView.findViewById(R.id.destination_input_view);
        mOriginTextView = (TextView) itemView.findViewById(R.id.origin_input_view);
        mDriverTextView = (TextView) itemView.findViewById(R.id.driver_input_view);
        mPassengersTextView = (TextView) itemView.findViewById(R.id.passenger_input_view);
        mPriceTextView = (TextView) itemView.findViewById(R.id.price_input_view);
        mInfoButton = (ImageView) itemView.findViewById(R.id.contact_info_image_view);
    }
}

    class TripsHistoryChildEventListener implements ChildEventListener{

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Trip trip = dataSnapshot.getValue(Trip.class);
            trip.setKey(dataSnapshot.getKey());
            mTrips.add(0,trip);
            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String key = dataSnapshot.getKey();
            Trip trip = dataSnapshot.getValue(Trip.class);
            for(Trip t: mTrips){
                if(key.equals(t.getKey())){
                    t.setValues(trip);
                    notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            for(Trip trip: mTrips){
                if(key.equals(trip.getKey())){
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
            Log.e("TAG", "DB error "+databaseError);
        }
    }
}
