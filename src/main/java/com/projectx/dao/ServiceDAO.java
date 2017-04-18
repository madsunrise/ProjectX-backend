package com.projectx.dao;

import com.projectx.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by ivan on 13.04.17.
 */
@org.springframework.stereotype.Service
@Transactional
public class ServiceDAO {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDAO.class.getSimpleName());

    private static final String TABLE_NAME = "service";
    private final JdbcTemplate template;

    public static final int SERVICE_NAME_LENGTH = 100;


    public ServiceDAO(JdbcTemplate template) {
        this.template = template;
    }

    void initTable() {
        final String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + '(' +
                "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(" + SERVICE_NAME_LENGTH + ") NOT NULL," +
                "description TEXT," +
                "date TIMESTAMP DEFAULT NOW()," +
                "rating TINYINT DEFAULT 0," +           // For example 45 = 4.5, 30 = 3.0 etc
                "price MEDIUMINT UNSIGNED DEFAULT 0," +
                "user_id MEDIUMINT UNSIGNED NOT NULL," +
                "photos TEXT," +
                "FOREIGN KEY(user_id) REFERENCES " + UserDAO.TABLE_NAME + "(id) " +
                "ON DELETE CASCADE," +
                "KEY(date), " +
                "KEY(rating)," +
                "KEY(price))" +
                "DEFAULT CHARSET utf8 DEFAULT COLLATE utf8_general_ci";
        template.execute(createTable);
        logger.info("Table service initialized");
    }


    public long addService(Service service) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(new ServicePstCreator(service), keyHolder);
        final Map<String, Object> keys = keyHolder.getKeys();
        BigInteger id =  (BigInteger) keys.get("GENERATED_KEY");
        return id.longValue();
    }

    public void updateService(Service service) {
        final String query = "UPDATE " + TABLE_NAME + " SET name=?, description=?, price=?, photos=? WHERE id=?";
        template.update(query, service.getName(), service.getDescription(), service.getPrice(), service.getId(), service.getPhotoFileNames());
    }

    public void removeService(long id) {
        final String query = "DELETE FROM " + TABLE_NAME + " WHERE id=?";
        template.update(query, id);
    }



    public Service getServiceById(long id)  {
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?;";
        try {
            return template.queryForObject(query, serviceMapper, id);
        }
        catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }


    public List<Service> getServices(int page, int limit) {
        final int offset = limit * (page - 1);
        final String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY date DESC LIMIT ? OFFSET ?";
        return template.query(query, serviceMapper, limit, offset);
    }

    public List<Service> getServicesForUser(long userId, int page, int limit) {
        final int offset = limit * (page - 1);
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = ? ORDER BY date DESC LIMIT ? OFFSET ?";
        return template.query(query, serviceMapper, userId, limit, offset);
    }



    private final RowMapper<Service> serviceMapper = (rs, i) -> {
        final Service service = new Service();
        service.setId(rs.getLong("id"));
        service.setName(rs.getString("name"));
        service.setDescription(rs.getString("description"));
        service.setRating(rs.getInt("rating"));
        service.setPrice(rs.getInt("price"));
        service.setUserId(rs.getLong("user_id"));
        Date date = rs.getTimestamp("date");
        service.setDateCreated(date);
        service.setRawPhotos(rs.getString("photos"));
        return service;
    };


    private static class ServicePstCreator implements PreparedStatementCreator {
        private final Service service;

        ServicePstCreator(Service service) {
            this.service = service;
        }
        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final String query = "INSERT INTO " + TABLE_NAME +
                    " (name, description, price, user_id, photos) VALUES (?,?,?,?,?);";
            final PreparedStatement pst = con.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, service.getName());
            pst.setString(2, service.getDescription());
            pst.setInt(3, service.getPrice());
            pst.setLong(4, service.getUserId());
            pst.setString(5, service.getRawPhotos());
            return pst;
        }
    }
}

