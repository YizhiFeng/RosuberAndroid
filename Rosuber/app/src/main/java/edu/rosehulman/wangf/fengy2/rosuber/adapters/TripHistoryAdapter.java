package edu.rosehulman.wangf.fengy2.rosuber.adapters;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import edu.rosehulman.wangf.fengy2.rosuber.Constants;
import edu.rosehulman.wangf.fengy2.rosuber.MainActivity;
import edu.rosehulman.wangf.fengy2.rosuber.R;
import edu.rosehulman.wangf.fengy2.rosuber.Trip;
import edu.rosehulman.wangf.fengy2.rosuber.User;
import edu.rosehulman.wangf.fengy2.rosuber.fragments.TripHistoryFragment;

/**
 * Created by wangf on 1/29/2017.
 */

public class TripHistoryAdapter extends RecyclerView.Adapter<TripHistoryAdapter.ViewHolder> {

    private Context mContext;
    private TripHistoryFragment.TripHistoryCallback mCallback;
    private ArrayList<Trip> mTrips = new ArrayList<>();
    private DatabaseReference mTripRef;
    private DatabaseReference mUserRef;
    private User mUser;
    private Activity mActivity;

    public TripHistoryAdapter(Context context, TripHistoryFragment.TripHistoryCallback callback,
                              User user, Activity activity) {
        mContext = context;
        mCallback = callback;
        mUser = user;
        mActivity = activity;

        mTripRef = FirebaseDatabase.getInstance().getReference().child("trips");
        Query driverQuery = mTripRef.orderByChild("driverKey").equalTo(mUser.getKey());
        driverQuery.addChildEventListener(new TripsHistoryChildEventListener());
        Query passengerQuery = mTripRef.orderByChild("passengerKey/" + mUser.getKey()).equalTo(true);
        passengerQuery.addChildEventListener(new TripsHistoryChildEventListener());
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users");

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_history_list_view, parent, false);
        return new TripHistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Trip trip = mTrips.get(position);
        holder.mDestinationTextView.setText(trip.getDestination());
        holder.mTimeTextView.setText(trip.getTime());
        holder.mOriginTextView.setText(trip.getOrigin());
        holder.mPriceTextView.setText(trip.getPrice() + "");

        holder.mDriverTextView.setText("");
        if (trip.getDriverKey() != null) {

            mUserRef.child(trip.getDriverKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    holder.mDriverTextView.setText(user.getName());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        if (trip.getPassengerKey().size() != 0) {
            int count = 0;
            final int size = trip.getPassengerKey().size();
            holder.mPassengersTextView.setText("");
            for (String key : trip.getPassengerKey().keySet()) {

                final int finalCount = count;
                mUserRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if(finalCount == (size-1)){
                            holder.mPassengersTextView.setText(holder.mPassengersTextView.getText().toString() + user.getName());
                        }else{
                            holder.mPassengersTextView.setText(holder.mPassengersTextView.getText().toString() + user.getName() + ", ");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                count++;
            }
        }


        // to be filled
        holder.mInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onContactInfoButtonClicked(trip.getDriverKey(), trip.getPassengerKey());
            }
        });

        holder.mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onEditTripClicked(trip);
            }
        });
    }

    public void editTrip(Trip trip) {
        mTripRef.child(trip.getKey()).setValue(trip);
    }

    @Override
    public int getItemCount() {
        return mTrips.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTimeTextView;
        TextView mDestinationTextView;
        TextView mOriginTextView;
        TextView mDriverTextView;
        TextView mPassengersTextView;
        TextView mPriceTextView;
        ImageView mInfoButton;
        ImageView mEditButton;

        public ViewHolder(View itemView) {
            super(itemView);
            mTimeTextView = (TextView) itemView.findViewById(R.id.time_input_view);
            mDestinationTextView = (TextView) itemView.findViewById(R.id.destination_input_view);
            mOriginTextView = (TextView) itemView.findViewById(R.id.origin_input_view);
            mDriverTextView = (TextView) itemView.findViewById(R.id.driver_input_view);
            mPassengersTextView = (TextView) itemView.findViewById(R.id.passenger_input_view);
            mPriceTextView = (TextView) itemView.findViewById(R.id.price_input_view);
            mInfoButton = (ImageView) itemView.findViewById(R.id.contact_info_image_view);
            mEditButton = (ImageView) itemView.findViewById(R.id.edit_trip_image_view);
        }
    }

    private void sendNotification(Trip trip) {
        Intent intent = new Intent(mContext, mActivity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        Bundle args = new Bundle();
//        args.putString(Constants.TAG, "hi");
//        intent.putExtras(args);
        PendingIntent pendingIntent = PendingIntent.getActivity(mActivity, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String messageBody = "Your trip from " + trip.getOrigin() + " to " + trip.getDestination() + " has been updated";

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("Trip Update")
                .setPriority(Notification.PRIORITY_MAX)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    class TripsHistoryChildEventListener implements ChildEventListener {

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
                    sendNotification(t);
                }
            }

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            int count =0;
            for (Trip trip : mTrips) {
                if (key.equals(trip.getKey())) {
                    mTrips.remove(trip);
                    notifyItemRemoved(count);
                    notifyDataSetChanged();
                    return;
                }
                count++;
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
