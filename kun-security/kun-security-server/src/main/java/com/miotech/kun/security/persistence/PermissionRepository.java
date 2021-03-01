package com.miotech.kun.security.persistence;

import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.security.model.bo.HasPermissionRequest;
import com.miotech.kun.security.model.bo.SavePermissionRequest;
import com.miotech.kun.security.model.constant.EntityType;
import com.miotech.kun.security.model.constant.PermissionType;
import com.miotech.kun.security.model.entity.Permission;
import com.miotech.kun.security.model.entity.Permissions;
import org.apache.commons.collections4.CollectionUtils;
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
 * @created: 2021/1/19
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class PermissionRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserRepository userRepository;

    public Permissions find(HasPermissionRequest permissionRequest) {
        List<Object> queryArgs = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("MATCH p=(subject:")
                .append(permissionRequest.getSubjectType())
                .append(" {id: ?})-[:HAS_PERMISSION*]->")
                .append("(object");
        queryArgs.add(permissionRequest.getSubjectId());
        if (permissionRequest.getObjectType() != null) {
            queryBuilder.append(":").append(permissionRequest.getObjectType());
        }
        if (permissionRequest.getObjectId() != null) {
            queryBuilder.append(" {id: ?}");
            queryArgs.add(permissionRequest.getObjectId());
        }
        queryBuilder
                .append(")").append("\n")
                .append("RETURN last(relationships(p)) as hp, object.name as objectName, head(labels(object)) as objectType");

        return jdbcTemplate.query(queryBuilder.toString(), rs -> {
            Permissions permissions = new Permissions();
            while (rs.next()) {
                Permission permission = new Permission();
                setPermissionFieldFromResultSet(rs, permission);
                permission.setObjectName(rs.getString("objectName"));
                permission.setObjectType(EntityType.valueOf(rs.getString("objectType")));
                permissions.add(permission);
            }
            return permissions;
        }, queryArgs.toArray());
    }

    private void convertNodeToPermission(Map<String, Object> nodeMap, Permission permission) {
        permission.setId((Long) nodeMap.get("id"));
        permission.setType(PermissionType.valueOf((String) nodeMap.get("type")));
        permission.setCreateUser(userRepository.getUsernameById((Long) nodeMap.get("createUser")));
        permission.setCreateTime((Long) nodeMap.get("createTime"));
        permission.setUpdateUser(userRepository.getUsernameById((Long) nodeMap.get("updateUser")));
        permission.setUpdateTime((Long) nodeMap.get("updateTime"));
    }

    private void setPermissionFieldFromResultSet(ResultSet rs, Permission permission) throws SQLException {
        Map<String, Object> nodeMap = rs.getObject("hp", Map.class);
        convertNodeToPermission(nodeMap, permission);
    }

    public Permissions savePermission(SavePermissionRequest savePermissionRequest) {
        if (CollectionUtils.isEmpty(savePermissionRequest.getHasPermissions())) {
            return new Permissions();
        }

        StringBuilder deleteQueryBuilder = new StringBuilder();
        for (HasPermissionRequest hasPermission : savePermissionRequest.getHasPermissions()) {
            deleteQueryBuilder
                    .append("MATCH p=")
                    .append("(subject:").append(hasPermission.getSubjectType())
                    .append(" {id: ").append(hasPermission.getSubjectId()).append("})")
                    .append("-[:HAS_PERMISSION]->")
                    .append("(object:").append(hasPermission.getObjectType())
                    .append(" {id: ").append(hasPermission.getObjectId()).append("})").append("\n")
                    .append("DELETE head(relationships(p))");
        }
        jdbcTemplate.update(deleteQueryBuilder.toString());

        StringBuilder queryBuilder = new StringBuilder();

        for (HasPermissionRequest hasPermission : savePermissionRequest.getHasPermissions()) {
            queryBuilder
                    .append("MATCH ")
                    .append("(subject:").append(hasPermission.getSubjectType())
                    .append(" {id: ").append(hasPermission.getSubjectId()).append("})")
                    .append(", ")
                    .append("(object:").append(hasPermission.getObjectType())
                    .append(" {id: ").append(hasPermission.getObjectId()).append("})").append("\n")
                    .append("CREATE p=(subject)")
                    .append("-[:HAS_PERMISSION ").append("{id: ").append(IdGenerator.getInstance().nextId()).append(",")
                    .append("type: ").append("\"").append(hasPermission.getPermissionType()).append("\"").append(",")
                    .append("createUser: ").append(savePermissionRequest.getCreateUser()).append(",")
                    .append("createTime: ").append(savePermissionRequest.getCreateTime()).append(",")
                    .append("updateUser: ").append(savePermissionRequest.getUpdateUser()).append(",")
                    .append("updateTime: ").append(savePermissionRequest.getUpdateTime()).append("}]->(object)").append("\n")
                    .append("RETURN head(relationships(p)) as hp");
        }

        return jdbcTemplate.query(queryBuilder.toString(), rs -> {
            Permissions permissions = new Permissions();
            while (rs.next()) {
                Permission permission = new Permission();
                setPermissionFieldFromResultSet(rs, permission);
                permissions.add(permission);
            }
            return permissions;
        });
    }

    public Long removePermission(Long id) {
        String query = "MATCH p=(n1)-[:HAS_PERMISSION {id: ?}]-(n2) DELETE relationships(p)";

        jdbcTemplate.update(query, id);
        return id;
    }
}
