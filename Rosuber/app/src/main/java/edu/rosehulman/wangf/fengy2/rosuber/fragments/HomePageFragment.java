package edu.rosehulman.wangf.fengy2.rosuber.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.rosehulman.wangf.fengy2.rosuber.R;

/**
 * Created by wangf on 1/15/2017.
 */

public class HomePageFragment extends Fragment {

    public HomePageFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_homepage, container, false);
    }
}
