package edu.rosehulman.wangf.fengy2.rosuber.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import edu.rosehulman.wangf.fengy2.rosuber.Constants;
import edu.rosehulman.wangf.fengy2.rosuber.MainActivity;
import edu.rosehulman.wangf.fengy2.rosuber.R;
import edu.rosehulman.wangf.fengy2.rosuber.User;

/**
 * Created by wangf on 1/15/2017.
 */

public class ProfileFragment extends Fragment {

    private DatabaseReference mRef;
    private User mUser;
    private ProfileUpdateListener mListner;
    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://rosuber-android.appspot.com").child("images");

    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = getArguments().getParcelable(Constants.USER);

        mRef = FirebaseDatabase.getInstance().getReference().child("users");
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_map_ok).setVisible(false);
        menu.findItem(R.id.action_map_search).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Profile");
        getActivity().findViewById(R.id.fab).setVisibility(View.GONE);
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView nameTextView = (TextView) rootView.findViewById(R.id.name_input_text_view);
        TextView emailTextView = (TextView) rootView.findViewById(R.id.email_input_text_view);
        TextView phoneTextView = (TextView) rootView.findViewById(R.id.phone_input_text_view);
        final ImageButton editImageButton = (ImageButton) rootView.findViewById(R.id.editImageButton);
        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListner.onEditButtonPressed();
            }
        });
        nameTextView.setText(mUser.getName());
        emailTextView.setText(mUser.getEmail());
        phoneTextView.setText(mUser.getPhoneNumber() + "");

        final ImageButton imageButton = (ImageButton) rootView.findViewById(R.id.profileImageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListner.onImageButtonPressed();
            }
        });
        StorageReference profileImageRef = imageRef.child(mUser.getKey());

        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("profile image", "onSuccess: "+uri.toString());
                Picasso.with(getContext()).load(uri.toString()).into(imageButton);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("image load", "onFailure: ");
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TripListFragment.TripListCallback) {
            mListner = (ProfileUpdateListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnContactListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListner = null;
    }

    public interface ProfileUpdateListener {
        void onImageButtonPressed();
        void onEditButtonPressed();
    }
}
