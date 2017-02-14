package edu.rosehulman.wangf.fengy2.rosuber.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import edu.rosehulman.wangf.fengy2.rosuber.R;
import edu.rosehulman.wangf.fengy2.rosuber.Trip;
import edu.rosehulman.wangf.fengy2.rosuber.adapters.TripListAdapter;

/**
 * Created by wangf on 1/16/2017.
 */

public class TripListFragment extends Fragment {
    private TripListCallback mCallback;
    private TripListAdapter madapter;

    public TripListFragment(){
        setHasOptionsMenu(true);
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
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_search).setVisible(true);
        menu.findItem(R.id.action_map_ok).setVisible(false);
        menu.findItem(R.id.action_map_search).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_search){
            mCallback.onTripSearch();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TripListCallback) {
            mCallback = (TripListCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnContactListener");
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
        void onTripSearch();
    }
}
