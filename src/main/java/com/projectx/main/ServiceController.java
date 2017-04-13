package com.projectx.main;

import com.projectx.dao.ServiceDAO;
import com.projectx.dao.SessionDAO;
import com.projectx.dao.UserDAO;
import com.projectx.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.projectx.dao.ServiceDAO.SERVICE_NAME_LENGTH;

/**
 * Created by ivan on 13.04.17.
 */
@RestController
public class ServiceController {
    private final UserDAO userDAO;
    private final SessionDAO sessionDAO;
    private final ServiceDAO serviceDAO;


    public ServiceController(UserDAO userDAO, SessionDAO sessionDAO, ServiceDAO serviceDAO) {
        this.userDAO = userDAO;
        this.sessionDAO = sessionDAO;
        this.serviceDAO = serviceDAO;
    }

    @RequestMapping(path = "/fill_random", method = RequestMethod.GET)
    public ResponseEntity<?> fillRandomRecords(@RequestParam long count) throws Exception {
        for (int i = 1; i <= count; ++i) {
            Service service = new Service();
            service.setPrice(i * 1000);
            service.setRating((i * 39) % 50);
            service.setDescription("Some description for service #" + i);
            service.setName("Service #" + i);
            service.setUserId(3L);
            serviceDAO.addService(service);
        }
        return ResponseEntity.ok("OK");
    }


    @RequestMapping(path = "/services", method = RequestMethod.GET)
    public ResponseEntity<?> listServices(@RequestParam(required = false) String category,
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

        List<Service> services = serviceDAO.getServices(page, limit);
        return ResponseEntity.ok(services);
    }


    @RequestMapping(path = "/services", method = RequestMethod.POST)
    public ResponseEntity<?> createService(@RequestBody ServiceRequest request) {
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

        Long userId = sessionDAO.getUserId("$2a$10$8FkXscJrko56Y4r7CWVoOe3XFHJx9lccEZuzRL7WJCkN0f127Im/.");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in to create new service");
        }

        Service service = new Service(name, description, price, userId);
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

        ServiceResponse response = new ServiceResponse(service);
        logger.debug("Getting info for service {} is successful, info: {}", id, response);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(path = "/services/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateServiceInfo(@PathVariable(name = "id") long serviceId, @RequestBody ServiceRequest request) {
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

        Long userId = sessionDAO.getUserId("$2a$10$8FkXscJrko56Y4r7CWVoOe3XFHJx9lccEZuzRL7WJCkN0f127Im/.");
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
    public ResponseEntity<?> deleteService(@PathVariable(name = "id") long serviceId) {

        Long userId = sessionDAO.getUserId("$2a$10$8FkXscJrko56Y4r7CWVoOe3XFHJx9lccEZuzRL7WJCkN0f127Im/.");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in to update service");
        }

        Service service = serviceDAO.getServiceById(serviceId);
        if (service == null) {
            logger.debug("Deleted service failed because service {} doesn't exist", serviceId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found");
        }

        if (service.getUserId() != userId) {
            logger.debug("Updating service failed because user {} is not author of service with id = {}", userId, serviceId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You have not access to do it");
        }

        serviceDAO.removeService(service.getId());
        logger.debug("{} was successful removed", service);
        return ResponseEntity.ok(null);
    }





        private static class ServiceRequest {
        protected String name;
        protected String description;
        protected int price;

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
    }

    private static class ServiceResponse extends ServiceRequest {
        private int rating;
        private long id;

        public ServiceResponse() {
        }

        public ServiceResponse(Service service) {
            this.id = service.getId();
            this.name = service.getName();
            this.description = service.getDescription();
            this.price = service.getPrice();
            this.rating = service.getRating();
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "ServiceResponse{" +
                    "id=" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", price=" + price + '\'' +
                    ", rating=" + rating +
                    '}';
        }
    }


    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class.getSimpleName());
}




