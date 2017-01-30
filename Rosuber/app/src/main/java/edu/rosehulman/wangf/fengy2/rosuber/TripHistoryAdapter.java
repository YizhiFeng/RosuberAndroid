package edu.rosehulman.wangf.fengy2.rosuber;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import edu.rosehulman.wangf.fengy2.rosuber.fragments.TripHistoryFragment;

/**
 * Created by wangf on 1/29/2017.
 */

public class TripHistoryAdapter extends RecyclerView.Adapter<TripHistoryAdapter.ViewHolder> {

    public TripHistoryAdapter(){

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
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
}
