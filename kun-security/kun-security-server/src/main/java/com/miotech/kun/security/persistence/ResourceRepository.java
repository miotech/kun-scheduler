package com.miotech.kun.security.persistence;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.security.model.bo.ResourceRequest;
import com.miotech.kun.security.model.entity.Resource;
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
 * @created: 2021/1/16
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class ResourceRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserRepository userRepository;

    public Resource findByName(String resourceName) {
        String query = "MATCH (res:RESOURCE {name: ?}) RETURN res";

        return jdbcTemplate.query(query, rs -> {
            Resource resource = new Resource();
            if (rs.next()) {
                setResourceFieldFromResultSet(rs, resource);
            }
            return resource;
        }, resourceName);
    }

    private void convertNodeToResource(Map<String, Object> nodeMap, Resource resource) {
        resource.setId((Long) nodeMap.get("id"));
        resource.setName((String) nodeMap.get("name"));
        resource.setCreateUser(userRepository.getUsernameById((Long) nodeMap.get("createUser")));
        resource.setCreateTime((Long) nodeMap.get("createTime"));
        resource.setUpdateUser(userRepository.getUsernameById((Long) nodeMap.get("updateUser")));
        resource.setUpdateTime((Long) nodeMap.get("updateTime"));
    }

    private void setResourceFieldFromResultSet(ResultSet rs, Resource resource) throws SQLException {
        Map<String, Object> nodeMap = rs.getObject("res", Map.class);
        convertNodeToResource(nodeMap, resource);
    }

    public Resource addResource(ResourceRequest resourceRequest) {
        List<Object> queryArgs = new ArrayList<>();
        String query;
        if (resourceRequest.getIsRoot()) {
            query = "CREATE (res:RESOURCE {id: ?, name: ?, createUser: ?, createTime: ?, updateUser: ?, updateTime: ?}) \n" +
                    "RETURN res AS res";
            queryArgs.add(IdGenerator.getInstance().nextId());
            queryArgs.add(resourceRequest.getResourceName());
            queryArgs.add(resourceRequest.getCreateUser());
            queryArgs.add(resourceRequest.getCreateTime());
            queryArgs.add(resourceRequest.getUpdateUser());
            queryArgs.add(resourceRequest.getUpdateTime());
        } else {
            query = "MATCH (btRes:RESOURCE {id: ?}) \n" +
                    "CREATE (res:RESOURCE {id: ?, name: ?, createUser: ?, createTime: ?, updateUser: ?, updateTime: ?})" +
                    "-[:BELONG_TO {id: ?, createUser: ?, createTime: ?, updateUser: ?, updateTime: ?}]->" +
                    "(btRes) \n" +
                    "RETURN res AS res";
            queryArgs.add(resourceRequest.getBelongToResourceId());
            queryArgs.add(IdGenerator.getInstance().nextId());
            queryArgs.add(resourceRequest.getResourceName());
            queryArgs.add(resourceRequest.getCreateUser());
            queryArgs.add(resourceRequest.getCreateTime());
            queryArgs.add(resourceRequest.getUpdateUser());
            queryArgs.add(resourceRequest.getUpdateTime());
            queryArgs.add(IdGenerator.getInstance().nextId());
            queryArgs.add(resourceRequest.getCreateUser());
            queryArgs.add(resourceRequest.getCreateTime());
            queryArgs.add(resourceRequest.getUpdateUser());
            queryArgs.add(resourceRequest.getUpdateTime());
        }

        return jdbcTemplate.query(query, rs -> {
            Resource resource = new Resource();
            if (rs.next()) {
                setResourceFieldFromResultSet(rs, resource);
            }
            return resource;
                },
                queryArgs.toArray());
    }

    public Long removeResource(Long id) {
        String query = "MATCH p=(res:RESOURCE {id: ?})-[:BELONG_TO]-(btRes:RESOURCE) DELETE res, relationships(p)";

        jdbcTemplate.update(query, id);
        return id;
    }
}
