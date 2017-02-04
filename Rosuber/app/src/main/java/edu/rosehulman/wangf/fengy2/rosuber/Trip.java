package edu.rosehulman.wangf.fengy2.rosuber;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangf on 1/16/2017.
 */
public class Trip implements Parcelable{

    private String origin;
    private String destination;
    private String driverKey;
    private Map<String, Boolean> passengerKey = new HashMap<String, Boolean>();
    private String time;
    private long price;
    private long capacity;
    private String key;

    protected Trip(Parcel in) {
        origin = in.readString();
        destination = in.readString();
        driverKey = in.readString();
        time = in.readString();
        price = in.readLong();
        capacity = in.readLong();
        key = in.readString();
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

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDriverKey() {
        return driverKey;
    }

    public void setDriverKey(String driverKey) {
        this.driverKey = driverKey;
    }

    public Map<String, Boolean> getPassengerKey() {
        return passengerKey;
    }

    public void setPassengerKey(Map<String, Boolean> passengerKey) {
        this.passengerKey = passengerKey;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void addPassenger(String passenger){
        this.passengerKey.put(passenger,true);
    }

    public Trip(){

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(origin);
        parcel.writeString(destination);
        parcel.writeString(driverKey);
        parcel.writeString(time);
        parcel.writeLong(price);
        parcel.writeLong(capacity);
        parcel.writeString(key);
    }

    public void setValues(Trip trip) {
        this.capacity = trip.capacity;
        this.origin = trip.origin;
        this.destination = trip.destination;
        this.price = trip.price;
        this.driverKey = trip.driverKey;
        this.passengerKey = trip.passengerKey;
        this.time = trip.time;
    }
}
