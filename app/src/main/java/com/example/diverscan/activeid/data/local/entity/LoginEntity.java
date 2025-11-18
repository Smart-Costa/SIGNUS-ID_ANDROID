package com.example.diverscan.activeid.data.local.entity;

public class LoginEntity {
    public String userSysId;
    public String username;
    public String email;
    public String password;
    public boolean isApproved;
    public boolean isOnLine;
    public boolean isLockedOut;
    public String Idrol;

    public LoginEntity() { }

    public LoginEntity(String userSysId, String username, String email, String password,
                boolean isApproved, boolean isOnLine, boolean isLockedOut, String Idrol) {
        this.userSysId = userSysId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.isApproved = isApproved;
        this.isOnLine = isOnLine;
        this.isLockedOut = isLockedOut;
        this.Idrol = Idrol;
    }
}
