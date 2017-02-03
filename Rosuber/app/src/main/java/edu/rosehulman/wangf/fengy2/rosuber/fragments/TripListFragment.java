package edu.rosehulman.wangf.fengy2.rosuber.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.rosehulman.wangf.fengy2.rosuber.MainActivity;
import edu.rosehulman.wangf.fengy2.rosuber.R;
import edu.rosehulman.wangf.fengy2.rosuber.Trip;
import edu.rosehulman.wangf.fengy2.rosuber.TripListAdapter;

/**
 * Created by wangf on 1/16/2017.
 */

public class TripListFragment extends Fragment {
    private TripListCallback mCallback;
    private TripListAdapter madapter;

    public TripListFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Available Trips");
        getActivity().findViewById(R.id.fab).setVisibility(View.VISIBLE);
        RecyclerView view = (RecyclerView)inflater.inflate(R.layout.fragment_trip_list, container, false);
        view.setLayoutManager(new LinearLayoutManager(getContext()));
        TripListAdapter adapter = new TripListAdapter(getContext(), mCallback);
        madapter = adapter;
        view.setAdapter(adapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TripListCallback) {
            mCallback = (TripListCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public TripListAdapter getAdapter(){
        return madapter;
    }

    public interface TripListCallback {
        void onTripSelected(Trip trip);
    }
}
