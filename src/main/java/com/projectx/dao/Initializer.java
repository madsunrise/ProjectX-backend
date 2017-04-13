package com.projectx.dao;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by ivan on 13.04.17.
 */
@Service
public class Initializer {

    private UserDAO userDAO;

    public Initializer(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @PostConstruct
    public void initAll() {
        userDAO.initTable();
    }
}
