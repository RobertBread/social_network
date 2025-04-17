package com.example.guiex1.domain;

public class Password {
    private long userID;
    private String password;
    public Password(long userID, String password) {
        this.userID = userID;
        this.password = password;
    }
    public long getUserID() {
        return userID;
    }
    public void setUserID(long userID) {
        this.userID = userID;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

}
