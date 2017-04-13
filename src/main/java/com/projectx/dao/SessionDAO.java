package com.projectx.dao;

import com.projectx.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by ivan on 13.04.17.
 */
@Service
@Transactional
public class SessionDAO {
    private static final Logger logger = LoggerFactory.getLogger(SessionDAO.class.getSimpleName());

    static final String TABLE_NAME = "session";
    private final JdbcTemplate template;

    public SessionDAO(JdbcTemplate template) {
        this.template = template;
    }

    void initTable() {
        final String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + '(' +
                "id MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +        // Up to 16.777.215 rows
                "token CHAR(60) NOT NULL," +
                "user_id MEDIUMINT UNSIGNED NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES " + UserDAO.TABLE_NAME + "(id) " +
                "ON DELETE CASCADE," +
                "UNIQUE (token, user_id))" +
                "DEFAULT CHARSET utf8 DEFAULT COLLATE utf8_general_ci";
        template.execute(createTable);
        logger.debug("Table session was initialized");
    }


    public boolean addSession(String token, long userId) {
        try {
            final String query = "INSERT INTO " + TABLE_NAME +
                    " (token, user_id) VALUES (?,?);";
            template.update(query, token, userId);
            return true;
        }
        catch (DuplicateKeyException ex) {
            return false;
        }
    }


    public Long getUserId(String token)  {
        final String query = "SELECT user_id FROM " + TABLE_NAME + " WHERE token = ?";
        try {
            return template.queryForObject(query, Long.class, token);
        }
        catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }


    private static class SessionPstCreator implements PreparedStatementCreator {
        private final Session session;

        SessionPstCreator(Session session) {
            this.session = session;
        }
        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO " + TABLE_NAME +
                    " (token, user_id) VALUES (?,?);";
            final PreparedStatement pst = con.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, session.getToken());
            pst.setLong(2, session.getUserId());
            return pst;
        }
    }
}


