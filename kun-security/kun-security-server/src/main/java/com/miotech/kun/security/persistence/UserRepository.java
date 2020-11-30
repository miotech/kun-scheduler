package com.miotech.kun.security.persistence;

import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/29
 */
@Repository
public class UserRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public User find(Long id) {
        String sql = "select * from kun_user where id = ?";

        return jdbcTemplate.query(sql, rs -> {
            User user = new User();
            if (rs.next()) {
                user.setId(rs.getLong("id"));
                user.setName(rs.getString("name"));
            }
            return user;
        }, id);
    }

    public User findByName(String name) {
        String sql = "select * from kun_user where name = ?";

        return jdbcTemplate.query(sql, rs -> {
            User user = new User();
            if (rs.next()) {
                user.setId(rs.getLong("id"));
                user.setName(rs.getString("name"));
            }
            return user;
        }, name);
    }

    public List<User> findAll() {
        String sql = "select * from kun_user";

        return jdbcTemplate.query(sql, rs -> {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setName(rs.getString("name"));
                users.add(user);
            }
            return users;
        });
    }

    public User insert(UserInfo userInfo) {
        String sql = "insert into kun_user values " + toValuesSql(1, 2);

        Long id = IdGenerator.getInstance().nextId();
        jdbcTemplate.update(sql, id, userInfo.getUsername());

        return find(id);
    }
}
