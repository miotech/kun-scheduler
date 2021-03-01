package com.miotech.kun.security.persistence;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.security.model.bo.UserGroupRequest;
import com.miotech.kun.security.model.entity.User;
import com.miotech.kun.security.model.entity.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2021/2/24
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class UserGroupRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserRepository userRepository;

    public UserGroup findByName(String groupName) {
        String query = "MATCH (ug:USER_GROUP {name: ?}) RETURN ug";

        return jdbcTemplate.query(query, rs -> {
            UserGroup userGroup = new UserGroup();
            if (rs.next()) {
                setUserGroupFieldFromResultSet(rs, userGroup);
            }
            return userGroup;
        }, groupName);
    }

    private void setUserGroupFieldFromResultSet(ResultSet rs, UserGroup userGroup) throws SQLException {
        Map<String, Object> nodeMap = rs.getObject("ug", Map.class);
        convertNodeToUserGroup(nodeMap, userGroup);
    }

    private void convertNodeToUserGroup(Map<String, Object> nodeMap, UserGroup userGroup) {
        userGroup.setId((Long) nodeMap.get("id"));
        userGroup.setName((String) nodeMap.get("name"));
        userGroup.setCreateUser(userRepository.getUsernameById((Long) nodeMap.get("createUser")));
        userGroup.setCreateTime((Long) nodeMap.get("createTime"));
        userGroup.setUpdateUser(userRepository.getUsernameById((Long) nodeMap.get("updateUser")));
        userGroup.setUpdateTime((Long) nodeMap.get("updateTime"));
    }

    public UserGroup addUserGroup(UserGroupRequest userGroupRequest) {
        String query = "CREATE (ug:USER_GROUP {id: ?, name: ?, createUser: ?, createTime: ?, updateUser: ?, updateTime: ?}) RETURN ug";

        Long id = IdGenerator.getInstance().nextId();

        return jdbcTemplate.query(query, rs -> {
            UserGroup userGroup = new UserGroup();
            if (rs.next()) {
                setUserGroupFieldFromResultSet(rs, userGroup);
            }
            return userGroup;
        },
                id,
                userGroupRequest.getGroupName(),
                userGroupRequest.getCreateUser(),
                userGroupRequest.getCreateTime(),
                userGroupRequest.getUpdateUser(),
                userGroupRequest.getUpdateTime());
    }

}
