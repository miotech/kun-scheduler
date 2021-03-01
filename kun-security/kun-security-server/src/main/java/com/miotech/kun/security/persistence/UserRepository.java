package com.miotech.kun.security.persistence;

import com.miotech.kun.common.utils.IdUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.security.common.ConfigKey;
import com.miotech.kun.security.common.UserStatus;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2020/6/29
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class UserRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public String getUsernameById(Long id) {
        if (IdUtils.equals(id, ConfigKey.DEFAULT_INTERNAL_PASS_TOKEN_ID)) {
            return ConfigKey.DEFAULT_INTERNAL_PASS_TOKEN_KEY;
        }
        String query = "MATCH (u:USER {id: ?}) RETURN u.name";

        return jdbcTemplate.queryForObject(query, String.class, id);
    }

    private void convertNodeToUser(Map<String, Object> nodeMap, User user) {
        user.setId((Long) nodeMap.get("id"));
        user.setName((String) nodeMap.get("name"));
        user.setPassword((String) nodeMap.get("password"));
        user.setFirstName((String) nodeMap.get("firstName"));
        user.setLastName((String) nodeMap.get("lastName"));
        user.setEmail((String) nodeMap.get("email"));
        user.setAuthOrigin((String) nodeMap.get("authOrigin"));
        user.setCreateUser(getUsernameById((Long) nodeMap.get("createUser")));
        user.setCreateTime((Long) nodeMap.get("createTime"));
        user.setUpdateUser(getUsernameById((Long) nodeMap.get("updateUser")));
        user.setUpdateTime((Long) nodeMap.get("updateTime"));
    }

    private void setUserFieldFromResultSet(ResultSet rs, User user) throws SQLException {
        Map<String, Object> nodeMap = rs.getObject("u", Map.class);
        convertNodeToUser(nodeMap, user);
    }

    public User find(Long id) {
        String query = "MATCH (u:USER {id: ?}) RETURN u";

        return jdbcTemplate.query(query, rs -> {
            User user = new User();
            if (rs.next()) {
                setUserFieldFromResultSet(rs, user);
            }
            return user;
        }, id);
    }

    public User findByName(String name) {
        String query = "MATCH (u:USER {name: ?}) RETURN u";

        return jdbcTemplate.query(query, rs -> {
            User user = new User();
            if (rs.next()) {
                setUserFieldFromResultSet(rs, user);
            }
            return user;
        }, name);
    }

    public List<User> findAllUser() {
        String query = "MATCH (u:USER) RETURN u.id as userId, u.name as username";

        return jdbcTemplate.query(query, rs -> {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("userId"));
                user.setName(rs.getString("username"));
                users.add(user);
            }
            return users;
        });
    }

    public User addUser(UserInfo userInfo) {
        String query = "CREATE (u:USER {id: ?, name: ?, email: ?, firstName: ?, lastName: ?, authOrigin: ?,  password: ?, createUser: ?, createTime: ?, updateUser: ?, updateTime: ?}) RETURN u";

        Long id = IdGenerator.getInstance().nextId();
        return jdbcTemplate.query(query, rs -> {
                    User user = new User();
                    if (rs.next()) {
                        setUserFieldFromResultSet(rs, user);
                    }
                    return user;
                },
                id,
                userInfo.getUsername(),
                userInfo.getEmail(),
                userInfo.getFirstName(),
                userInfo.getLastName(),
                userInfo.getAuthOrigin(),
                userInfo.getPassword(),
                id,
                userInfo.getCreateTime(),
                id,
                userInfo.getUpdateTime());
    }

    public Long updateUserStatus(Long id, UserStatus userStatus) {
        String query = "MATCH (u:USER {id: ?}) SET u.status = ?";

        jdbcTemplate.update(query, id, userStatus.name());
        return id;
    }
}
