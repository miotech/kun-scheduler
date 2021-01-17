package com.miotech.kun.security.persistence;

import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.model.entity.Resource;
import com.miotech.kun.security.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
public class UserRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private void convertNodeToUser(Map<String, Object> nodeMap, User user) {
        user.setId((Long) nodeMap.get("id"));
        user.setName((String) nodeMap.get("name"));
        user.setCreateUser((String) nodeMap.get("createUser"));
        user.setCreateTime((Long) nodeMap.get("createTime"));
        user.setUpdateUser((String) nodeMap.get("updateUser"));
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
        String query = "CREATE (u:USER {id: ?, name: ?,  password: ?, createUser: ?, createTime: ?, updateUser: ?, updateTime: ?}) RETURN u";

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
                userInfo.getPassword(),
                userInfo.getCreateUser(),
                userInfo.getCreateTime(),
                userInfo.getUpdateUser(),
                userInfo.getUpdateTime());
    }
}
