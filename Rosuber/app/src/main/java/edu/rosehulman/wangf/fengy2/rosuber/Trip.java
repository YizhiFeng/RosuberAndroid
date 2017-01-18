package edu.rosehulman.wangf.fengy2.rosuber;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by wangf on 1/16/2017.
 */
public class Trip implements Parcelable{

    private String origin;
    private String destination;
    private String driverKey;
//    private ArrayList? passengerKeys
    private String time;
    private long price;
    private long capacity;

    public Trip(){

    }

    protected Trip(Parcel in) {
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
