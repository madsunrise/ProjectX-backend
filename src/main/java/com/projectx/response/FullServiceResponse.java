package com.projectx.response;

import com.projectx.model.Service;

import java.util.List;

/**
 * Created by ivan on 18.04.17.
 */
public class FullServiceResponse extends BasicServiceResponse {
    private String description;
    private long userId;

    public FullServiceResponse() {
    }

    public FullServiceResponse(Service service) {
        super(service);
        this.description = service.getDescription();
        this.userId = service.getUserId();
        this.photos = service.getPhotoFileNames();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }
}
