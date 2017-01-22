package edu.rosehulman.wangf.fengy2.rosuber;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import edu.rosehulman.wangf.fengy2.rosuber.fragments.TripListFragment;

/**
 * Created by wangf on 1/16/2017.
 */

public class TripListAdapter extends RecyclerView.Adapter<TripListAdapter.ViewHolder> {

    private ArrayList<Trip> mTrips = new ArrayList<>();
    private Context mContext;
    private TripListFragment.TripListCallback mCallback;

    public TripListAdapter(Context context, TripListFragment.TripListCallback callback){
        mContext = context;
        mCallback = callback;
        mTrips.add(new Trip());
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
    }


    @Override
    public int getItemCount() {
        return mTrips.size();
    }



    class ViewHolder extends RecyclerView.ViewHolder{
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


    public void addTrip(Trip trip){
        mTrips.add(trip);
        notifyDataSetChanged();
    }

    public void removeTrip(int pos){
        mTrips.remove(pos);
        notifyDataSetChanged();
    }
}
