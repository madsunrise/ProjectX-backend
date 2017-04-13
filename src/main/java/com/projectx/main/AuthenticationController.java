package com.projectx.main;

import com.projectx.dao.SessionDAO;
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
    private final SessionDAO sessionDAO;
    private final PasswordEncoder passwordEncoder;


    public AuthenticationController(UserDAO userDAO, PasswordEncoder passwordEncoder, SessionDAO sessionDAO) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.sessionDAO = sessionDAO;
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
                phone.length() != PHONE_LENGTH) {
            logger.debug("Registration failed (parameter length was exceed)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid parameters length");
        }

        final String hashedPassword = passwordEncoder.encode(password);

        User user = new User(name, email, phone, hashedPassword);
        try {
            long id = userDAO.addUser(user);
            user.setId(id);
        }
        catch (DuplicateEntryException ex) {
            logger.debug("User {} already exists!", user);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }


        logger.debug("Creating new user with email \"{}\" is successful", email);
        AuthResponse response = new AuthResponse();
        String time = String.valueOf(System.currentTimeMillis());
        String token = passwordEncoder.encode(time);
        response.setToken(token);
        boolean success = sessionDAO.addSession(token, user.getId());
        if (success) {
            return ResponseEntity.ok(response);
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cannot generate token");
        }
    }



    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        final String login = request.getLogin();
        final String password = request.getPassword();

        if (StringUtils.isEmpty(login)
                || StringUtils.isEmpty(password)) {
            logger.debug("Authorization failed (bad parametres)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not enough parametres");
        }

        final User user = userDAO.getUserByEmail(login);
        if (user == null) {
            logger.debug("Authorization failed because user {} does not exist", login);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            logger.debug("Authorization OK for user with login = {}", login);
            AuthResponse response = new AuthResponse();
            String time = String.valueOf(System.currentTimeMillis());
            String token = passwordEncoder.encode(time);
            response.setToken(token);
            boolean success = sessionDAO.addSession(token, user.getId());
            if (success) {
                return ResponseEntity.ok(response);
            }
            else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cannot generate token");
            }
        }

        logger.debug("Authorization failed - incorrect password for user {}", login);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
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

    private static class LoginRequest {
        private String login;           // Email
        private String password;

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    private static class AuthResponse {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }


    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class.getSimpleName());
}
