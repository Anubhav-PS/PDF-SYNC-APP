package com.anubhavps.pdfsync.models;

import android.app.Application;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User extends Application {

    private static User instance;

    private String user_UID;

    private String mailId;

    private String username;

    private String name;

    private String contactNumber;

    private String fcmToken;

    private long avatar;

    private User() {

    }

    public static User getInstance() {
        if (instance == null)
            instance = new User();
        return instance;
    }

    public void setInit(String user_UID,String username){
        this.avatar = 100;
        this.user_UID = user_UID;
        this.username = username;
        this.name = username;
    }

    public void setInstance(User instance) {
        User.instance = instance;
    }


    public String getUser_UID() {
        return user_UID;
    }

    public void setUser_UID(String user_UID) {
        this.user_UID = user_UID;
    }

    public String getMailId() {
        return mailId;
    }

    public void setMailId(String mailId) {
        this.mailId = mailId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public long getAvatar() {
        return avatar;
    }

    public void setAvatar(long avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
