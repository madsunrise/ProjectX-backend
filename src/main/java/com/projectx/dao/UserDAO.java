package com.projectx.dao;

import com.projectx.exception.DuplicateEntryException;
import com.projectx.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Created by ivan on 13.04.17.
 */
@Service
@Transactional
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class.getSimpleName());

    static final String TABLE_NAME = "users";
    private final JdbcTemplate template;

    public static final int NAME_LENGTH = 30;
    public static final int EMAIL_LENGTH = 50;
    public static final int PHONE_LENGTH = 10;

    public UserDAO(JdbcTemplate template) {
        this.template = template;
    }

    void initTable() {
        final String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + '(' +
                "id MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +        // Up to 16.777.215 rows
                "name VARCHAR(" + NAME_LENGTH + ") NOT NULL," +
                "email VARCHAR(" + EMAIL_LENGTH + ") NOT NULL UNIQUE," +
                "phone CHAR(" + PHONE_LENGTH + ") NOT NULL UNIQUE," +
                "password CHAR(60) NOT NULL," +
                "KEY(email))" +
                "DEFAULT CHARSET utf8 DEFAULT COLLATE utf8_general_ci";
        template.execute(createTable);
        logger.info("Table users initialized");
    }


    public long addUser(User user) throws DuplicateEntryException {
        try {
            final KeyHolder keyHolder = new GeneratedKeyHolder();
            template.update(new UserPstCreator(user), keyHolder);
            final Map<String, Object> keys = keyHolder.getKeys();
            BigInteger id =  (BigInteger) keys.get("GENERATED_KEY");
            return id.longValue();
        }
        catch (DuplicateKeyException ex) {
            throw new DuplicateEntryException(ex);
        }
    }



    public User getUserByEmail(String email)  {
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE email = ?;";
        try {
            return template.queryForObject(query, userMapper, email);
        }
        catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public User getUserById(long id)  {
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?;";
        try {
            return template.queryForObject(query, userMapper, id);
        }
        catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    private final RowMapper<User> userMapper = (rs, i) -> {
        final User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPassword(rs.getString("password"));
        return user;
    };


    private static class UserPstCreator implements PreparedStatementCreator {
        private final User user;

        UserPstCreator(User user) {
            this.user = user;
        }
        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO " + TABLE_NAME +
                    " (name, email, phone, password) VALUES (?,?,?,?);";
            final PreparedStatement pst = con.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, user.getName());
            pst.setString(2, user.getEmail());
            pst.setString(3, user.getPhone());
            pst.setString(4, user.getPassword());
            return pst;
        }
    }
}

