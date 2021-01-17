package com.miotech.kun.security.persistence;

import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.security.model.bo.HasPermission;
import com.miotech.kun.security.model.bo.PermissionRequest;
import com.miotech.kun.security.model.constant.PermissionType;
import com.miotech.kun.security.model.entity.Permission;
import com.miotech.kun.security.model.entity.Permissions;
import org.neo4j.driver.v1.types.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 2021/1/19
 */
@Repository
public class PermissionRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public Permissions findByUserId(Long userId) {
        String query = "MATCH (u:USER {id: ?})-[p:HAS_PERMISSION]->(res:RESOURCE) RETURN p, res.name as resName";

        return jdbcTemplate.query(query, rs -> {
            Permissions permissions = new Permissions();
            while (rs.next()) {
                Permission permission = new Permission();
                setPermissionFieldFromResultSet(rs, permission);
                permission.setResourceName(rs.getString("resName"));
                permissions.add(permission);
            }
            return permissions;
        }, userId);
    }

    private void convertNodeToPermission(Map<String, Object> nodeMap, Permission permission) {
        permission.setId((Long) nodeMap.get("id"));
        permission.setType(PermissionType.valueOf((String) nodeMap.get("type")));
        permission.setCreateUser((String) nodeMap.get("createUser"));
        permission.setCreateTime((Long) nodeMap.get("createTime"));
        permission.setUpdateUser((String) nodeMap.get("updateUser"));
        permission.setUpdateTime((Long) nodeMap.get("updateTime"));
    }

    private void setPermissionFieldFromResultSet(ResultSet rs, Permission permission) throws SQLException {
        Map<String, Object> nodeMap = rs.getObject("p", Map.class);
        convertNodeToPermission(nodeMap, permission);
    }

    private List<Map> convertListToQueryMap(List<?> list) {
        return list.stream().map(JSONUtils::toJsonObject).collect(Collectors.toList());
    }

    public Permissions savePermission(PermissionRequest permissionRequest) {
        String query = "UNWIND ? as hasPermission \n" +
                "MATCH (u:USER {id: hasPermission.userId}), (res:RESOURCE {id: hasPermission.resourceId}) \n" +
                "MERGE (u)-[p:HAS_PERMISSION]->(res) \n" +
                "ON CREATE SET p.id = hasPermission.permissionId, p.type = hasPermission.permissionType, p.createUser = ?, p.createTime = ?, p.updateUser = ?, p.updateTime = ? \n" +
                "ON MATCH SET p.type = hasPermission.permissionType, p.updateUser = ?, p.updateTime = ? \n" +
                "RETURN p";

        for (HasPermission hasPermission : permissionRequest.getHasPermissions()) {
            hasPermission.setPermissionId(IdGenerator.getInstance().nextId());
        }

        return jdbcTemplate.query(query, rs -> {
            Permissions permissions = new Permissions();
            while (rs.next()) {
                Permission permission = new Permission();
                setPermissionFieldFromResultSet(rs, permission);
                permissions.add(permission);
            }
            return permissions;
        },
                convertListToQueryMap(permissionRequest.getHasPermissions()),
                permissionRequest.getCreateUser(),
                permissionRequest.getCreateTime(),
                permissionRequest.getUpdateUser(),
                permissionRequest.getUpdateTime(),
                permissionRequest.getUpdateUser(),
                permissionRequest.getUpdateTime());
    }

    public Long removePermission(Long id) {
        String query = "MATCH (n1)-[p:HAS_PERMISSION {id: ?}]-(n2) DELETE p";

        jdbcTemplate.update(query, id);
        return id;
    }
}
