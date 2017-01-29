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
import edu.rosehulman.wangf.fengy2.rosuber.User;

/**
 * Created by wangf on 1/15/2017.
 */

public class ProfileFragment extends Fragment {

    private DatabaseReference mRef;
    private User mUser;

    public ProfileFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        String roseFirePath = getArguments().getString(Constants.ROSEFIRE_PATH);
//        String roseFirePath = "";
        mUser = getArguments().getParcelable(Constants.USER);
//        Log.d(Constants.TAG,roseFirePath);
//        if (roseFirePath == null || roseFirePath.isEmpty()) {
//            mRef = FirebaseDatabase.getInstance().getReference();
//        } else {
//            mRef = FirebaseDatabase.getInstance().getReference().child(roseFirePath);
//        }
        mRef = FirebaseDatabase.getInstance().getReference().child("users");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().findViewById(R.id.fab).setVisibility(View.GONE);
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView nameTextView = (TextView) rootView.findViewById(R.id.name_input_text_view);
        TextView emailTextView = (TextView)rootView.findViewById(R.id.email_input_text_view);
        nameTextView.setText(mUser.getName());
        emailTextView.setText(mUser.getEmail());
        mRef.child(mUser.getKey()).setValue(mUser);
        return rootView;
    }
}
