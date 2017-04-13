package com.projectx.main;

import com.projectx.dao.UserDAO;
import com.projectx.exception.DuplicateEntryException;
import com.projectx.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static com.projectx.dao.UserDAO.EMAIL_LENGTH;
import static com.projectx.dao.UserDAO.NAME_LENGTH;
import static com.projectx.dao.UserDAO.PHONE_LENGTH;

/**
 * Created by ivan on 13.04.17.
 */

@RestController
@RequestMapping(path = "/v1")
public class AuthenticationController {
    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class.getSimpleName());

    public AuthenticationController(UserDAO userDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
    }



    @RequestMapping(path = "/signup", method = RequestMethod.POST)
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        final String name = request.getName();
        final String email = request.getEmail();
        final String phone = request.getPhone();
        final String password = request.getPassword();


        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(email)
                || StringUtils.isEmpty(password) || StringUtils.isEmpty(phone)) {
            logger.debug("Registration failed (not enough parametres)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not enough parametres");
        }

        if (name.length() > NAME_LENGTH || email.length() > EMAIL_LENGTH ||
                phone.length() > PHONE_LENGTH) {
            logger.debug("Registration failed (parameter length was exceed)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Parametr length too long");
        }

        final String hashedPassword = passwordEncoder.encode(password);

        User user = new User(name, email, phone, hashedPassword);
        try {
            userDAO.addUser(user);
        }
        catch (DuplicateEntryException ex) {
            logger.debug("User {} already exists!", user);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }


        logger.debug("Creating new user with email \"{}\" is successful", email);
        return ResponseEntity.ok(null);
    }

    private static class SignupRequest {
        private String name;
        private String email;
        private String phone;
        private String password;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
