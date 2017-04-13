package com.projectx.model;

/**
 * Created by ivan on 13.04.17.
 */
public class Session {
    private long id;
    private String token;
    private long userId;

    public Session() {
    }

    public Session(String token, long userId) {
        this.token = token;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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
