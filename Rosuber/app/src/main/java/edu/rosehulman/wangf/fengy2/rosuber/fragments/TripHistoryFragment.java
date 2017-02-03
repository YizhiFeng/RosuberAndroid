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

import edu.rosehulman.wangf.fengy2.rosuber.R;
import edu.rosehulman.wangf.fengy2.rosuber.Trip;
import edu.rosehulman.wangf.fengy2.rosuber.TripHistoryAdapter;
import edu.rosehulman.wangf.fengy2.rosuber.TripListAdapter;

/**
 * Created by wangf on 1/29/2017.
 */

public class TripHistoryFragment extends Fragment {

    private TripHistoryAdapter madapter;
    private TripHistoryCallback mCallback;

    public TripHistoryFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("My Trips");
        getActivity().findViewById(R.id.fab).setVisibility(View.GONE);
        RecyclerView view = (RecyclerView)inflater.inflate(R.layout.fragment_trip_list, container, false);
        view.setLayoutManager(new LinearLayoutManager(getContext()));
        TripHistoryAdapter adapter = new TripHistoryAdapter(getContext(),mCallback);
        madapter = adapter;
        view.setAdapter(adapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof TripHistoryCallback) {
//            mCallback = (TripHistoryCallback) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mCallback = null;
    }

    public TripHistoryAdapter getAdapter(){
        return madapter;
    }

    public interface TripHistoryCallback {
        void onHistoryTripSelected(Trip trip);
    }
}