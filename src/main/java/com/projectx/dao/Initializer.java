package com.projectx.dao;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by ivan on 13.04.17.
 */
@Service
public class Initializer {

    private final UserDAO userDAO;
    private final ServiceDAO serviceDAO;
    private final SessionDAO sessionDAO;

    public Initializer(UserDAO userDAO, ServiceDAO serviceDAO, SessionDAO sessionDAO) {
        this.userDAO = userDAO;
        this.serviceDAO = serviceDAO;
        this.sessionDAO = sessionDAO;
    }

    @PostConstruct
    public void initAll() {
        userDAO.initTable();
        serviceDAO.initTable();
        sessionDAO.initTable();
    }
}
