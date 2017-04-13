package com.projectx.dao;

import com.projectx.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class SessionDAO {
    private static final Logger logger = LoggerFactory.getLogger(SessionDAO.class.getSimpleName());

    static final String TABLE_NAME = "session";
    private final JdbcTemplate template;

    public SessionDAO(JdbcTemplate template) {
        this.template = template;
    }

    void initTable() {
        final String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + '(' +
                "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "token CHAR(60) NOT NULL," +
                "user_id MEDIUMINT UNSIGNED NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES " + UserDAO.TABLE_NAME + "(id) " +
                "ON DELETE CASCADE," +
                "UNIQUE (token, user_id))" +
                "DEFAULT CHARSET utf8 DEFAULT COLLATE utf8_general_ci";
        template.execute(createTable);
        logger.debug("Table session was initialized");
    }


    public long addSession(Session session) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(new SessionPstCreator(session), keyHolder);
        final Map<String, Object> keys = keyHolder.getKeys();
        BigInteger id =  (BigInteger) keys.get("GENERATED_KEY");
        return id.longValue();
    }

    public Session getSession(long sessionId)  {
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        try {
            return template.queryForObject(query, sessionMapper, sessionId);
        }
        catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    private final RowMapper<Session> sessionMapper = (rs, i) -> {
        final Session session = new Session();
        session.setId(rs.getLong("id"));
        session.setToken(rs.getString("token"));
        session.setUserId(rs.getLong("user_id"));
        return session;
    };

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


