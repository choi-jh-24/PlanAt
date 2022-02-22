package com.example.planat_origin;

public class User {
    private String profile;
    private String email;
    private int strtime;
    private int endtime;
    private String UserName;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getStrtime() {
        return strtime;
    }

    public void setStrtime(int strtime) {
        this.strtime = strtime;
    }

    public int getEndtime() {
        return endtime;
    }

    public void setEndtime(int endtime) {
        this.endtime = endtime;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }
}