package edu.rosehulman.wangf.fengy2.rosuber.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.rosehulman.wangf.fengy2.rosuber.Constants;
import edu.rosehulman.wangf.fengy2.rosuber.R;

/**
 * Created by wangf on 1/15/2017.
 */

public class HomePageFragment extends Fragment {

    private DatabaseReference mRef;

    public HomePageFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String roseFirePath = getArguments().getString(Constants.ROSEFIRE_PATH);
//        String roseFirePath = "";
        Log.d(Constants.TAG,roseFirePath);
        if (roseFirePath == null || roseFirePath.isEmpty()) {
            mRef = FirebaseDatabase.getInstance().getReference();
        } else {
            mRef = FirebaseDatabase.getInstance().getReference().child(roseFirePath);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().findViewById(R.id.fab).setVisibility(View.VISIBLE);
        View rootView = inflater.inflate(R.layout.fragment_homepage, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.homepageText);
//        textView.setText("");
        return rootView;
    }
}
