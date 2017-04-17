package com.projectx.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.projectx.model.Service;

import java.util.Date;

/**
 * Created by ivan on 17.04.17.
 */
public class BasicServiceResponse {
    private long id;
    private String name;
    private int rating;
    private int price;
    @JsonProperty("date_created")
    private Date dateCreated;

    public BasicServiceResponse() {
    }

    public BasicServiceResponse(Service service) {
        this.id = service.getId();
        this.name = service.getName();
        this.rating = service.getRating();
        this.price = service.getPrice();
        this.dateCreated = service.getDateCreated();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
