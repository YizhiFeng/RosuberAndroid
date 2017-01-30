package edu.rosehulman.wangf.fengy2.rosuber;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

/**
 * Created by wangf on 1/17/2017.
 */

public class User implements Parcelable{
    private String key;
    private String name;
    private Long phoneNumber;
    private String email;
//    private ArrayList<String> ownedTripsKeys?

    public User() {
    }

    public User(String key, String name, String email) {
        this.key = key;
        this.name = name;
        this.email = email;
    }

    protected User(Parcel in) {
        key = in.readString();
        name = in.readString();
        phoneNumber = in.readLong();
        email = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(key);
        parcel.writeString(name);
        parcel.writeLong(phoneNumber);
        parcel.writeString(email);
    }
}
