package edu.rosehulman.wangf.fengy2.rosuber.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import edu.rosehulman.wangf.fengy2.rosuber.Constants;
import edu.rosehulman.wangf.fengy2.rosuber.R;
import edu.rosehulman.wangf.fengy2.rosuber.User;

/**
 * Created by wangf on 1/15/2017.
 */

public class ProfileFragment extends Fragment {

    private DatabaseReference mRef;
    private User mUser;
    private UploadImageListener mListner;
    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://rosuber-android.appspot.com").child("images");
    private String imgaeFilePath = null;

    public ProfileFragment() {
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
        TextView emailTextView = (TextView) rootView.findViewById(R.id.email_input_text_view);
        TextView phoneTextView = (TextView) rootView.findViewById(R.id.phone_input_text_view);
        ImageButton editImageButton = (ImageButton) rootView.findViewById(R.id.editImageButton);
        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        nameTextView.setText(mUser.getName());
        emailTextView.setText(mUser.getEmail());
        phoneTextView.setText(mUser.getPhoneNumber() + "");
        mRef.child(mUser.getKey()).setValue(mUser);

        final ImageButton imageButton = (ImageButton) rootView.findViewById(R.id.profileImageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListner.onImageButtonPressed();
            }
        });
        StorageReference profileImageRef = imageRef.child(mUser.getKey());

        File localFile = null;
        try {
            localFile = File.createTempFile("images", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        final File finalLocalFile = localFile;
        profileImageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                imgaeFilePath = finalLocalFile.getPath();
                Bitmap bmp = BitmapFactory.decodeFile(imgaeFilePath);
                imageButton.setImageBitmap(bmp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TripListFragment.TripListCallback) {
            mListner = (ProfileFragment.UploadImageListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListner = null;
    }

    public interface UploadImageListener {
        void onImageButtonPressed();
    }
}
