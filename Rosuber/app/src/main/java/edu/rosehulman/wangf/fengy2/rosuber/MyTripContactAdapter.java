package edu.rosehulman.wangf.fengy2.rosuber;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import edu.rosehulman.wangf.fengy2.rosuber.fragments.MyTripContactFragment;

/**
 * Created by fengy2 on 02/04/17.
 */

public class MyTripContactAdapter extends RecyclerView.Adapter<MyTripContactAdapter.ViewHolder> {
    private Context mContext;
    private MyTripContactFragment.OnContactListener mListener;
    private String[] mPassengerKeys;
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");


    public MyTripContactAdapter(Context context, MyTripContactFragment.OnContactListener mListener, String[] passengerKeys) {
        mContext = context;
        this.mListener = mListener;
        this.mPassengerKeys = passengerKeys;
    }

    @Override
    public MyTripContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.passengers_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyTripContactAdapter.ViewHolder holder, int position) {
        final String key = mPassengerKeys[position];
        fetch(holder.phone, holder.email, key);
        holder.messageIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onMessage(holder.phone.getText().toString());
            }
        });
        holder.emailIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onEmail(holder.email.getText().toString());
            }
        });
    }

    public void fetch(final TextView phoneView, final TextView emailView, String key) {
        userRef.child(key + "/phoneNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long newPhone = dataSnapshot.getValue(Long.class);
                phoneView.setText((newPhone == null) ? "N/A" : (newPhone + ""));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userRef.child(key + "/email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String email = dataSnapshot.getValue(String.class);
                emailView.setText(email);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        if (mPassengerKeys == null || mPassengerKeys.length == 0) {
            return 0;
        }
        return mPassengerKeys.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView phone;
        TextView email;
        ImageButton messageIB;
        ImageButton emailIB;

        public ViewHolder(View itemView) {
            super(itemView);
            phone = (TextView) itemView.findViewById(R.id.passenger_phone_input_text_view);
            email = (TextView) itemView.findViewById(R.id.passenger_email_input_text_view);
            messageIB = (ImageButton) itemView.findViewById(R.id.passenger_phone_button);
            emailIB = (ImageButton) itemView.findViewById(R.id.passenger_email_button);
        }
    }
}
