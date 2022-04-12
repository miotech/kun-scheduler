package com.miotech.kun.security.dao;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.security.common.UserStatus;
import com.miotech.kun.security.model.bo.UserExtensionInformation;
import com.miotech.kun.security.model.bo.UserRequest;
import com.miotech.kun.security.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserDao {

    private static final String USER_TABLE_NAME = "kun_security_user";

    private static final List<String> userCols = ImmutableList.of("id", "username", "password", "external_information", "created_at", "updated_at", "status");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User create(UserRequest userRequest) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(userCols.toArray(new String[0]))
                .into(USER_TABLE_NAME)
                .asPrepared()
                .getSQL();

        UserExtensionInformation extensionInformation = new UserExtensionInformation(userRequest.getEmail(), userRequest.getWeComId());

        jdbcTemplate.update(sql,
                IdGenerator.getInstance().nextId(),
                userRequest.getUsername(),
                userRequest.getPassword(),
                JSONUtils.toJsonString(extensionInformation),
                DateTimeUtils.now(),
                DateTimeUtils.now(),
                UserStatus.ACTIVE.name());

        return findByUsername(userRequest.getUsername());
    }

    public List<User> findAll() {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(userCols.toArray(new String[0]))
                .from(USER_TABLE_NAME)
                .getSQL();

        return jdbcTemplate.query(sql, UserMapper.INSTANCE);
    }

    public User findById(Long id) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(userCols.toArray(new String[0]))
                .from(USER_TABLE_NAME)
                .where("id = ?")
                .getSQL();
        try {
            return jdbcTemplate.queryForObject(sql, UserMapper.INSTANCE, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User findByUsername(String username) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(userCols.toArray(new String[0]))
                .from(USER_TABLE_NAME)
                .where("username = ?")
                .getSQL();

        try {
            return jdbcTemplate.queryForObject(sql, UserMapper.INSTANCE, username);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void updateStatus(Long id, UserStatus userStatus) {
        String query = DefaultSQLBuilder.newBuilder()
                .update(USER_TABLE_NAME)
                .set("status = ?")
                .where("id = ?")
                .getSQL();

        jdbcTemplate.update(query, userStatus.name(), id);
    }

    public static class UserMapper implements RowMapper<User> {
        public static final UserMapper INSTANCE = new UserMapper();

        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();

            user.setId(rs.getLong("id"));
            user.setName(rs.getString("username"));
            user.setPassword(rs.getString("password"));

            UserExtensionInformation extensionInformation = JSONUtils.toJavaObject(rs.getString("external_information"), UserExtensionInformation.class);
            if (extensionInformation != null) {
                user.setEmail(extensionInformation.getEmail());
                user.setWeComId(extensionInformation.getWeComId());
            }

            user.setCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("created_at")).toEpochSecond() * 1000);
            user.setUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("updated_at")).toEpochSecond() * 1000);

            return user;
        }
    }


}
