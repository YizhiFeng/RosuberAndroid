package edu.rosehulman.wangf.fengy2.rosuber.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import edu.rosehulman.wangf.fengy2.rosuber.MainActivity;
import edu.rosehulman.wangf.fengy2.rosuber.R;

/**
 * Created by wangf on 1/15/2017.
 */

public class AboutFragment extends Fragment {
    private AboutCallback mCallback;

    public AboutFragment() {
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
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Rosuber");
        getActivity().findViewById(R.id.fab).setVisibility(View.GONE);
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        (view.findViewById(R.id.about_IB)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onEmailToDevelopers();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AboutFragment.AboutCallback) {
            mCallback = (AboutFragment.AboutCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AboutCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public interface AboutCallback {
        void onEmailToDevelopers();
    }
}
