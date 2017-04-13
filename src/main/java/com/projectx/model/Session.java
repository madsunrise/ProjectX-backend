package com.projectx.model;

/**
 * Created by ivan on 13.04.17.
 */
public class Session {
    private String token;
    private long userId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
