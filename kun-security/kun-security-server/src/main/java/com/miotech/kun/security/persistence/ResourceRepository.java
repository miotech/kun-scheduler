package com.miotech.kun.security.persistence;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.security.model.bo.ResourceRequest;
import com.miotech.kun.security.model.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2021/1/16
 */
@Repository
public class ResourceRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private void convertNodeToResource(Map<String, Object> nodeMap, Resource resource) {
        resource.setId((Long) nodeMap.get("id"));
        resource.setName((String) nodeMap.get("name"));
        resource.setCreateUser((String) nodeMap.get("createUser"));
        resource.setCreateTime((Long) nodeMap.get("createTime"));
        resource.setUpdateUser((String) nodeMap.get("updateUser"));
        resource.setUpdateTime((Long) nodeMap.get("updateTime"));
    }

    private void setResourceFieldFromResultSet(ResultSet rs, Resource resource) throws SQLException {
        Map<String, Object> nodeMap = rs.getObject("res", Map.class);
        convertNodeToResource(nodeMap, resource);
    }

    public Resource addResource(ResourceRequest resourceRequest) {
        String query = "MATCH (btRes:RESOURCE {id: ?}) \n" +
                "CREATE (res:RESOURCE {id: ?, name: ?, createUser: ?, createTime: ?, updateUser: ?, updateTime: ?})" +
                "-[bt:BELONG_TO {id: ?, createUser: ?, createTime: ?, updateUser: ?, updateTime: ?}]->" +
                "(btRes) \n" +
                "RETURN res AS res";

        Long resourceId = IdGenerator.getInstance().nextId();
        Long belongToId = IdGenerator.getInstance().nextId();
        return jdbcTemplate.query(query, rs -> {
            Resource resource = new Resource();
            if (rs.next()) {
                setResourceFieldFromResultSet(rs, resource);
            }
            return resource;
                },
                resourceRequest.getBelongToResourceId(),
                resourceId,
                resourceRequest.getResourceName(),
                resourceRequest.getCreateUser(),
                resourceRequest.getCreateTime(),
                resourceRequest.getUpdateUser(),
                resourceRequest.getUpdateTime(),
                belongToId,
                resourceRequest.getCreateUser(),
                resourceRequest.getCreateTime(),
                resourceRequest.getUpdateUser(),
                resourceRequest.getUpdateTime());
    }

    public Long removeResource(Long id) {
        String query = "MATCH (res:RESOURCE {id: ?})-[bt: BELONG_TO]-(btRes: RESOURCE) DELETE res, bt";

        jdbcTemplate.update(query, id);
        return id;
    }
}
