package com.akshaysadarangani.autometa;

/**
 * Created by Akshay on 8/1/2017.
 */

public class User {
    public String name;
    public String email;
    public String uid;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public User(String name, String email, String uid) {
        this.name = name;
        this.email = email;
        this.uid = uid;
    }
}
