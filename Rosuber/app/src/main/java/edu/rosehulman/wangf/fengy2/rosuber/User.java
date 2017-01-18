package edu.rosehulman.wangf.fengy2.rosuber;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

/**
 * Created by wangf on 1/17/2017.
 */

public class User {
    private String key;
    private String name;
    private String nickname;
    private String phoneNumber;
    private String email;
//    private ArrayList<String> ownedTripsKeys?

    public User(){

    }

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
