package com.projectx.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by ivan on 13.04.17.
 */
public class Service {
    private long id;
    private String name;
    private String description;
    private int rating;
    private int price;
    private long userId;
    @JsonProperty("date_created")
    private Date dateCreated = new Date();
    private String photos;

    public Service() {
    }

    public Service(String name, String description, int price, long userId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.userId = userId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getRawPhotos() {
        return this.photos;
    }

    public List<String> getPhotos() {
        String[] split = photos.split("\\s+");
        return Arrays.asList(split);
    }

    public void setRawPhotos(String photos) {
        this.photos = photos;
    }

    public void setPhotos(List<String> fileNames) {
        StringBuilder sb = new StringBuilder();
        for (String fileName: fileNames) {
            sb.append(fileName);
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length() - 1); // Last space
        this.photos = sb.toString();
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", rating=" + rating +
                ", price=" + price +
                ", userId=" + userId +
                ", dateCreated=" + dateCreated +
                '}';
    }
}

