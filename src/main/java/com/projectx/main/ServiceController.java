package com.projectx.main;

import com.projectx.dao.ServiceDAO;
import com.projectx.dao.SessionDAO;
import com.projectx.model.Service;
import com.projectx.response.BasicServiceResponse;
import com.projectx.response.FullServiceResponse;
import com.projectx.utils.PhotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static com.projectx.dao.ServiceDAO.SERVICE_NAME_LENGTH;

/**
 * Created by ivan on 13.04.17.
 */
@RestController
public class ServiceController {
    private final SessionDAO sessionDAO;
    private final ServiceDAO serviceDAO;
    private final PasswordEncoder passwordEncoder;


    public ServiceController(SessionDAO sessionDAO, ServiceDAO serviceDAO, PasswordEncoder passwordEncoder) {
        this.sessionDAO = sessionDAO;
        this.serviceDAO = serviceDAO;
        this.passwordEncoder = passwordEncoder;
    }
    


    @RequestMapping(path = "/services", method = RequestMethod.GET)
    public ResponseEntity<?> listServices(@RequestParam(required = false, name = "user") Long userId,
                                          @RequestParam(required = false) String category,
                                          @RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam int limit) {

        if (page == null) {
            page = 1;
        }
        if (page <= 0 || limit <= 0) {
            logger.debug("Failed to get services due to bad request: {}");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong parameters");
        }

        List<Service> services;
        if (userId != null) {
            services = serviceDAO.getServicesForUser(userId, page, limit);
        }
        else {
            services = serviceDAO.getServices(page, limit);
        }

        List<BasicServiceResponse> response = new ArrayList<>();
        for (Service service: services) {
            response.add(new BasicServiceResponse(service));
        }
        return ResponseEntity.ok(response);
    }



    @RequestMapping(path = "/services", method = RequestMethod.POST)
    public ResponseEntity<?> createService(@CookieValue(required = false, name = "session_id") Long sessionId,
                                           @CookieValue(required = false) String token,
                                           @RequestBody ServiceRequest request) throws IOException {

        if (sessionId == null || StringUtils.isEmpty(token)) {
            logger.debug("No cookie is represented");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in to create service");
        }

        String name = request.getName();
        String description = request.getDescription();
        int price = request.getPrice();

        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(description) || price <= 0) {
            logger.debug("Creating new service failed (bad parameters)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad parameters");
        }

        if (name.length() > SERVICE_NAME_LENGTH) {
            logger.debug("Creating new service failed (service name too long)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Service name too long");
        }

        Long userId = sessionDAO.getUser(sessionId, token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in to create new service");
        }


        Service service = new Service(name, description, price, userId);

        List<String> photos = request.getPhotos();
        List<String> fileNames = PhotoService.savePhotosToFiles(photos);
        service.setPhotos(fileNames);

        serviceDAO.addService(service);
        logger.debug("Added new service by userID = {}: {}", userId, service);
        return ResponseEntity.ok(null);
    }









    @RequestMapping(path = "/services/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getServiceInfo(@PathVariable long id) {
        Service service = serviceDAO.getServiceById(id);
        if (service == null) {
            logger.debug("Failed getting info about service with id={} - it doesn't exist", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        logger.debug("Getting info for service {} is successful, info: {}", id, service);
        return ResponseEntity.ok(new FullServiceResponse(service));
    }

    @RequestMapping(path = "/services/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateServiceInfo(@CookieValue(required = false, name = "session_id") Long sessionId,
                                               @CookieValue(required = false) String token,
                                               @PathVariable(name = "id") long serviceId,
                                               @RequestBody ServiceRequest request) {

        if (sessionId == null || StringUtils.isEmpty(token)) {
            logger.debug("No cookie is represented");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in to update service");
        }

        String name = request.getName();
        String description = request.getDescription();
        int price = request.getPrice();

        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(description) || price <= 0) {
            logger.debug("Updating service failed (bad parameters)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad parameters");
        }

        if (name.length() > SERVICE_NAME_LENGTH) {
            logger.debug("Updating service failed (service name too long)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Service name too long");
        }

        Long userId = sessionDAO.getUser(sessionId, token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in to update service");
        }

        Service service = serviceDAO.getServiceById(serviceId);
        if (service == null) {
            logger.debug("Updating service failed because service {} doesn't exist", serviceId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found");
        }

        if (service.getUserId() != userId) {
            logger.debug("Updating service failed because user {} is not author of service with id = {}", userId, serviceId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You have not access to do it");
        }

        service.setName(name);
        service.setDescription(description);
        service.setPrice(price);
        serviceDAO.updateService(service);
        logger.debug("{} was successful updated", service);
        return ResponseEntity.ok(null);
    }


    @RequestMapping(path = "/services/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteService(@CookieValue(required = false, name = "session_id") Long sessionId,
                                           @CookieValue(required = false) String token,
                                           @PathVariable(name = "id") long serviceId) {

        if (sessionId == null || StringUtils.isEmpty(token)) {
            logger.debug("No cookie represent");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in to delete service");
        }

        Long userId = sessionDAO.getUser(sessionId, token);
        if (userId == null) {
            logger.debug("No session found for this token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in to delete service");
        }

        Service service = serviceDAO.getServiceById(serviceId);
        if (service == null) {
            logger.debug("Deleting service failed because service {} doesn't exist", serviceId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found");
        }

        if (service.getUserId() != userId) {
            logger.debug("Deleting service failed because user {} is not author of service with id = {}", userId, serviceId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You have not access to do it");
        }

        serviceDAO.removeService(service.getId());
        logger.debug("{} was successful removed", service);
        return ResponseEntity.ok(null);
    }




    private static class ServiceRequest {
        private String name;
        private String description;
        private int price;
        private List<String> photos = new ArrayList<>();

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

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public List<String> getPhotos() {
            return photos;
        }

        public void setPhotos(List<String> photos) {
            this.photos = photos;
        }
    }


    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class.getSimpleName());
}




